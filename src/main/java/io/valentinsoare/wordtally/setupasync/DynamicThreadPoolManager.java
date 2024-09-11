package io.valentinsoare.wordtally.setupasync;

import io.valentinsoare.wordtally.outputformat.OutputFormat;
import org.springframework.core.task.TaskExecutor;

import java.util.concurrent.*;


/**
 * Manages dynamic thread pools for asynchronous task execution within the application.
 * Implements {@link TaskExecutor} to provide a custom thread pool execution strategy.
 * This manager allows for the configuration of core and maximum pool sizes, keep alive time, and queue size,
 * enabling fine-tuned control over resource allocation and task execution behavior.
 *
 * The thread pool is configured with a custom {@link ThreadFactory} for naming threads for easier debugging and monitoring,
 * and a {@link RejectedExecutionHandler} to handle tasks that cannot be executed immediately.
 */

public class DynamicThreadPoolManager implements TaskExecutor {
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int arrayQueueSize;

    private String nameOfTheWorkingThread;
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * Private constructor to prevent direct instantiation.
     * Initializes the thread pool with predefined configurations for core pool size, maximum pool size,
     * keep alive time, and queue size. It also sets a custom thread factory for naming threads and a rejection handler
     * for tasks that cannot be executed immediately.
     */
    private DynamicThreadPoolManager() {
        this.corePoolSize = 6;
        this.maxPoolSize = 9;
        this.keepAliveTime = 1;
        this.arrayQueueSize = 20000;
        this.nameOfTheWorkingThread = "working-thread";

        ThreadFactory threadFactoryExecTasks = (r -> {
            Thread t = new Thread(r);
            t.setName(String.format("%s-%s", nameOfTheWorkingThread, t.getId()));
            return t;
        });

        threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(arrayQueueSize),
                threadFactoryExecTasks,
                new AsynchronousRejectionHandler(new OutputFormat())
        );
    }

    /**
     * Factory method to create a new instance of {@link DynamicThreadPoolManager}.
     * This method ensures that the thread pool manager is instantiated with the default configuration.
     *
     * @return A new instance of {@link DynamicThreadPoolManager}.
     */
    public static DynamicThreadPoolManager generateNewDynamicThreadPoolManager() {
        return new DynamicThreadPoolManager();
    }

    /**
     * Executes a given task in the thread pool.
     * This method delegates the execution of the task to the {@link ThreadPoolExecutor} instance managed by this class.
     *
     * @param task The task to be executed.
     */
    @Override
    public void execute(Runnable task) {
        threadPoolExecutor.execute(task);
    }
}
