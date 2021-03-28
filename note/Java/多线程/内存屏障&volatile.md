多线程通常要保证原子性，可见性，有序性。volatile可以保证后两个，写的时候刷新的主存，读的时候也从主存读，且可以保证不发生指令重排。其实现方法就和内存屏障有关



[内存屏障](https://mp.weixin.qq.com/s?__biz=Mzg5OTU4ODc0Mg==&mid=2247484170&idx=1&sn=cf22994839dee382ae0987c689ca221e&scene=19#wechat_redirect)