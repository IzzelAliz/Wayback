package com.ilummc.wayback.tasks;

public interface Task {

    Executable create();

    String detail();

    String name();

}
