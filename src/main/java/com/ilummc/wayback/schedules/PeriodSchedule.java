package com.ilummc.wayback.schedules;

import com.ilummc.wayback.tasks.Executable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class PeriodSchedule extends ProgressedSchedule {

    private long initial, period;
    private long queuedTime, lastRun = 0;
    private TimeUnit timeUnit;

    public void setInitial(long initial) {
        this.initial = initial;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String timeToRun() {
        if (lastRun == 0) {
            Duration duration = Duration.ofMillis(timeUnit.toMillis(initial) - (System.currentTimeMillis() - queuedTime));
            return duration.toString().toLowerCase().substring(2);
        } else {
            Duration duration = Duration.ofMillis(timeUnit.toMillis(period) - (System.currentTimeMillis() - lastRun));
            return duration.toString().toLowerCase().substring(2);
        }
    }

    public String lastRun() {
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - lastRun);
        return duration.toString().toLowerCase().substring(2);
    }

    public long getLastRun() {
        return lastRun;
    }

    @Override
    public void run() {
        lastRun = System.currentTimeMillis();
        super.run();
    }

    @Override
    public void addToQueue() {
        id = WaybackSchedules.nextId();
        queuedTime = System.currentTimeMillis();
        WaybackSchedules.instance().period(this, initial, period, timeUnit);
    }

    public static PeriodSchedule of(Executable executable, long initial, long period, TimeUnit timeUnit) {
        PeriodSchedule.SimplePeriodSchedule schedule = new PeriodSchedule.SimplePeriodSchedule(executable);
        schedule.setInitial(initial);
        schedule.setPeriod(period);
        schedule.setTimeUnit(timeUnit);
        return schedule;
    }

    private static class SimplePeriodSchedule extends PeriodSchedule {

        private Executable executable;

        private SimplePeriodSchedule(Executable executable) {
            this.executable = executable;
        }

        @Override
        protected void execute() throws Exception {
            executable.execute();
        }

        @Override
        public String detail() {
            return executable.detail();
        }

        @Override
        public String name() {
            return executable.name();
        }

        @Override
        public double progress() {
            return executable.progress();
        }

        @Override
        public boolean terminate() {
            return executable.terminate();
        }

        @Override
        public void forceTerminate() {
            executable.forceTerminate();
        }

        @Override
        public long eta() {
            return executable.eta();
        }

        @Override
        public ProgressedSchedule copyOfRetry() {
            return executable.schedule();
        }

    }
}
