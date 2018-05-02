package my.mimos.m3gnet.libraries.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ariffin.ahmad on 8/26/2016.
 */
public abstract class AbsWorkerThread implements Runnable {
    public final AtomicBoolean running = new AtomicBoolean(false);
    protected final AtomicBoolean stop = new AtomicBoolean(false);

    protected Thread thread;
    private int interval_msecs;
    private final boolean flag_once;

    public AbsWorkerThread() {
        this.interval_msecs = 0;
        this.flag_once      = true;
    }

    public AbsWorkerThread(int interval_msecs) {
        this.interval_msecs = interval_msecs;
        this.flag_once      = false;
    }

    public void start() {
        if (!running.get()) {
            stop.set(false);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (running.get()) {
            stop.set(true);
            thread.interrupt();
            try { thread.join(3 * 1000); } catch (InterruptedException e) {}
        }
    }

    public void wakeup() {
        if (running.get())
            thread.interrupt();
    }

    public void modifyInterval(int interval_msecs) {
        this.interval_msecs = interval_msecs;
    }

    @Override
    public void run() {
        running.set(true);

        if (!preProcess())
            stop.set(true);

        while (!stop.get()) {
            process();

            if (!stop.get() && interval_msecs > 0)
                try { Thread.sleep(interval_msecs); } catch (InterruptedException e) {}

            if (flag_once)
                break;
        }
        postProcess();

        running.set(false);
    }

    public boolean preProcess() {
        return true;
    }

    public void postProcess() {
    }

    public abstract void process();
}