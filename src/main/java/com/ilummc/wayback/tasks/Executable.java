package com.ilummc.wayback.tasks;

import com.ilummc.wayback.schedules.DelayedSchedule;
import com.ilummc.wayback.schedules.PeriodSchedule;
import com.ilummc.wayback.schedules.ProgressedSchedule;

import java.util.concurrent.TimeUnit;

public interface Executable extends Task {

    void execute() throws Exception;

    boolean terminate();

    void forceTerminate();

    double progress();

    long eta();

    @Override
    default Executable create() {
        return null;
    }

    default ProgressedSchedule schedule() {
        return ProgressedSchedule.of(this);
    }

    default DelayedSchedule scheduleDelayed(long delay, TimeUnit timeUnit) {
        return DelayedSchedule.of(this, delay, timeUnit);
    }

    default PeriodSchedule schedulePeriodic(long initial, long period, TimeUnit timeUnit) {
        return PeriodSchedule.of(this, initial, period, timeUnit);
    }

}
