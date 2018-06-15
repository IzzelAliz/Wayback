package com.ilummc.wayback.schedules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ilummc.wayback.WaybackConf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WaybackSchedules {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public static int nextId() {
        return COUNTER.getAndIncrement();
    }

    private final StatisticsExecutor executor;

    @SuppressWarnings("unchecked")
    public List<ProgressedSchedule> getPending() {
        return executor.getList();
    }

    public List<ProgressedSchedule> getRunning() {
        return executor.getRunning();
    }

    void execute(ProgressedSchedule task) {
        executor.execute(task);
    }

    void delay(ProgressedSchedule task, long delay, TimeUnit timeUnit) {
        executor.schedule(task, delay, timeUnit);
    }

    void period(ProgressedSchedule task, long initialDelay, long period, TimeUnit unit) {
        executor.scheduleWithFixedDelay(task, initialDelay, period, unit);
    }

    public static WaybackSchedules instance() {
        return WaybackTasksHolder.instance;
    }

    private WaybackSchedules() {
        this.executor = new StatisticsExecutor(WaybackConf.getConf().getPoolSize());
        this.executor.prestartAllCoreThreads();
    }

    private static class StatisticsExecutor extends ScheduledThreadPoolExecutor {

        private ArrayBlockingQueue<ProgressedSchedule> running;

        private List<ProgressedSchedule> getRunning() {
            return ImmutableList.copyOf(running);
        }

        private Map<RunnableScheduledFuture<?>, ProgressedSchedule> map = new ConcurrentHashMap<>();

        StatisticsExecutor(int corePoolSize) {
            super(corePoolSize);
            this.running = new ArrayBlockingQueue<>(corePoolSize);
        }

        @SuppressWarnings("unchecked")
        List<ProgressedSchedule> getList() {
            ArrayList<RunnableScheduledFuture> list = Lists.newArrayList((BlockingQueue) getQueue());
            return list.stream().map(map::get).collect(Collectors.toList());
        }

        private Optional<ProgressedSchedule> maoTo(Runnable runnable) {
            if (runnable instanceof RunnableScheduledFuture)
                return Optional.ofNullable(map.get(runnable));
            else return Optional.empty();
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            if (runnable instanceof ProgressedSchedule)
                map.put(task, (ProgressedSchedule) runnable);
            return task;
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            this.maoTo(r).ifPresent(schedule -> running.add(schedule));
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            this.maoTo(r).ifPresent(schedule -> {
                running.remove(schedule);
                if (!schedule.isComplete()) {
                    map.entrySet().removeIf(entry -> entry.getValue() == schedule);
                }
            });
        }

    }

    private static final class WaybackTasksHolder {
        private static WaybackSchedules instance = new WaybackSchedules();
    }

}
