## 遍历方式比较



1. `iterator.next()`
2. `for(Object o : list)`
3. `list.get(i)`
4. `list.foreach(lambda表达式)`实际调用方法2

对于顺序存储，ArrayList或者Array来说，用简单的取值加法运算实测中，效率2>3>1，在别的资料中都说是3效率最高，可能是循环体复杂后2会变慢？所以简单运算用2，复杂的用3好了。

对于链式存储，LinkedList，就肯定是3最慢，数据量大的时候1比2快，数据量小的时候2比1快