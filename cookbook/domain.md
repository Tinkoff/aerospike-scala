# ByteSegment

Package: `ru.tinkoff.aerospikescala.domain`
```scala
case class ByteSegment(bytes: Array[Byte], offset: Int, length: Int)
```
corresponds to `com.aerospike.client.Value`
```java
 ByteSegmentValue(byte[] bytes, int offset, int length)
```

# Params for Call function

```scala
case class Param1(packageName: String, functionName: String, functionArgs: List[Value],
                      statement: Option[Statement] = None, listener: Option[ExecuteListener] = None) extends Param

case class Param2(statement: Statement, listener: Option[RecordSequenceListener] = None) extends Param

case class Param3(namespace: String, setName: String, binNames: List[String], callback: Option[ScanCallback] = None)

case class Param4(namespace: String, setName: String, binNames: List[String], listener: Option[RecordSequenceListener])

case class Param5(code: String, serverPath: String, language: Language)
```

# SingleBin[T]
This type works with `callKB` function. Available For any type of `Bin` and operations `Put, Append, Prepend, Add`.
```scala
case class SingleBin[B](name: String, value: B)
```

# MBin[T]
This type works with `callKB` function. Applicable when you need to pass more than one value in one operation. Available For any type of `Bin` and operations `Put, Append, Prepend, Add`.
```scala
case class MBin[B](values: Map[String, B])
```

# DBCredentials
Package: `ru.tinkoff.aerospikemacro.domain`
Contains database credentials - namespace and setName. 
```scala
case class DBCredentials(namespace: String, setname: String)
```
`Note` for test usage it's recommended to use ```scala DBCredentials("test", "test")```