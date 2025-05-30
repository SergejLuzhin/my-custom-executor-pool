import java.util.concurrent.*;

public interface CustomExecutor extends Executor {
    <T> Future<T> submit(Callable<T> task);

    void shutdown();

    void shutdownNow();
}
