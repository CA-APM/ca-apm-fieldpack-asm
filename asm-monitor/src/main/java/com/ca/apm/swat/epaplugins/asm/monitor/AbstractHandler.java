package com.ca.apm.swat.epaplugins.asm.monitor;

public abstract class AbstractHandler implements Handler {
    private final Handler successor;

    public AbstractHandler(Handler successor) {
        this.successor = successor;
    }

    @Override
    public Handler getSuccessor() {
        return successor;
    }

}
