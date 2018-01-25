/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox.business;

import net.m4e.app.user.business.UserEntity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import javax.persistence.*;

/**
 * This class joins a user and a mail. It is used to share the same mail
 * entity between mail sender and mail recipients. Furthermore it provides
 * information for mail state from a user perspective (e.g. "is read", "trashed").
 * 
 * @author boto
 * Date of creation Nov 1, 2017
 */
@Entity
@NamedQueries({
    /* Given a user ID return all its accociated mails */
    @NamedQuery(
      name="MailUserEntity.findMails",
      query = "SELECT mail, mailuser.unread, mailuser.trashDate FROM MailEntity mail, MailUserEntity mailuser WHERE mail.id = mailuser.mailId AND mailuser.trashDate IS NULL AND mailuser.userId = :userId ORDER BY mail.sendDate DESC"
    ),
    /* Given a user ID get the total count of its mails */
    @NamedQuery(
      name="MailUserEntity.countMails",
      query = "SELECT COUNT(mailuser) FROM MailUserEntity mailuser WHERE mailuser.trashDate IS NULL AND mailuser.userId = :userId"
    ),
    /* Given a user ID get the count of its unread mails */
    @NamedQuery(
      name="MailUserEntity.countUnreadMails",
      query = "SELECT COUNT(mailuser) FROM MailUserEntity mailuser WHERE mailuser.trashDate IS NULL AND mailuser.unread = true AND mailuser.userId = :userId"
    ),
    /* Return a MailUserEntity given its mail and user ID */
    @NamedQuery(
      name="MailUserEntity.findMailUser",
      query = "SELECT mailuser FROM MailUserEntity mailuser WHERE mailuser.mailId = :mailId AND mailuser.userId = :userId"
    )
})
public class MailUserEntity implements Serializable {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     * Unique entity ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * ID of the mail this entry references
     */
    private Long mailId = 0L;

    /**
     * ID of user referencing the mail.
     */
    private Long userId = 0L;

    /**
     * The 'unread' state of the mail. Once the mail is read by user this flag will be set to false.
     */
    private boolean unread = true;

    /**
     * The timestamp of 'trashing' the mail, i.e. marking it as 'free to delete'. Trashed mails can
     * get purged by system after some period of time. A trashed mail can get untrashed by setting this
     * timestamp to 0 since epoch.
     */
    private Instant trashDate;

    /**
     * Get ID.
     * @return ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set ID.
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the ID of referenced mail.
     * 
     * @return ID of referenced mail
     */
    public Long getMailId() {
        return mailId;
    }

    /**
     * Set the ID of referenced mail.
     * 
     * @param mailId ID of referenced mail
     */
    public void setMailId(Long mailId) {
        this.mailId = mailId;
    }

    /**
     * Get the ID of referring user.
     * 
     * @return ID of referring user
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Set the ID of referring user.
     * 
     * @param userId ID of referring user
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Is the mail marked as trash?
     * 
     * @return Return true if the mail is marked as trash
     */
    public boolean isTrashed() {
        return (trashDate != null) && (trashDate.getEpochSecond() != 0);
    }

    /**
     * Get the trash date in milliseconds since epoch.
     * 
     * @return Return the timestamp of trashing the mail
     */
    public Instant getTrashDate() {
        return trashDate;
    }

    /**
     * Mark the mail as trash by setting a trash timestamp. Trashed mails will get
     * deleted automatically after a period of time.
     * 
     * @param trashDate Date of trashing the mail
     */
    public void setTrashDate(Instant trashDate) {
        this.trashDate = trashDate;
    }

    /**
     * Is the mail in state 'unread'?
     * 
     * @return 'unread' state
     */
    public boolean isUnread() {
        return unread;
    }

    /**
     * Set the 'unread' state.
     * 
     * @param unread Pass false once the mail was read
     */
    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MailUserEntity)) {
            return false;
        }
        MailUserEntity other = (MailUserEntity) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.app.mailbox.business.MailUserEntity[ id=" + id + " ]";
    }
}
