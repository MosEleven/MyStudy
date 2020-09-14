## 1、什么是Mybatis

- MyBatis 是一款优秀的持久层框架
- 它支持自定义 SQL、存储过程以及高级映射
- MyBatis 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作
- 可以通过简单的 XML 或注解将各种数据配置和映射为数据库中的记录。

### 持久化

- 就是将瞬时数据转化为持久数据，如磁盘文件，文本，数据库等
- 内存数据断电即失
- JDBC，IO，序列化都是持久化

### 持久层

- 完成持久化
- 完成对象数据和关系数据的转化

## 2、为什么要使用Mybatis

- 简单，没有第三方依赖
- 灵活，非入侵式
- 解除sql和代码的耦合。通过Dao层，xml配置sql语句
- orm映射

## 3、怎么使用Mybatis

### 基本原理

​		每个基于 MyBatis 的应用都是以一个 SqlSessionFactory 的实例为核心的。SqlSessionFactory 的实例可以通过 SqlSessionFactoryBuilder 获得。而 SqlSessionFactoryBuilder 则可以从 XML 配置文件或一个预先配置的 Configuration 实例来构建出 SqlSessionFactory 实例。

​		通过SqlSessionFactory获取一个SqlSession实例，这个实例提供了在数据库执行sql命令所需要的所有方法，既可以直接执行映射的sql语句，也可以通过获取一个Mapper（Dao接口）来执行sql方法，对应的sql语句可以配置在xml中，也可以使用注解写在方法上面。

### 命名空间的作用

​		利用全限定名来实现接口绑定，将不同的语句隔离开来

- 命名解析
  - 全限定名直接找
  - 短名称不唯一会报错

### 作用域和生命周期

- DI框架可以创建线程安全的，基于事务的SqlSession和映射器

- SqlSessionFactoryBuilder：一次性使用就可以丢弃，局部方法作用域

- SqlSessionFactory：一旦被创建就一直存在，应用作用域，单例模式（可以看作一个连接池）

- SqlSession：每个线程都应该有一个实例，应为它不是线程安全的，最佳作用域是请求或者方法作用域，**所有可能被多线程调用的地方都不能存放它的实例！**

- ```java
  try (SqlSession session = sqlSessionFactory.openSession()) {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
    // 你的应用逻辑代码
  }
  ```

- 映射器实例：最大作用域和产生该实例的SqlSession相同，但最适合用在方法作用域上

### 基本流程

1. 需要一个mybatis的核心配置文件，用于数据库的连接。包括环境，事务处理器，数据源等

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE configuration
     PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
     "http://mybatis.org/dtd/mybatis-3-config.dtd">
   <configuration>
     <environments default="development">
       <environment id="development">
         <transactionManager type="JDBC"/>
         <dataSource type="POOLED">
           <property name="driver" value="${driver}"/>
           <property name="url" value="${url}"/>
           <property name="username" value="${username}"/>
           <property name="password" value="${password}"/>
         </dataSource>
       </environment>
     </environments>
     <mappers>
       <mapper resource="org/mybatis/example/BlogMapper.xml"/>
     </mappers>
   </configuration>
   ```

2. 使用上面的xml配置生成一个SqlSessionFactory，最好用单例模式

   ```java
   public class MybatisUtil {
       private static SqlSessionFactory sqlSessionFactory;
       static {
           String resource = "mybatis-config.xml";
           InputStream inputStream = null;
           try {
               inputStream = Resources.getResourceAsStream(resource);
           } catch (IOException e) {
               e.printStackTrace();
           }
           sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
       }
   
       public static SqlSession getSqlSession(){
           return sqlSessionFactory.openSession();
       }
   
   }
   ```

   

3. 创建与数据库表对应的实体类（POJO）

4. 创建一个上述实体类的Mapper（Dao接口），负责具体的sql操作，进行对象模型和关系模型间的转换

5. 每个Mapper可能有好几个sql方法，可以用xml或者注解来配置

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper
     PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
     "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="org.mybatis.example.BlogMapper">
     <select id="selectBlog" resultType="Blog">
       select * from Blog where id = #{id}
     </select>
   </mapper>
   ```

   ```java
   public interface BlogMapper {
     @Select("SELECT * FROM blog WHERE id = #{id}")
     Blog selectBlog(int id);
   }
   ```

   

6. 主程序中通过SqlSessionFactory来获取一个SqlSession实例，通过这个实例来进行sql操作，官方推荐先getMapper，再调用其方法

   ```java
   BlogMapper mapper = session.getMapper(BlogMapper.class);
   Blog blog = mapper.selectBlog(101);
   ```

   

### 注意

- 每一个mapper都要在mybatis的核心配置文件中注册
- maven只会处理resource下的配置文件，要改一下maven配置的build内容



## 4、疑问

1. 请求作用域和方法作用域的区别
2. 每个请求都是一个单独的线程吗
3. @Controller是单例模式，SqlSession还需要手动关闭吗
4. 怎么体现出SqlSession可以比映射器的作用域更大？
   - 一个session生成多个mapper？

## 5、什么是数据源【附加】

​		数据源的“源”可能和我平时理解的“源”有些不一样。假设有一片湖，挖了几条沟渠引导出了湖水，在生活中我们说源头可能指的是那片湖本身，而数据源的“源”类比过来指的是那几条沟渠的集合。将湖水看作是数据，将沟渠们看作是数据源，则数据源的作用就是连接数据，并提供几条挖好的沟渠（有的也不提供）。

数据源需要做一下事情：

1. 提供数据的位置
2. 保证应用与数据间的交互，验证信息确保成功建立连接
3. 封装上述操作

数据源分为提供连接池和不提供连接池的

- 不提供连接池的每次请求连接就新开一个连接，用完就销毁，影响性能
- 提供连接池可以节省初始化和认证时间，节省性能