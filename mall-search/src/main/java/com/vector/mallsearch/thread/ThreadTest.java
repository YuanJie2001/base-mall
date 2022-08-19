package com.vector.mallsearch.thread;

import java.util.Optional;
import java.util.concurrent.*;

/**
 * @ClassName ThreadTest
 * 
 * @Author YuanJie
 * @Date 2022/8/17 10:13
 */
public class ThreadTest {
    // 当前系统中应当只有一两个池,每个异步任务交由线程池执行
    /**
     * 七大参数
     * corePoolSize – the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set
     * maximumPoolSize – the maximum number of threads to allow in the pool
     * keepAliveTime – (maximumPoolSize - corePoolSize)when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
     * unit – the time unit for the keepAliveTime argument
     * workQueue – the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
     * threadFactory – the factory to use when the executor creates a new thread
     * handler – the handler to use when execution is blocked because the thread bounds and queue capacities are reached
     */

    /**
     * 工作顺序:
     * 1)、线程池创建,准备好core数量的核心线程，准备接受任务
     * 1.1、core满了，就将再进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
     * 1.2、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
     * 1.3、max满了就用RejectedExecutionHandLer拒绝任务
     * 1.4、max都执行完成，有很多空闲.在指定的时间keepAliveTime以后，释放max-core这些线程
     * <p>
     * new LinkedBLoclingDeque<>():默认是Integer的最大值。内存不够
     * <p>
     * 拒绝策略: DiscardOldestPolicy丢弃最旧的任务
     * AbortPolicy 丢弃新任务并抛出异常
     * CallerRunsPolicy 峰值同步调用
     * DiscardPolicy 丢弃新任务不抛异常
     * 一个线程池core 7; max 20 , queue: 5e，100并发进来怎么分配的;
     * 7个会立即得到执行，50个会进入队列，再开13个进行执行。剩下的30个就使用拒绝策略。
     */
    // 创建自定义线程
    public static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100000),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // CompletableFuture异步编排,类似vue中promise,Docker-compose容器编排
        System.out.println("main...start....");

        // 无返回值
//        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程池: " + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果: " + i);
//        }, threadPoolExecutor);
        // 有返回值 且进行异步编排
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    System.out.println("当前线程池: " + Thread.currentThread().getId());
                    String i = String.valueOf(10 / 2);
                    System.out.println("运行结果: " + i);
                    return i;
                }, threadPoolExecutor)
                // 接收上一步的结果和异常
                .handle((result, error) -> {
                    if (Optional.ofNullable(result).isPresent()) {
                        return "success";
                    }
                    return "error";
                });
        String s = future.get();
        System.out.println("main...end...." + s);

        // 线程串行化 thenRunAsync 没法获取上一步执行结果
        System.out.println("main---start---");
        CompletableFuture<Void> VoidCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("当前线程池: " + Thread.currentThread().getId());
                    int i = 10 / 2;
                    System.out.println("运行结果: " + i);
                    return i;
                }, threadPoolExecutor)
                .thenRunAsync(() -> {
                    System.out.println("thenRunAsync()继续执行其他任务,没法获取上一步执行结果");
                }, threadPoolExecutor);
        System.out.println("main---end---");

        // 线程串行化 thenAcceptAsync 能接收上一步结果但无返回值
        CompletableFuture<Void> NullCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("当前线程池: " + Thread.currentThread().getId());
                    int i = 10 / 2;
                    System.out.println("运行结果: " + i);
                    return i;
                }, threadPoolExecutor)
                .thenAcceptAsync((result) -> {
                    System.out.println("thenRunAsync()作为程序的最后执行结果,无返回值, i= " + result);
                }, threadPoolExecutor);

        // 线程串行化 thenApplyAsync 可以处理上一步结果,有返回值
        System.out.println("main---start---");
        CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
                    System.out.println("当前线程池: " + Thread.currentThread().getId());
                    int i = 10 / 2;
                    System.out.println("运行结果: " + i);
                    return i;
                }, threadPoolExecutor)
                .thenApplyAsync((result) -> {
                    System.out.println("thenRunAsync()可以处理上一步结果,有返回值");
                    result = result * 2;
                    return "最新的i = " + result;
                }, threadPoolExecutor);

        System.out.println("main---end---" + stringCompletableFuture.get());


        // 线程串行化 多任务组合
        System.out.println("main...start....");
        CompletableFuture<String> work01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务work01进行中");
            return "info 1";
        }, threadPoolExecutor);
        CompletableFuture<String> work02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务work02进行中");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "info 2";
        }, threadPoolExecutor);
        CompletableFuture<String> work03 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务work03进行中");
            return "info 3";
        }, threadPoolExecutor);

//        work01.get(); work02.get(); work03.get();....  get乃是每个线程都会被阻塞等待结果
        // 而allof()是一个非阻塞等待方法
        CompletableFuture<Void> allResult = CompletableFuture.allOf(work01, work02, work03);
        // 等待最长的任务执行完毕后,获得最终结果
        allResult.get();
        System.out.println("main...end...." + work01.get() + "=>" + work02.get() + "=>" + work03.get());


//        // 一个成功即可
//        CompletableFuture<Object> anyResult = CompletableFuture.anyOf(work01,work02,work03);
//        System.out.println("main...end...."+allResult.get());
    }
}
