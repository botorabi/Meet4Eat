package net.m4e.app.mailbox;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * ! TODO get the attachments
 */
class NewMail {

    @Size(min = 1, max = MailEntityInputValidator.USER_INPUT_MAX_LEN_SUBJECT)
    private String subject;

    private String content;

    @Min(0)
    private String receiverId;


    public NewMail(final String subject, final String content, final String receiverId) {
        this.subject = subject;
        this.content = content;
        this.receiverId = receiverId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(final String receiverid) {
        this.receiverId = receiverId;
    }
}
