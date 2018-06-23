package com.ilummc.wayback.schedules;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ilummc.tlib.resources.TLocale;
import com.ilummc.wayback.WaybackCommand;
import com.ilummc.wayback.WaybackConf;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class WaybackSchedules {

    private static final AtomicInteger COUNTER = new AtomicInteger(1);

    public static int nextId() {
        return COUNTER.getAndIncrement();
    }

    private final StatisticsExecutor executor;

    public void shutdown() throws InterruptedException {
        executor.shutdownNow();
        while (executor.isTerminating()) {
            TLocale.Logger.warn("AWAIT_TERMINATE");
            WaybackCommand.printRunning();
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        }
    }

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
        executor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static WaybackSchedules instance() {
        return WaybackTasksHolder.instance;
    }

    private WaybackSchedules() {
        this.executor = new StatisticsExecutor(WaybackConf.getConf().getPoolSize());
        this.executor.prestartAllCoreThreads();
    }

    private static class WrappedThreadFactory implements ThreadFactory {

        private static final AtomicInteger COUNT = new AtomicInteger(1);

        private final ThreadFactory factory;

        private WrappedThreadFactory(ThreadFactory factory) {
            this.factory = factory;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = factory.newThread(r);
            thread.setName("Wayback Schedules " + COUNT.getAndIncrement());
            return thread;
        }

    }

    private static class StatisticsExecutor extends ScheduledThreadPoolExecutor {

        private ArrayBlockingQueue<ProgressedSchedule> running;

        private List<ProgressedSchedule> getRunning() {
            return ImmutableList.copyOf(running);
        }

        private Map<RunnableScheduledFuture<?>, ProgressedSchedule> map = Collections.synchronizedMap(Maps.newHashMap());

        StatisticsExecutor(int corePoolSize) {
            super(corePoolSize);
            this.running = new ArrayBlockingQueue<>(corePoolSize);
            setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            setThreadFactory(new WrappedThreadFactory(getThreadFactory()));
        }

        List<ProgressedSchedule> getList() {
            HashSet<ProgressedSchedule> list = Sets.newHashSet();
            list.addAll(map.values());
            list.addAll(getQueue().stream().map(r -> mapTo(r).orElse(null))
                    .filter(Objects::nonNull).collect(Collectors.toList()));
            return ImmutableList.copyOf(list);
        }

        private Optional<ProgressedSchedule> mapTo(Runnable runnable) {
            if (runnable instanceof RunnableScheduledFuture)
                return Optional.ofNullable(map.get(runnable));
            else return Optional.empty();
        }

        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
            if (runnable instanceof ProgressedSchedule)
                map.put(task, (ProgressedSchedule) runnable);
            return super.decorateTask(runnable, task);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            this.mapTo(r).ifPresent(schedule -> running.add(schedule));
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            this.mapTo(r).ifPresent(schedule -> {
                running.remove(schedule);
                if (t != null) {
                    t.printStackTrace();
                    map.entrySet().removeIf(entry -> entry.getValue() == schedule);
                }
            });
        }

    }

    private static final class WaybackTasksHolder {
        private static WaybackSchedules instance = new WaybackSchedules();
    }

}
