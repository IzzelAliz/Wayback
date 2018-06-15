package com.ilummc.wayback.schedules;

import com.ilummc.wayback.tasks.Executable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class DelayedSchedule extends ProgressedSchedule {

    private long delay;

    private long queuedTime;

    private TimeUnit timeUnit;

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String timeToRun() {
        Duration duration = Duration.ofMillis(timeUnit.toMillis(delay) - (System.currentTimeMillis() - queuedTime));
        return duration.toString().toLowerCase().substring(2);
    }

    @Override
    public void addToQueue() {
        id = WaybackSchedules.nextId();
        queuedTime = System.currentTimeMillis();
        WaybackSchedules.instance().delay(this, delay, timeUnit);
    }

    public static DelayedSchedule of(Executable executable, long delay, TimeUnit timeUnit) {
        SimpleDelayedSchedule schedule = new SimpleDelayedSchedule(executable);
        schedule.setDelay(delay);
        schedule.setTimeUnit(timeUnit);
        return schedule;
    }

    private static class SimpleDelayedSchedule extends DelayedSchedule {

        private Executable executable;

        private SimpleDelayedSchedule(Executable executable) {
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
