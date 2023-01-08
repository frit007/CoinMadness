package org.coin_madness.helpers;

import org.jspace.RemoteSpace;
import org.jspace.Space;
import org.jspace.TemplateField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RemoteSpaceWithDisconnect implements Space {
    private RemoteSpace remoteSpace;
    List<Thread> threads = new ArrayList<>();

    public RemoteSpaceWithDisconnect(RemoteSpace remoteSpace) {
        this.remoteSpace = remoteSpace;
    }

    public synchronized void interruptAllThreads() {
        for (Thread thread: threads) {
            thread.interrupt();
        }
        new Thread(() -> {
            try {
                remoteSpace.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public int size() {
        return remoteSpace.size();
    }

    private synchronized void registerThread() {
        threads.add(Thread.currentThread());
    }

    private synchronized void deRegisterThread() {
        threads.remove(Thread.currentThread());
    }

    @Override
    public boolean put(Object... objects) throws InterruptedException {
        registerThread();
        boolean result = remoteSpace.put(objects);
        deRegisterThread();
        return result;
    }

    @Override
    public Object[] get(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        Object[] result = remoteSpace.get(templateFields);
        deRegisterThread();
        return result;
    }

    @Override
    public Object[] getp(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        Object[] result = remoteSpace.getp(templateFields);
        deRegisterThread();
        return result;
    }

    @Override
    public List<Object[]> getAll(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        List<Object[]> result = remoteSpace.getAll(templateFields);
        deRegisterThread();
        return result;
    }

    @Override
    public Object[] query(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        Object[] result = remoteSpace.query(templateFields);
        deRegisterThread();
        return result;
    }

    @Override
    public Object[] queryp(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        Object[] result = remoteSpace.queryp(templateFields);
        deRegisterThread();
        return result;
    }

    @Override
    public List<Object[]> queryAll(TemplateField... templateFields) throws InterruptedException {
        registerThread();
        List<Object[]> result = remoteSpace.queryAll(templateFields);
        deRegisterThread();
        return result;
    }
}
