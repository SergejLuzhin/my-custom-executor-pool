import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final BlockingQueue<Runnable> taskQueue;
    private final ThreadFactory threadFactory;
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean isShutdown = false;
    private final AtomicInteger acceptedTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);

    public void taskCompleted() {
        completedTasks.incrementAndGet();
    }

    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, TimeUnit unit,
                            int queueSize, int minSpareThreads) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = unit;
        this.minSpareThreads = minSpareThreads;
        this.taskQueue = new ArrayBlockingQueue<>(queueSize);
        this.threadFactory = new CustomThreadFactory("MyPool");

        for (int i = 0; i < corePoolSize; i++) {
            addWorker();
        }
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) throw new RejectedExecutionException("Pool is shutting down");

        boolean offered = taskQueue.offer(command);
        if (offered) {
            acceptedTasks.incrementAndGet();
            System.out.println("[Pool] Task accepted into queue: " + command);
        } else {
            if (workers.size() < maxPoolSize) {
                addWorker();
                if (taskQueue.offer(command)) {
                    System.out.println("[Pool] Queue full, added new worker and accepted task: " + command);
                } else {
                    System.out.println("[Rejected] Task " + command + " was rejected after adding worker!");
                }
            } else {
                System.out.println("[Rejected] Task " + command + " was rejected due to overload!");
            }
        }

        maintainMinSpareThreads();
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(new TaskWrapper<>(callable));
        execute(future);
        return future;
    }

    @Override
    public synchronized void shutdown() {
        isShutdown = true;
        System.out.println("[Pool] Shutdown initiated.");
    }

    @Override
    public synchronized void shutdownNow() {
        isShutdown = true;
        for (Worker worker : workers) {
            worker.interrupt();
        }
        workers.clear();
        System.out.println("[Pool] Immediate shutdown.");
    }

    boolean isShutdown() {
        return isShutdown;
    }

    synchronized boolean shouldTerminate(Worker worker) {
        return workers.size() > corePoolSize;
    }

    synchronized void workerTerminated(Worker worker) {
        workers.remove(worker);
    }

    private synchronized void addWorker() {
        Worker worker = new Worker(taskQueue, this, keepAliveTime, timeUnit);
        workers.add(worker);
        Thread thread = threadFactory.newThread(worker);
        worker.setName(thread.getName());
        worker.start();
    }

    private synchronized void maintainMinSpareThreads() {
        long idleThreads = workers.stream()
                .filter(t -> t.getState() == Thread.State.WAITING || t.getState() == Thread.State.TIMED_WAITING)
                .count();

        if (idleThreads < minSpareThreads && workers.size() < maxPoolSize) {
            addWorker();
            System.out.println("[Pool] Ensured min spare threads. Added one.");
        }
    }

    public void awaitTermination() {
        while (true) {
            synchronized (this) {
                if (workers.isEmpty()) {
                    System.out.println("[Pool] All worker threads have terminated.");
                    System.out.println("[Pool] Total accepted tasks: " + acceptedTasks.get());
                    System.out.println("[Pool] Total completed tasks: " + completedTasks.get());
                    break;
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }

}
