# Logback

[配置详解](https://blog.csdn.net/u012129558/article/details/79947477)



configuration：

- 根节点
- 可配置类似于热加载的东西

appender：

- 配置各种日志输出器，以区分输出的格式，目的地
- 配合logger，不同的包可以打印不同的日志

logger：

- 指定包和对应的appender

root：

- 根logger，相当于全局配置