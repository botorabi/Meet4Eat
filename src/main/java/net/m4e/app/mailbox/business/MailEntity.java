/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.mailbox.business;

import java.io.Serializable;
import java.util.Collection;

import javax.persistence.*;

import net.m4e.app.resources.DocumentEntity;

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
     * Mail sender's user name
     */
    private String senderName = "";

    /**
     * Mail receiver.
     */
    private Long receiverId = 0L;

    /**
     * Mail receiver's user name
     */
    private String receiverName = "";

    /**
     * Time stamp of sending mail (in milliseconds since epoch)
     */
    private Long sendDate = 0L;

    /**
     * The mail subject
     */
    private String subject = "";

    /**
     * The mail content limited to 10k characters
     */
    @Column(length=MAX_CONTENT_LENGTH)
    private String content = "";

    /**
     * Mail attachments
     */
    @OneToMany(cascade = {CascadeType.ALL})
    private Collection<DocumentEntity> attachments;

    public MailEntity(final Long senderId, final String senderName, final Long receiverId, final String receiverName, final Long sendDate, final String subject, final String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.sendDate = sendDate;
        this.subject = subject;
        this.content = content;
    }

    /**
     * JPA-Constructor.
     */
    public MailEntity() {
    }

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
     * @param senderId Mail sender ID
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * Get the mail sender name.
     * 
     * @return Mail sender name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Set the mail sender name.
     * 
     * @param senderName Mail sender name
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Get the mail receiver ID.
     * 
     * @return Receiver ID
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
     * Get the mail receiver name.
     * 
     * @return Receiver name
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * Set the mail receiver name.
     * 
     * @param receiverName The receiver name
     */
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * Set the timestamp of the mail.
     * 
     * @return Time stamp of sending in milliseconds since epoch
     */
    public Long getSendDate() {
        return sendDate;
    }

    /**
     * Get the timestamp of the mail.
     * 
     * @param sendDate Time stamp of sending in milliseconds since epoch
     */
    public void setSendDate(Long sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * Get the mail subject.
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
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "net.m4e.app.mailbox.business.MailEntity[ id=" + id + " ]";
    }
}
