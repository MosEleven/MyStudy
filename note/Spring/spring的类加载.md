## spring容器类加载时机

通常java是在使用时才进行加载，spring由于有容器托管，是会先都加载一遍还是也是用的时候再加载呢？枚举呢

枚举实际上就是语法糖，就是单例模式，实际上枚举并没有交由spring托管啊！！！

spring托管的如果没有特殊处理，会在启动时加载

(clazz.getModifiers() & Modifier.ENUM) != 0



## @Autowired和@PostConstruct

通常spring的加载是先无参构造方法，再Autowired注入，再执行PostConstruct。

当@Auto wired注解在有参构造函数上时，会在构造前注入