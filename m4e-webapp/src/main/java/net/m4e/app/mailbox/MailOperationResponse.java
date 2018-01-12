package net.m4e.app.mailbox;

/**
 * @author ybroeker
 */
class MailOperationResponse {
    MailOperation operation;
    String id;

    public MailOperation getOperation() {
        return operation;
    }

    public void setOperation(final MailOperation operation) {
        this.operation = operation;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public MailOperationResponse(final String id) {
        this.id = id;
    }

    public MailOperationResponse(final MailOperation operation, final String id) {
        this.operation = operation;
        this.id = id;
    }
}
