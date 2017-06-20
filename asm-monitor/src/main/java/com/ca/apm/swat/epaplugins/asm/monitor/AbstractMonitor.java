package com.ca.apm.swat.epaplugins.asm.monitor;

public abstract class AbstractMonitor implements Monitor {
    private final Handler successor;

    public AbstractMonitor(Handler successor) {
        this.successor = successor;
    }

    @Override
    public Handler getSuccessor() {
        return successor;
    }
}
