package com.ilummc.wayback.policy;

import com.ilummc.wayback.tasks.Executable;

public interface Policy {

    void accept(Executable task);

    Policy create();

    void reset();

}
