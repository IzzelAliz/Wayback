package com.ilummc.wayback.data;

public class FileChange {

    private final Change change;

    private final String file;

    public FileChange(Change change, String file) {
        this.change = change;
        this.file = file;
    }

    public Change getChange() {
        return change;
    }

    public String getFile() {
        return file;
    }

    public enum Change {
        MODIFY, DELETE, CREATE;
    }

}
