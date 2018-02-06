/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox.business;

import net.m4e.app.user.business.UserEntity;
import net.m4e.common.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A collection of mailbox related utilities
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@ApplicationScoped
public class Mails {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final EntityManager entityManager;

    private final Entities entities;

    private static final String QUERY_PARAM_USER_ID = "userId";

    private static final String QUERY_PARAM_MAIL_ID = "mailId";


    /**
     * Default constructor needed by the container.
     */
    protected Mails() {
        entityManager = null;
        entities = null;
    }

    /**
     * Create an instance of mailbox utilities.
     * 
     * @param entityManager Entity manager
     * @param entities      Entities instance
     */
    @Inject
    public Mails(EntityManager entityManager, Entities entities) {
        this.entityManager = entityManager;
        this.entities = entities;
    }

    /**
     * Given a mail entity filled with all its fields, create it in database.
     * 
     * @param mail          Mail entity
     */
    public void createMail(MailEntity mail) {
        entities.create(mail);
        createMailUser(entities, mail.getId(), mail.getSenderId());
        // sometimes ppl send mails to themselves, catch that
        if (!Objects.equals(mail.getSenderId(), mail.getReceiverId())) {
            createMailUser(entities, mail.getId(), mail.getReceiverId());
        }
    }

    /**
     * Create mail for multiple recipients.
     * 
     * @param mail          Mail to create for recipients
     * @param recipients    Recipients
     */
    public void createMails(MailEntity mail, List<Long> recipients) {
        for (Long recipient: recipients) {
            MailEntity newMail = new MailEntity();
            newMail.setSenderId(mail.getSenderId());
            newMail.setSenderName(mail.getSenderName());
            newMail.setReceiverId(recipient);
            newMail.setReceiverName("");
            newMail.setSubject(mail.getSubject());
            newMail.setContent(mail.getContent());
            newMail.setSendDate((new Date()).getTime());
            try {
                createMail(newMail);
            }
            catch (Exception ex) {
                LOGGER.warn("*** could not create mail, reason: {}", ex.getMessage());
            }
        }
    }

    /**
     * Create the join table MailUserEntity for given user and mail IDs.
     * 
     * @param entities  Entities utils
     * @param mailId    Mail ID
     * @param userId    User ID
     */
    private void createMailUser(Entities entities, Long mailId, Long userId) {
        MailUserEntity mailUser = new MailUserEntity();
        mailUser.setMailId(mailId);
        mailUser.setUserId(userId);
        mailUser.setUnread(true);
        entities.create(mailUser);
    }

    /**
     * Get the total count of user's mails.
     * 
     * @param user  The user
     * @return      Total count of mails
     */
    public long getCountTotalMails(UserEntity user) {
        Query query = entityManager.createNamedQuery("MailUserEntity.countMails");
        query.setParameter(QUERY_PARAM_USER_ID, user.getId());
        return (long)query.getSingleResult();
    }

    /**
     * Get the count of user's unread mails.
     * 
     * @param user  The user
     * @return      The count of unread mails
     */
    public long getCountUnreadMails(UserEntity user) {
        Query query = entityManager.createNamedQuery("MailUserEntity.countUnreadMails");
        query.setParameter(QUERY_PARAM_USER_ID, user.getId());
        return (long)query.getSingleResult();
    }

    /**
     * Get user's mails in given range. Pass 0/0 as range in order to get all user mails (limited to 100 mails).
     * 
     * @param user  User entity
     * @param from  Range begin
     * @param to    Range end
     * @return      User mails in given range
     */
    public List<Mail> getMails(UserEntity user, int from, int to) {
        final int MAX_RANGE = 100;
        TypedQuery<Object[]> query = entityManager.createNamedQuery("MailUserEntity.findMails", Object[].class);
        query.setParameter(QUERY_PARAM_USER_ID, user.getId());
        List<Object[]> results;
        // limit the max range
        if (to > 0 && from >= 0) {
            if (to - from > MAX_RANGE) {
                to = from + MAX_RANGE;
            }
            results = query.setMaxResults(to - from + 1).setFirstResult(from).getResultList();
        }
        else {
            results = query.setMaxResults(MAX_RANGE).getResultList();
        }
        //! NOTE unfortunately (at least with JPA 2.0) it is not possible to easily store the results using a proper class type,
        //        so we have to do it manually here
        List<Mail> mails = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Object[] res = results.get(i);
            Long ts = (Long)res[2];
            mails.add(new Mail((MailEntity)res[0], (boolean)res[1], Instant.ofEpochMilli(ts == null ? 0L: ts)));
        }
        return mails;
    }

    /**
     * Find a MailUser entity. Return null if not found.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @return              MailUser if found or null if no entry for given user and mail ID exists.
     */
    public MailUserEntity findMailUser(Long userId, Long mailId) {
        TypedQuery<MailUserEntity> query = entityManager.createNamedQuery("MailUserEntity.findMailUser", MailUserEntity.class);
        query.setParameter(QUERY_PARAM_USER_ID, userId);
        query.setParameter(QUERY_PARAM_MAIL_ID, mailId);
        return query.getSingleResult();
    }

    /**
     * Trash or untrash a user mail. Trashed mails get purged after some period of time. Notice that
     * only the user specific reference to an actual mail is trashed, not the mail itself.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @param trash         Pass true to trash, false to untrash the mail
     * @throws Exception    Throws an exception if something goes wrong.
     */
    public void trashUserMail(Long userId, Long mailId, boolean trash) throws Exception {
        MailUserEntity mailUser = findMailUser(userId, mailId);
        if (mailUser == null) {
            throw new Exception("Mail does not exist.");
        }
        if (trash && mailUser.isTrashed()) {
            throw new Exception("Mail is already trashed.");
        }
        if (!trash && !mailUser.isTrashed()) {
            throw new Exception("Mail was not trashed.");
        }
        mailUser.setTrashDate(trash ? Instant.now() : null);
        entities.update(mailUser);
    }

    /**
     * Mark a user mail as 'read' or 'unread'.
     * 
     * @param userId        ID of user referring to the mail
     * @param mailId        The actual Mail ID
     * @param unread        Pass true for marking the mail as 'unread'
     * @throws Exception    Throws an exception if something goes wrong.
     */
    public void markAsUnreadUserMail(Long userId, Long mailId, boolean unread) throws Exception {
        MailUserEntity mailUser = findMailUser(userId, mailId);
        if (mailUser == null) {
            throw new Exception("Mail does not exist.");
        }
        mailUser.setUnread(unread);
        entities.update(mailUser);
    }

    /**
     * Perform a mail operation.
     *
     * @param userId    the ID of user referring to the mail
     * @param mailId    the actual Mail ID
     * @param operation the Mail operation
     * @throws UnsupportedOperationException if the {@code operation} is not supported
     */
    public ExcecutedMailOperation performMailOperation(Long userId, Long mailId, @NotNull MailOperation operation) throws Exception {
        switch (operation) {
            case TRASH:
                trashUserMail(userId, mailId, true);
                break;
            case UNTRASH:
                trashUserMail(userId, mailId, false);
                break;
            case READ:
                markAsUnreadUserMail(userId, mailId, false);
                break;
            case UNREAD:
                markAsUnreadUserMail(userId, mailId, true);
                break;
            default:
                throw new UnsupportedOperationException(String.format("operation %s not supported", operation));
        }

        return new ExcecutedMailOperation(operation, mailId.toString());
    }
}
