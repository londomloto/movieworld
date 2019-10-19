package com.digitalent.submission.movieworld.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorHelper {
    private static ExecutorHelper instance;
    private final Executor disk;
    private final Executor network;
    private final Executor ui;

    private ExecutorHelper(Executor disk, Executor network, Executor ui) {
        this.disk = disk;
        this.network = network;
        this.ui = ui;
    }

    public static synchronized ExecutorHelper getInstance() {
        if (instance == null) {
            instance = new ExecutorHelper(
                    Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(3),
                    new MainThreadExecutor());
        }
        return instance;
    }

    public Executor disk() {
        return disk;
    }

    @SuppressWarnings("unused")
    public Executor network() {
        return network;
    }

    @SuppressWarnings("unused")
    public Executor ui() {
        return ui;
    }

    static class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    }
}
