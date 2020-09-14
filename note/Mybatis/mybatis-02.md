## 1、[常用xml配置标签](https://mybatis.org/mybatis-3/zh/configuration.html#)

用于Mybatis配置文件，即之前那个配置数据库链接的文件，除了环境配置意外还有属性、设置、别名、映射器注册等选项

### properties属性

- resource：引用别的配置文件，通常是".properties"文件，里面就是单纯的键值对
- property：单独的每一个属性标签，如果重复定义会被引用的外部属性覆盖
- 配置过的属性可以通过${property_name}来获取其属性值
- 可以配置默认值（要添加一个特定的属性）
- 可以修改默认值的分隔符
- **优先级** ：方法传递的属性>外部配置的属性>内部标签的属性

### settings设置

- 太多了吧，缓存2个，懒加载3个，自动映射3个，

### typeAliases类型别名

- 类型别名可为 Java 类型设置一个缩写名字。 它仅用于 XML 配置，意在降低冗余的全限定类名书写。
- 可以单独配置，也可以用package包扫描。有注解用注解，没有就用首字母小写的类名
- 常见的Java类型有内建的别名（注意区分包装类和原始类型）

### tpyeHandler类型处理器

MyBatis 在设置预处理语句（PreparedStatement）中的参数或从结果集中取出一个值时， 都会用类型处理器将获取到的值以合适的方式转换成 Java 类型。

你可以重写已有的类型处理器或创建你自己的类型处理器来处理不支持的或非标准的类型。 具体做法为

- 实现 `org.apache.ibatis.type.TypeHandler` 接口
- 继承一个很便利的类 `org.apache.ibatis.type.BaseTypeHandler`
-  并且可以（可选地）将它映射到一个 JDBC 类型。

### objectFactory对象工厂

每次 MyBatis 创建结果对象的新实例时，它都会使用一个对象工厂（ObjectFactory）实例来完成实例化工作。 默认的对象工厂需要做的仅仅是实例化目标类，要么通过默认无参构造方法，要么通过存在的参数映射来调用带有参数的构造方法。 如果想覆盖对象工厂的默认行为，可以通过创建自己的对象工厂来实现。

### plugins插件

MyBatis 允许你在映射语句执行过程中的某一点进行拦截调用。默认情况下，MyBatis 允许使用插件来拦截的方法调用包括：

- Executor (update, query, flushStatements, commit, rollback, getTransaction, close, isClosed)
- ParameterHandler (getParameterObject, setParameters)
- ResultSetHandler (handleResultSets, handleOutputParameters)
- StatementHandler (prepare, parameterize, batch, update, query)

### environments环境

可以配置多种环境以适用于不同的情况，可以接受环境配置的两个方法签名是：

```java
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment);
SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader, environment, properties);
```

- 事务处理器有两种：jdbc和managed
- 数据源有三种：unpooled，pooled和JNDI

### databaseIdProvider数据库厂商标识

- MyBatis 可以根据不同的数据库厂商执行不同的语句，这种多厂商的支持是基于映射语句中的 `databaseId` 属性。 
- MyBatis 会加载带有匹配当前数据库 `databaseId` 属性和所有不带 `databaseId` 属性的语句。 如果同时找到带有 `databaseId` 和不带 `databaseId` 的相同语句，则后者会被舍弃。

### mapper映射器

用来注册mapper（写了Sql语句的地方），告诉Mybatis去哪里找这些sql语句。

- resource：相对路径的资源引用
- URL：完全限定资源定位符
- class：对应mapper的全限定名
- package：将包内的映射器接口实现都注册进来

## 2、xml的映射文件



## #和$符号【重点】

#在预处理时将参数用？替换，$会直接拼接sql语句，而且如果是string类型的不会自动加单引号