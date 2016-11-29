# KeyWrapper

This wrapper converts passed `key` value into `com.aerospike.client.Key`. If you want to use **namespace** and **setName** specifyed in
**application.conf** file - then add 
```scala
val config = ConfigFactory.load()
val namespace = config.getString("ru-tinkoff-aerospike-dsl.keyWrapper-namespace"))
val setName = config.getString("ru-tinkoff-aerospike-dsl.keyWrapper-setName"))

implicit val dbs = DBCredentials(namespace, setName)
```
Or if you want to use different namespaces/setNames - call function 
```scala ru.tinkoff.aerospikemacro.converters.KeyWrapper.create[T](DBCredentials("ns", "setName"))```
to create each of them an pass explicitly.
 
Check it out with small example:
```scala
def getKey[K](k: K)(implicit kC: KeyWrapper[K]): Key = kC(k)
def withCustomKey[T](any: T, dbs: DBCredentials): Key = create(dbs)(any)
```
getKey("StringKey") should give ```scala new com.aerospike.client.Key("test", "test", "StringKey")```
withCustomKey("StringKey", DBCredentials("dbName01", "tableName1")) should give ```scala new com.aerospike.client.Key("dbName01", "tableName1", "StringKey")```

Types of created keys detected from passed value. I recommend to use `Int, Long, String, Boolean, Float, Double, Array[Byte], Seq, List or Map`. 
Also there is [ru.tinkoff.aerospikescala.domain.ByteSegment](./cookbook/domain.md) case class which corresponds to `com.aerospike.client.ByteSegmentValue`. 

For using some case class as a `Key` you will have to show how to store it in `Aerospike`.

For example:
```scala
case class Cat(name: String, age: Int)
```
We can store it as a `String`:
```scala
implicit val bValue = new KeyWrapper[Cat] {
  override def apply(cat: Cat): Key = new Key("test", "test", cat.toString)
}
```
`Note` it's recommended to use simple key values. Be careful! If you wrote some serialization for key value - get exact value when calling one of `Get functions`.