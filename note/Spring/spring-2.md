## 代理模式

Spring AOP的底层，面向切面编程的底层实现

代理模式的分类

- 静态代理
- 动态代理

### 静态代理

角色分析

- 抽象角色：一般由接口或抽象类完成
- 真实角色：被代理的角色，只做核心的工作
- 代理角色：代理真实角色，一般会有些附加操作
- 访问者：访问代理角色

优点：

- 真实角色的任务更加纯粹，公共业务交给代理去做
- 代理完成公共业务，实现了业务的分工
- 业务扩展时更好集中管理

缺点：

- 一个真实角色就会产生一个代理，工作量翻倍，效率降低，因为他要把真实角色的功能重写一遍放到代理角色里面，用代理角色去调用真实角色

### 动态代理 反射

- 动态代理和静态代理角色一样
- 动态代理类是动态生成的，不是直接写好的
- 可分为两大类
  - 基于接口的——jdk动态代理
  - 基于类的——cglib
  - Java字节码实现——Javasist

需要了解两个类：Proxy，InvocationHandler

#### 反射机制

```java
//反射机制的一些用法
Class cls = XXX.class;
Field field = cls.getfield("fname");
//field field = cls.getDeclaredField("fname");
//field.setAccessible(true);
Constructor con = cls.getDeclaredConstructor();
//con.setAccessible(true);
XXX xxx = (XXX)con.newInstance();
field.set(xxx,"new attr");
Method method = cls.getDeclaredMethod();
//method.setAccessible(true);
method.invoke(xxx,args);
```

#### 类加载器

加载、验证、准备、解析、初始化。加载的是.class文件

1. 启动类加载器：加载jvm自身需要的类，C++实现，是虚拟机的一部分
2. 扩展类加载器：
3. 应用类加载器：一般默认

- 双亲委派模型

  除了启动类加载器，别的类加载器都需要有父类，但并不是继承关系而是组合关系来复用类加载器的代码

```java
//类加载器
抽象类ClassLoader
  SecureClassLoader
  URLClassLoader    Launcher
  ExtClassLoader&AppClassLoader（Launcher的静态内部类）
```

#### Proxy和InvocationHandler

```java
//Proxy有一个静态方法，可以动态生成一个代理类并返回其实例，需要三个参数
//classloader，类加载器
//interfaces，接口数组（哪些方法被代理了）
//invocationhandler，调用处理器，负责具体的 “代理类调用被代理的接口的方法” 实现过程
Foo f = (Foo) Proxy.newProxyInstance(Foo.class.getClassLoader(),
                                          new Class<?>[] { Foo.class },
                                          handler);
/* method invocation on a proxy instance through one of its proxy interfaces will be dispatched to the invoke method of the instance's invocation handler, passing the proxy instance, a java.lang.reflect.Method object identifying the method that was invoked, and an array of type Object containing the arguments.*/
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
  Object result = method.invoke(target,args);
  return result;
}
//在这里的target表明是哪个具体的实现类来调用方法【只有这里】
```

#### 反射

#### 优点

- 一个动态代理类可以代理一系列接口，表示一类业务
- 可以代理多个类，只要这些类实现了被代理的接口的方法，只要在method.invoke中把被代理的类传进去

## AOP

### 概念

- 面向切面编程，通过预编译的方式和运行时期的动态代理实现程序功能的统一维护
- 业务逻辑的各部分进行分离，耦合度降低，提高了开发效率
- 在不影响业务类的情况下实现动态增强

### 术语

- https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-introduction-defn
- Aspect（切面）：横切关注点，被模块化的一个类
- Advice（通知）：切面要完成的动作，即类的方法
- Target object（目标）： An object being advised by one or more aspects. Also referred to as the “advised object”. Since Spring AOP is implemented by using runtime proxies, this object is always a proxied object.
- AOP proxy（代理）：向目标应用通知后创建的对象。An object created by the AOP framework in order to implement the aspect contracts (advise method executions and so on). In the Spring Framework, an AOP proxy is a JDK dynamic proxy or a CGLIB proxy.
- Join point:（连接点）：A point during the execution of a program, such as the execution of a method or the handling of an exception. In Spring AOP, a join point always represents a method execution.
- Pointcut:（切入点）：切面通知 执行的“地点”的定义。A predicate that matches join points. Advice is associated with a pointcut expression and runs at any join point matched by the pointcut (for example, the execution of a method with a certain name). The concept of join points as matched by pointcut expressions is central to AOP, and Spring uses the AspectJ pointcut expression language by default.

```xml
<!--使用AOP要添加这个依赖-->
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.6</version>
</dependency>
```



### 方式一：使用Spring API接口

```xml
<!--使用aop：config进行配置xml-->
<bean id="userService" class="com.zx.service.UserServiceImpl"/>
<bean id="log" class="com.zx.log.Log"/>
<bean id="afterLog" class="com.zx.log.AfterLog"/>

<aop:config>
    <aop:pointcut id="pointcut" expression="execution(* com.zx.service.UserServiceImpl.*(..))"/>
    <aop:advisor advice-ref="log" pointcut-ref="pointcut"/>
    <aop:advisor advice-ref="afterLog" pointcut-ref="pointcut"/>
</aop:config>
```

```java
//aspect类要实现Advice的接口
public class AfterLog implements AfterReturningAdvice {
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("执行了"+method.getName()+"方法，返回结果为"+returnValue);
    }
}
```

### 方式二：自定义类实现

重点是配置Aspect

```xml
<!--使用自定义类-->
<bean id="diy" class="com.zx.diy.Diy"/>
<aop:config>
    <aop:pointcut id="poing" expression="execution(* com.zx.service.UserServiceImpl.*(..))"/>
    <aop:aspect ref="diy">
        <aop:before method="before" pointcut-ref="poing"/>
        <aop:after-returning method="after" pointcut-ref="poing"/>
    </aop:aspect>
</aop:config>
```

```java
public class Diy {
    public void before(){
        System.out.println("======方法执行前======");
    }
    public void after(){
        System.out.println("======方法执行后======");
    }
}
```

### 方式三：使用注解实现

```xml
<!--只需要注册bean-->
<bean id="annoPointC" class="com.zx.diy.AnnoPointC"/>
<!--开启注解支持-->
<aop:aspectj-autoproxy/>
```

```java
@Aspect
public class AnnoPointC {
    @Before("execution(* com.zx.service.UserServiceImpl.*(..))")
    public void before(){
        System.out.println("=====方法执行前=====");
    }
}
```

#### 执行顺序：

Around环绕前--->Before--->AfterReturning--->After--->Around环绕后