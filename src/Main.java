import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool pool = new CustomThreadPool(
                2,          // corePoolSize
                4,          // maxPoolSize
                5,          // keepAliveTime
                TimeUnit.SECONDS,
                5,          // queueSize
                1           // minSpareThreads
        );

        // Отправляем 15 задач (превышает размер очереди и потоков)
        for (int i = 0; i < 15; i++) {
            final int taskId = i;
            pool.execute(() -> {
                System.out.println("[Task] Start task " + taskId);
                try {
                    Thread.sleep(2000); // имитируем нагрузку
                } catch (InterruptedException ignored) {
                }
                System.out.println("[Task] End task " + taskId);
            });
        }

        pool.shutdown();
        pool.awaitTermination();
    }
}
