## 简介

注解实现的对象转换工具

## maven

```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-jdk8</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
```

## 使用案例

https://blog.csdn.net/zhige_me/article/details/80699784

https://blog.csdn.net/qq_35211818/article/details/104714363

### 普通映射

```java
@Mapper
public interface PersonConverter {
    PersonConverter INSTANCE = Mappers.getMapper(PersonConverter.class);
    @Mappings({
        @Mapping(source = "birthday", target = "birth"),
        @Mapping(source = "birthday", target = "birthDateFormat", dateFormat = "yyyy-MM-dd HH:mm:ss"),
        @Mapping(target = "birthExpressionFormat", expression = "java(org.apache.commons.lang3.time.DateFormatUtils.format(person.getBirthday(),\"yyyy-MM-dd HH:mm:ss\"))"),
        @Mapping(source = "user.age", target = "age"),
        @Mapping(target = "email", ignore = true)
    })
    PersonDTO domain2dto(Person person);

    //list类型的重载可以不用重新配置
    List<PersonDTO> domain2dto(List<Person> people);
}
```

```java
//这样使用
PersonDTO personDTO = PersonConverter.INSTANCE.domain2dto(person);
```

### 多对一

```java
@Mapper
public interface ItemConverter {
    ItemConverter INSTANCE = Mappers.getMapper(ItemConverter.class);

    @Mappings({
            @Mapping(source = "sku.id",target = "skuId"),
            @Mapping(source = "sku.code",target = "skuCode"),
            @Mapping(source = "sku.price",target = "skuPrice"),
            @Mapping(source = "item.id",target = "itemId"),
            @Mapping(source = "item.title",target = "itemName")
    })
    SkuDTO domain2dto(Item item, Sku sku);
}
```

### 自定义方法

```java
default Boolean convert2Bool(Integer value) {
    if (value == null || value < 1) {
        return Boolean.FALSE;
    } else {
        return Boolean.TRUE;
    }
}
```

### 更新

```java
// 比如在 PersonConverter 里面加入如下，@InheritConfiguration 用于继承刚才的配置
@InheritConfiguration(name = "domain2dto")
void update(Person person, @MappingTarget PersonDTO personDTO);

// 测试类 PersonConverterTest 加入如下
Person person = new Person(1L,"zhige","zhige.me@gmail.com",new Date(),new User(1));
PersonDTO personDTO = PersonConverter.INSTANCE.domain2dto(person);
assertEquals("zhige", personDTO.getName());
person.setName("xiaozhi");
PersonConverter.INSTANCE.update(person, personDTO);
assertEquals("xiaozhi", personDTO.getName());
```

### 继承反转配置

```java
//@InheritInverseConfiguration(name = "carToCarDto")
//不用重新反过来配置
@Mapper
public interface CarMapper {
	CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);
	   
    @Mapping(source = "make", target = "manufacturer")
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto carToCarDto(Car car);

	@InheritInverseConfiguration(name = "carToCarDto")
	Car carDTOTocar(CarDto carDto);
}
```

