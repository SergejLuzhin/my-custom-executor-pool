import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private final String poolName;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = poolName + "-worker-" + threadNumber.getAndIncrement();
        System.out.println("[ThreadFactory] Creating new thread: " + name);
        Thread thread = new Thread(r, name);
        thread.setDaemon(false);
        return thread;
    }
}
