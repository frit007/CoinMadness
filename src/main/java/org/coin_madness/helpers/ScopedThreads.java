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

    public synchronized void startThread(String name, Runnable runnable) {
        if(hasBeenCleared) {
            return;
        }
        Thread thread = new Thread(() -> {
            Thread.currentThread().setName(name);
            addThread(Thread.currentThread());
            try {
                runnable.run();
            } catch (Exception e) {

            }
            removeThread(Thread.currentThread());
        });
        thread.start();
    }
    private synchronized void addThread(Thread thread) {
        threads.add(thread);
    }
    private synchronized void removeThread(Thread thread) {
        threads.remove(thread);
    }

    public synchronized List<Thread> getThreads() {
        return new ArrayList<>(threads);
    }

    public void startHandledThread(String threadName, HandledScreenThread runnable) {
        startThread(threadName,() -> {
            try {
                runnable.handle();
            } catch (InterruptedException e) {
                if(!hasBeenCleared) {
                    System.out.println("Scoped thread " + threadName + " Was interrupted to soon");
                    // only show error messages if the errors have not been cleared
                    e.printStackTrace();
                    onError.handle();
                }
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
