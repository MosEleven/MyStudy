### ctl

#### 源码注解

```java
/**
The main pool control state, ctl, is an atomic integer packing two conceptual fields workerCount, indicating the effective number of threads runState, indicating whether running, shutting down etc In order to pack them into one int, we limit workerCount to (2^29)-1 (about 500 million) threads rather than (2^31)-1 (2 billion) otherwise representable. If this is ever an issue in the future, the variable can be changed to be an AtomicLong, and the shift/mask constants below adjusted. But until the need arises, this code is a bit faster and simpler using an int. The workerCount is the number of workers that have been permitted to start and not permitted to stop. The value may be transiently different from the actual number of live threads, for example when a ThreadFactory fails to create a thread when asked, and when exiting threads are still performing bookkeeping before terminating. The user-visible pool size is reported as the current size of the workers set. The runState provides the main lifecycle control, taking on values: RUNNING: Accept new tasks and process queued tasks SHUTDOWN: Don't accept new tasks, but process queued tasks STOP: Don't accept new tasks, don't process queued tasks, and interrupt in-progress tasks TIDYING: All tasks have terminated, workerCount is zero, the thread transitioning to state TIDYING will run the terminated() hook method TERMINATED: terminated() has completed The numerical order among these values matters, to allow ordered comparisons. The runState monotonically increases over time, but need not hit each state. The transitions are: RUNNING -> SHUTDOWN On invocation of shutdown(), perhaps implicitly in finalize() (RUNNING or SHUTDOWN) -> STOP On invocation of shutdownNow() SHUTDOWN -> TIDYING When both queue and pool are empty STOP -> TIDYING When pool is empty TIDYING -> TERMINATED When the terminated() hook method has completed Threads waiting in awaitTermination() will return when the state reaches TERMINATED. Detecting the transition from SHUTDOWN to TIDYING is less straightforward than you'd like because the queue may become empty after non-empty and vice versa during SHUTDOWN state, but we can only terminate if, after seeing that it is empty, we see that workerCount is 0 (which sometimes entails a recheck -- see below).
**/
```

#### 我的理解

```java
//高三位是状态位，后29位是数量，所以总worker数在[0,2^29-1]之间，当数量不够用时还可以换成long
//初始值设置为running状态、worker数量0
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

//高3位分隔线
private static final int COUNT_BITS = Integer.SIZE - 3;

//最大容量，取反则是状态位
//0x 0001_f ff ff ff
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

//0x 1110_0 00 00 00
private static final int RUNNING    = -1 << COUNT_BITS;
//0x 0
private static final int SHUTDOWN   =  0 << COUNT_BITS;
//0x 0010_0 00 00 00
private static final int STOP       =  1 << COUNT_BITS;
//0x 0100_0 00 00 00
private static final int TIDYING    =  2 << COUNT_BITS;
//0x 0110_0 00 00 00
private static final int TERMINATED =  3 << COUNT_BITS;
//写成2进制后可以看出，状态码用3位2禁止表示递增方向为：111->000->001->010->011;也满足带符号的大小排序

// Packing and unpacking ctl
private static int runStateOf(int c)     { return c & ~CAPACITY; }
private static int workerCountOf(int c)  { return c & CAPACITY; }
private static int ctlOf(int rs, int wc) { return rs | wc; }
```



### execute

#### 源码注解

```java
/*
 * Proceed in 3 steps:
 *
 * 1. If fewer than corePoolSize threads are running, try to
 * start a new thread with the given command as its first
 * task.  The call to addWorker atomically checks runState and
 * workerCount, and so prevents false alarms that would add
 * threads when it shouldn't, by returning false.
 *
 * 2. If a task can be successfully queued, then we still need
 * to double-check whether we should have added a thread
 * (because existing ones died since last checking) or that
 * the pool shut down since entry into this method. So we
 * recheck state and if necessary roll back the enqueuing if
 * stopped, or start a new thread if there are none.
 *
 * 3. If we cannot queue task, then we try to add a new
 * thread.  If it fails, we know we are shut down or saturated
 * and so reject the task.
 */
```
#### 我的理解

```java
//我认为整个执行最困难的点在于如何在多线程的环境下保证按照设计的流程走，每一步都需要考虑并发的可能性
//还有就是线程池的池体现在哪里了，目前还是创造线程，没看到线程的池利用
public void execute(Runnable command) {
    //先校验运行对象是否为空
    if (command == null)
        throw new NullPointerException();

    int c = ctl.get();
    /**
         * worker即代表当前线程数
         * 当前worker数量小于核心线程数时，尝试添加worker
         *   如果添加成功则直接返回
         *   添加失败则进入下一步
         * 我理解的失败的原因可能是
         *   状态不是running了
         *   核心线程数在跑的时候被别人占满了
         *   新创建的线程在alive状态（抛出异常）
         * 源码注解的原因看下面那个add Worker
         */
    if (workerCountOf(c) < corePoolSize) {
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    /**
         * 尝试把worker放进阻塞队列里面
         * 如果当前没有worker就尝试创建一个空worker
         */
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        if (!isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    /**、
         * 走到这个分支说明状态不是running，或者加不进阻塞队列
         * 就尝试创建非核心线程来添加worker
         */
    else if (!addWorker(command, false))
        reject(command);
}
```

### add Worker

#### 源码注解

```java
/**
 * Checks if a new worker can be added with respect to current
 * pool state and the given bound (either core or maximum). If so,
 * the worker count is adjusted accordingly, and, if possible, a
 * new worker is created and started, running firstTask as its
 * first task. This method returns false if the pool is stopped or
 * eligible to shut down. It also returns false if the thread
 * factory fails to create a thread when asked.  If the thread
 * creation fails, either due to the thread factory returning
 * null, or due to an exception (typically OutOfMemoryError in
 * Thread.start()), we roll back cleanly.
 *
 * @param firstTask the task the new thread should run first (or
 * null if none). Workers are created with an initial first task
 * (in method execute()) to bypass queuing when there are fewer
 * than corePoolSize threads (in which case we always start one),
 * or when the queue is full (in which case we must bypass queue).
 * Initially idle threads are usually created via
 * prestartCoreThread or to replace other dying workers.
 *
 * @param core if true use corePoolSize as bound, else
 * maximumPoolSize. (A boolean indicator is used here rather than a
 * value to ensure reads of fresh values after checking other pool
 * state).
 * @return true if successful
 */
```

#### 我的理解







### 收获

1. 状态流转可以使用类似ctl的手段，配置一套掩码和计算方式
   - 对于顺序状态好用，非顺序的可能还是直接赋值比较方便

