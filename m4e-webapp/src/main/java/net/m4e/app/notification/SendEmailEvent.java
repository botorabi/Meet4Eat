/*
 * Copyright (c) 2017-2018 by Botorabi. All rights reserved.
 * https://github.com/botorabi/Meet4Eat
 * 
 * License: MIT License (MIT), read the LICENSE text in
 *          main directory for more details.
 */

package net.m4e.app.notification;

import java.util.List;

/**
 * Event used for sending an e-mail to a user.
 * 
 * @author boto
 * Date of creation Oct 1, 2017
 */
public class SendEmailEvent {

    /**
     * Recipients
     */
    private List<String> recipients;

    /**
     * Carbon copy recipients
     */
    private List<String> recipientsCC;

    /**
     * Blind copy recipients
     */
    private List<String> recipientsBCC;

    /**
     * E-Mail's subject
     */
    private String subject;

    /**
     * E-Mails body
     */
    private String body;

    /**
     * Is the body in HTML format? If not then plain text is assumed.
     */
    private boolean htmlBody = false;


    public SendEmailEvent() {}

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public List<String> getRecipientsCC() {
        return recipientsCC;
    }

    public void setRecipientsCC(List<String> recipientsCC) {
        this.recipientsCC = recipientsCC;
    }

    public List<String> getRecipientsBCC() {
        return recipientsBCC;
    }

    public void setRecipientsBCC(List<String> recipientsBCC) {
        this.recipientsBCC = recipientsBCC;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String title) {
        this.subject = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(boolean htmlBody) {
        this.htmlBody = htmlBody;
    }
}
