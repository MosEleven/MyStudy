## 常用依赖

```xml
<!-- https://mvnrepository.com/artifact/org.springframework/spring-webmvc -->
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-webmvc</artifactId>
  <version>5.2.7.RELEASE</version>
</dependency>
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <version>4.12</version>
  <scope>test</scope>
</dependency>
```

## 常用applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <context:annotation-config/>

</beans>
```

## 注解

- @Autowired 自动装配：通过类型、名字

  - 如果不能为一装配则需要配合@Qualifier（value=“xxx”）使用
  - （request=false）表示这个没有也行，不会报错，和@Nullable一样

- @Resource 自动装配：通过名字、类型

- @Component 组建：放在类上说明这个类被Spring管理了,标记这个类是一个组件，要被Spring托管

  - @value（“xxx”） 注入值：可以放在变量上也可以放在set方法上

  - 衍生注解，在web开发中会按照MVC三层架构分层

    - dao【@Repository】
    - service【@Service】
    - controller【@Controller】

    这四个注解的功能都是一样的，都是将类注册在spring中，装配bean

- @Scope（“xxx”） 作用域：用来标志singleton、prototype、request、session、application、websocket

## XML与注解

- xml可以用于任何场合

- 注解不是自己的类使用不了，维护相对复杂

- 最好的情况是：

  - xml负责管理bean

  - 注解负责属性注入

  - 要让注解生效就必须开启注解的支持

  - ```xml
    <context:component-scan base-package="com.zx"/>
    <context:annotation-config/>
    ```

## JavaConfig

可以完全脱离xml文件，仅使用Java类进行配置

- @Configuration

  他本身也会注册到Spring中被Spring容器托管，因为他本身也是一个component

  这就是一个配置类，相当于之前的applicationContext.xml

  - @Bean 注册一个Bean

    ```java
    //注册一个bean，相当于一个xml中的bean标签
    //id 就是函数名，class就是返回值
    @Bean
    public User getUser(){
      return new User();
    }
    //如果全是使用Java类配置的话就只能使用ACAC而不是CPX来获取IOC容器
    ApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
    User user = context.getBean("getUser", User.class);
    ```

- @Import（xxx.class）：导入一个配置类



## 需要进一步学习常见的几个注解

## 方式一：

xml配置使用<bean>和<constuctor>,<property>进行属性注入。包括怎么对array，list，set，map等进行注入

- 通常都需要有一个无参构造器
- p或者c命名空间对应property或constructor，写法会更简单一点

```xml
<bean id="user" class="com.zx.dao.User">
    <constructor-arg name="name" value="name name"/>
</bean>
<bean id="student" class="com.zx.pojo.Student">
    <property name="name" value="zn pig"/>
    <property name="address" ref="address"/>
    <property name="card">
        <map>
            <entry key="生份证" value="123456"/>
            <entry key="银行卡" value="666666"/>
        </map>
    </property>
</bean>
<bean id="user" class="com.zx.pojo.User" p:name="zn" p:age="8"/>
<bean id="user2" class="com.zx.pojo.User" c:name="znzn" c:age="66" scope="singleton"/>
```

## 方式二：

xml注册bean，使用标签进行属性注入

- 一定要开启注解的支持

```xml
<context:annotation-config/>
```

```java
public class People {
    @Autowired
    @Nullable
    private Dog dog;
    @Autowired
    private Cat cat;
}
public class User {
    @Value("zx-anno")
    public String name;
}
```

## 方式三：

纯Java配置，使用@Configuration和@ComponentScan( )，无需xml

```java
@Configuration
@ComponentScan("com.zx.pojo")
public class MyConfig {
    //注册一个bean，相当于一个xml中的bean标签
    //id 就是函数名，class就是返回值
    @Bean
    public User getUser(){
        return new User();
    }
}
```

