/*
 * Copyright (c) 2017 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import net.m4e.app.resources.DocumentEntity;
import net.m4e.app.user.UserEntity;

/**
 * A class describing a mail
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@Entity
public class MailEntity implements Serializable {

    /**
     * Maximal length of mail content
     */
    private final static int MAX_CONTENT_LENGTH = 10240;

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
     * Mail Sender ID.
     */
    private Long senderId = 0L;

    /**
     * Mail receiver.
     */
    private Long receiverId = 0L;

    /**
     * Time stamp of sending mail (in millisenods since epoche)
     */
    private Long sendDate = 0L;

    /**
     * Mail's subject
     */
    private String subject = "";

    /**
     * Mail's content limited to 10k characters
     */
    @Column(length=MAX_CONTENT_LENGTH)
    private String content = "";

    /**
     * Mail attachments
     */
    @OneToMany(cascade = {CascadeType.ALL})
    private Collection<DocumentEntity> attachments;

    /**
     * The 'unread' state of the mail. Once the mail is read by user this flag will be set to false.
     */
    private boolean unread = true;

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
     * Get the mail sender.
     * 
     * @return Mail sender
     */
    public Long getSenderId() {
        return senderId;
    }

    /**
     * Set the mail sender.
     * 
     * @param senderId Mail Sender
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * Get the mail receiver.
     * 
     * @return Receiver
     */
    public Long getReceiverId() {
        return receiverId;
    }

    /**
     * Set the mail receiver.
     * 
     * @param receiverId The receiver
     */
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * Set the timestamp of the mail.
     * 
     * @return Time stamp of sending in milliseconds since ecpoche
     */
    public Long getSendDate() {
        return sendDate;
    }

    /**
     * Get the timestamp of the mail.
     * 
     * @param sendDate Time stamp of sending in milliseconds since ecpoche
     */
    public void setSendDate(Long sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * Get mail's subject.
     * 
     * @return The mail subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the mail subject.
     * 
     * @param subject The mail subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get the mail content.
     * 
     * @return The mail content
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the mail content. The length is limited, see definition of 'content'.
     * 
     * @param content The mail content
     */
    public void setContent(String content) {
        if (content.length() > MAX_CONTENT_LENGTH) {
            this.content = content.substring(0, MAX_CONTENT_LENGTH - 3);
            this.content += "...";
        }
        else {
            this.content = content;
        }
    }

    /**
     * Get the mail attachments.
     * 
     * @return The mail attachments if any exist. otherwise null
     */
    public Collection<DocumentEntity> getAttachments() {
        return attachments;
    }

    /**
     * Set the mail attachments.
     * 
     * @param attachments Mail attachments
     */
    public void setAttachments(Collection<DocumentEntity> attachments) {
        this.attachments = attachments;
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
        if (!(object instanceof MailEntity)) {
            return false;
        }
        MailEntity other = (MailEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "net.m4e.app.mailbox.MailEntity[ id=" + id + " ]";
    }
}
