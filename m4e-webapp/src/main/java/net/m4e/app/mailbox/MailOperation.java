package net.m4e.app.mailbox;

/**
 * @author ybroeker
 */
public enum MailOperation {
    TRASH, UNTRASH, READ, UNREAD;

    static MailOperation fromString(String string) {
        for (final MailOperation mailOperation : values()) {
            if (mailOperation.toString().equalsIgnoreCase(string)) {
                return mailOperation;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
