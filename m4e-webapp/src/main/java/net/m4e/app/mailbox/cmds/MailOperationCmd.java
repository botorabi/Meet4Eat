package net.m4e.app.mailbox.cmds;

import net.m4e.app.mailbox.MailOperation;

/**
 * @author ybroeker
 */
public final class MailOperationCmd {
    private MailOperation operation;

    public MailOperationCmd(final MailOperation operation) {
        this.operation = operation;
    }

    protected MailOperationCmd() {
    }

    public MailOperation getOperation() {
        return operation;
    }

    public void setOperation(final MailOperation operation) {
        this.operation = operation;
    }
}
