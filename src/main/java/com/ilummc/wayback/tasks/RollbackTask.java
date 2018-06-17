package com.ilummc.wayback.tasks;

public class RollbackTask implements Executable {

    @Override
    public void execute() throws Exception {

    }

    @Override
    public boolean terminate() {
        return false;
    }

    @Override
    public void forceTerminate() {

    }

    @Override
    public double progress() {
        return 0;
    }

    @Override
    public long eta() {
        return 0;
    }

    @Override
    public Executable create() {
        return null;
    }

    @Override
    public String detail() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

}
