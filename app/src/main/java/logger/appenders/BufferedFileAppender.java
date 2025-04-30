package logger.appenders;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.util.ExecutorServiceUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class BufferedFileAppender<E> extends FileAppender<E> {
    private int flushIntervalSec;
    private int maxBufferCount;

    @NonNull
    private final List<E> buffer = new ArrayList<>();

    @NonNull
    private ScheduledExecutorService scheduler;

    @Nullable
    private ScheduledFuture<?> scheduledFlush;

    @Override
    public void start() {
        super.start();
        scheduler = ExecutorServiceUtil.newScheduledExecutorService();
        scheduleNextFlush();
    }

    private void scheduleNextFlush() {
        if (scheduledFlush != null) {
            scheduledFlush.cancel(false);
        }
        scheduledFlush = scheduler.schedule(
                this::safeFlushIfNotEmpty,
                flushIntervalSec,
                TimeUnit.SECONDS
        );
    }

    private void safeFlushIfNotEmpty() {
        synchronized (buffer) {
            if (!buffer.isEmpty()) {
                safeFlush();
            }
        }
        scheduleNextFlush();
    }

    @Override
    public void stop() {
        try {
            if (scheduledFlush != null) {
                scheduledFlush.cancel(false);
            }
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                addError("Scheduler did not terminate gracefully, forcing shutdown");
                scheduler.shutdownNow();
            }
            safeFlush();
        } catch (InterruptedException e) {
            addError("Interrupted while stopping scheduler", e);
            Thread.currentThread().interrupt();
        } finally {
            super.stop();
        }
    }

    @Override
    protected void append(E event) {
        if (!isStarted() || event == null) {
            return;
        }

        if (event instanceof DeferredProcessingAware) {
            ((DeferredProcessingAware) event).prepareForDeferredProcessing();
        }

        synchronized (buffer) {
            buffer.add(event);
            if (buffer.size() >= maxBufferCount) {
                safeFlush();
                scheduleNextFlush();
            }
        }
    }

    private void safeFlush() {
        if (!isStarted()) {
            return;
        }
        List<E> copy;
        synchronized (buffer) {
            if (buffer.isEmpty()) {
                return;
            }
            copy = new ArrayList<>(buffer);
            buffer.clear();
        }
        try {
            for (E event : copy) {
                super.append(event);
            }
            getOutputStream().flush();
        } catch (Exception e) {
            addError("Failed to flush buffer, lost " + copy.size() + " events", e);
        }
    }

    public void setFlushIntervalSec(int flushIntervalSec) {
        if (flushIntervalSec <= 0) {
            throw new IllegalArgumentException("flushIntervalSec must be positive");
        }
        this.flushIntervalSec = flushIntervalSec;
    }

    public void setMaxBufferCount(int maxBufferCount) {
        if (maxBufferCount <= 0) {
            throw new IllegalArgumentException("maxBufferCount must be positive");
        }
        this.maxBufferCount = maxBufferCount;
    }
}