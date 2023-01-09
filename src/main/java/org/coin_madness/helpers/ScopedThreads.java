package org.coin_madness.helpers;

import java.util.ArrayList;
import java.util.List;

// a collection of long-running threads that can be stopped at the same time
public class ScopedThreads {
    public static interface HandledScreenThread {
        void handle() throws InterruptedException;
    }
    private  List<Thread> threads = new ArrayList<>();
    private Action onError;
    private boolean hasBeenCleared = false;

    public ScopedThreads(Action onError) {
        this.onError = onError;
    }

    public synchronized void startThread(Runnable runnable) {
        if(hasBeenCleared) {
            return;
        }
        Thread thread = new Thread(runnable);
        threads.add(thread);
        thread.start();
    }
    public void startHandledThread(HandledScreenThread runnable) {
        startThread(() -> {
            try {
                runnable.handle();
            } catch (InterruptedException e) {
                if(!hasBeenCleared) {
                    // only show error messages if the errors have not been cleared
                    e.printStackTrace();
                }
                onError.handle();
            }
        });
    }

    public synchronized void cleanup() {
        for (Thread thread: threads) {
            thread.interrupt();
        }
        hasBeenCleared = true;
        threads.clear();
    }
}
