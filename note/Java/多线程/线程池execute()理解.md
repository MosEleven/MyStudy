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
//线程创建的时候传入了worker本身，start方法调用了worker重写的run方法，即runWorker()
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
            //按理是不可能走到这个分支的呀，没加进核心线程且线程池在运行状态，worker数还是0
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

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    //整个retry大循环就是为了ctl自增，代表worker+1
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // Check if queue empty only if necessary.
        if (rs >= SHUTDOWN &&
            ! (rs == SHUTDOWN &&
               firstTask == null &&
               ! workQueue.isEmpty()))
            return false;

        for (;;) {
            int wc = workerCountOf(c);
            if (wc >= CAPACITY ||
                wc >= (core ? corePoolSize : maximumPoolSize))
                return false;
            if (compareAndIncrementWorkerCount(c))
                break retry;
            c = ctl.get();  // Re-read ctl
            if (runStateOf(c) != rs)
                continue retry;
            // else CAS failed due to workerCount change; retry inner loop
        }
    }

    //两层try，外层负责在添加worker失败时调用addWorkerFailed函数，内层负责解锁
    //这段的作用是尝试将worker加入workerset并start线程
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask);
        final Thread t = w.thread;
        if (t != null) {
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                // Recheck while holding lock.
                // Back out on ThreadFactory failure or if
                // shut down before lock acquired.
                int rs = runStateOf(ctl.get());

                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    if (t.isAlive()) // precheck that t is startable
                        throw new IllegalThreadStateException();
                    workers.add(w);
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        if (! workerStarted)
            addWorkerFailed(w);
    }
    return workerStarted;
}
```

### addWorkerFailed

#### 源码注解

```java
Rolls back the worker thread creation. - removes worker from workers, if present - decrements worker count - rechecks for termination, in case the existence of this worker was holding up termination
```

#### 我的理解

```java
//ctl自减，并检查需不需要改变线程池状态
private void addWorkerFailed(Worker w) {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        if (w != null)
            workers.remove(w);
        decrementWorkerCount();
        tryTerminate();
    } finally {
        mainLock.unlock();
    }
}
```

### runWorker

```java
//核心：循环拿任务
try{
    while (task != null || (task = getTask()) != null){...}
}finally {
    //拿不到任务就说明该被销毁了
    processWorkerExit(w, completedAbruptly);
}
```



### getTask

#### 源码注解

```java
Performs blocking or timed wait for a task, depending on current configuration settings, or returns null if this worker must exit because of any of: 1. There are more than maximumPoolSize workers (due to a call to setMaximumPoolSize). 2. The pool is stopped. 3. The pool is shutdown and the queue is empty. 4. This worker timed out waiting for a task, and timed-out workers are subject to termination (that is, allowCoreThreadTimeOut || workerCount > corePoolSize) both before and after the timed wait, and if the queue is non-empty, this worker is not the last thread in the pool.
Returns:
task, or null if the worker must exit, in which case workerCount is decremented
```

#### 我的理解

```java
private Runnable getTask() {
    //刚进来的时候才开始空闲，当作没有超时
    boolean timedOut = false; // Did the last poll() time out?

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // 是否要停止获取任务
        //stop一定停止，shutdown&&无任务也停止
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }

        int wc = workerCountOf(c);

        // 实际上也没有核心非核心的区分，都是靠timed参数来控制的
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

        //超时或者超出上限都要扣worker
        if ((wc > maximumPoolSize || (timed && timedOut))
            && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }

        try {
            Runnable r = timed ?
                //会超时的，超时返回null
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
            	//等，直到拿到任务，下面就会return
                workQueue.take();
            if (r != null)
                return r;
            
            //能走到这里是一定超时了，因为take是一直等，直到有任务
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```





### 收获

1. 状态流转可以使用类似ctl的手段，配置一套掩码和计算方式
   - 对于顺序状态好用，非顺序的可能还是直接赋值比较方便
   - ctl还有一个巧妙的点就是他将状态和数量合到一个数里面了

