import java.util.concurrent.*;

public class Worker extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadPool pool;
    private final long keepAliveTimeMillis;

    public Worker(BlockingQueue<Runnable> taskQueue, CustomThreadPool pool, long keepAliveTime, TimeUnit unit) {
        this.taskQueue = taskQueue;
        this.pool = pool;
        this.keepAliveTimeMillis = unit.toMillis(keepAliveTime);
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (pool.isShutdown() && taskQueue.isEmpty()) break;

                Runnable task = taskQueue.poll(keepAliveTimeMillis, TimeUnit.MILLISECONDS);
                if (task != null) {
                    System.out.println("[Worker] " + getName() + " executes " + task);
                    try {
                        task.run();
                        pool.taskCompleted();
                    } catch (Exception e) {
                        System.out.println("[Worker] " + getName() + " encountered error in task: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    if (pool.shouldTerminate(this)) {
                        System.out.println("[Worker] " + getName() + " idle timeout, stopping.");
                        break;
                    }
                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            System.out.println("[Worker] " + getName() + " terminated.");
            pool.workerTerminated(this);
        }
    }
}
