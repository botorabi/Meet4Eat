/*
 * Copyright (c) 2017-2019 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 *
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */
package net.m4e.app.mailbox.business;

import net.m4e.app.resources.DocumentEntity;
import net.m4e.common.EntityBase;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * A class describing a mail
 * 
 * @author boto
 * Date of creation Oct 31, 2017
 */
@Entity
public class MailEntity extends EntityBase implements Serializable {

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

    /**
     * Get the entity ID.
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the entity ID.
     */
    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the mail sender.
     */
    public Long getSenderId() {
        return senderId;
    }

    /**
     * Set the mail sender.
     */
    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    /**
     * Get the mail sender name.
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Set the mail sender name.
     */
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Get the mail receiver ID.
     */
    public Long getReceiverId() {
        return receiverId;
    }

    /**
     * Set the mail receiver.
     */
    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    /**
     * Get the mail receiver name.
     */
    public String getReceiverName() {
        return receiverName;
    }

    /**
     * Set the mail receiver name.
     */
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    /**
     * Set the timestamp of the mail.
     */
    public Long getSendDate() {
        return sendDate;
    }

    /**
     * Get the timestamp of the mail.
     */
    public void setSendDate(Long sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * Get the mail subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the mail subject.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get the mail content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the mail content. The length is limited, see definition of 'content'.
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
     */
    public Collection<DocumentEntity> getAttachments() {
        return attachments;
    }

    /**
     * Set the mail attachments.
     */
    public void setAttachments(Collection<DocumentEntity> attachments) {
        this.attachments = attachments;
    }
}
