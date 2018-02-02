package net.m4e.app.user.business;

import net.m4e.common.Entities;
import org.slf4j.*;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * A bean for purging user related resources.
 *
 * @author boto
 * Date of creation February 2, 2018
 */
public class UserResourcePurger {

    /**
     * Amount of hours for expiring a new registration.
     */
    public static final int REGISTER_EXPIRATION_HOURS = 24;

    /**
     * Amount of minutes for expiring a password reset request.
     */
    public static final int PASSWORD_RESET_EXPIRATION_MINUTES = 30;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Entities entities;
    private final Users users;

    public UserResourcePurger() {
        this.entities = null;
        this.users = null;
    }

    @Inject
    public UserResourcePurger(Entities entities,
                              Users users) {

        this.entities = entities;
        this.users = users;
    }

    /**
     * Purge all expired account registrations.
     *
     * @return Count of purged registrations
     */
    public int purgeAccountRegistrations() {
        Long now = (new Date()).getTime();
        int purgedRegistrations = 0;
        List<UserRegistrationEntity> allEntities = entities.findAll(UserRegistrationEntity.class);
        for (UserRegistrationEntity entity: allEntities) {
            Long duration = now - entity.getRequestDate();
            duration /= (1000 * 60 * 60);
            if (duration > REGISTER_EXPIRATION_HOURS) {
                purgedRegistrations++;
                deleteRegistrationEntity(entity);
            }
        }

        return purgedRegistrations;
    }

    private void deleteRegistrationEntity(UserRegistrationEntity registrationEntity) {
        UserEntity user = registrationEntity.getUser();
        registrationEntity.setUser(null);
        entities.delete(registrationEntity);
        if (user != null) {
            try {
                users.markUserAsDeleted(user);
            }
            catch(Exception ex) {
                LOGGER.warn("could not mark the user as deleted, id: " + user.getId());
            }
        }
    }

    /**
     * Purge all expired password reset requests.
     *
     * @return Count of purged password resets
     */
    public int purgePasswordResets() {
        Long now = (new Date()).getTime();
        int purgedPasswordResets = 0;
        List<UserPasswordResetEntity> resets = entities.findAll(UserPasswordResetEntity.class);
        for (UserPasswordResetEntity reset: resets) {
            Long duration = now - reset.getRequestDate();
            duration /= (1000 * 60);
            if (duration > PASSWORD_RESET_EXPIRATION_MINUTES) {
                purgedPasswordResets++;
                entities.delete(reset);
            }
        }

        return purgedPasswordResets;
    }
}
