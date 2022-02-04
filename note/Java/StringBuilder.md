## 关于builder和buffer的知识点

String，Builder，Buffer

- 都实现了序列化和char sequence、appendable接口
- 后两个继承AblstractStringBuilder类，使用char [ ] value;没有final
- append会先检查数组大小，扩容是 max( <1+2, 新长度），先调用Arrays.copyOf，再调用System.arraycopy
- buffer的方法加了synchronized关键字，线程安全
- buffer使用了一个私有的 private transient char[] toStringCache 作为缓存，但只有在toString的时候起了作用。



## 常用方法

- append
- deleteCharAt
- delete
- setLength 清空首选
- lastIndexOf
- insert
- reverse