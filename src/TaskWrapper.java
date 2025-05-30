import java.util.concurrent.Callable;

public class TaskWrapper<T> implements Callable<T> {
    private final Callable<T> task;

    public TaskWrapper(Callable<T> task) {
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        System.out.println("[Worker] " + Thread.currentThread().getName() + " executes " + task);
        return task.call();
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
