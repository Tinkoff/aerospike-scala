# One Key-Bin type schema

Let's try to use `KBScheme[String, Int]` where `Key` is `String` and `Bin` is `Int`.
```scala
import ru.tinkoff.aerospike.dsl.{CallKB, SpikeImpl}
import ru.tinkoff.aerospike.dsl.scheme.KBScheme
import ru.tinkoff.aerospikemacro.converters._
import scala.concurrent.{ExecutionContext, Future}
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikeexamples.example.AClient
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}
import scala.concurrent.ExecutionContext.Implicits.global

case class SampleKBScheme(spike: SpikeImpl) extends KBScheme[String, Int] {
  implicit val dbc = AClient.dbc

  def putOne(k: String, a: SingleBin[Int])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Int](CallKB.Put, k, a)

  def putMany(k: String, a: MBin[Int])(implicit e: ExecutionContext): Future[Unit] = spike.callKB[String, Int](CallKB.Put, k, a)

  def getOne(k: String)(implicit e: ExecutionContext): Future[(String, Option[Int])] = spike.getByKey[String, Int](k, Nil).map(_._1.head)

  def getMany(k: String)(implicit e: ExecutionContext): Future[Map[String, Option[Int]]] = spike.getByKey[String, Int](k, Nil).map(_._1)
}
```
`def putOne` - this function will put one `Bin` with `Int` value,
`def putMany` - this one will put your `Map[String, Int]` to Aerospike like ```scala Key -> List[Bin] ```.
For example ```scala Map("a" -> 2, "b" -> 13)``` for key = "k1" will look like this: 
```scala k1 -> List(Bin("a", 2), Bin("b", 13)) ```

Run application to test that scheme:
```scala
import ru.tinkoff.aerospike.dsl.SpikeImpl
import ru.tinkoff.aerospikemacro.printer.Printer
import ru.tinkoff.aerospikescala.domain.{MBin, SingleBin}
import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf
import scala.language.experimental.macros
import scala.concurrent.ExecutionContext.Implicits.global

object KBSampleApp  extends App {

  val client = AClient.client
  val spike = new SpikeImpl(client)

  val myObj = SampleKBScheme(spike)

  myObj.putOne("oneKey", SingleBin("oneName", 2))
  myObj.putMany("manyKey", MBin(Map("aName" -> 2, "bName" -> 13)))

}
```
After running `KBSampleApp` select all data from that set:
```js
aql> select * from test.test33
[
  {
    "aName": 2,
    "bName": 13
  },
  {
    "oneName": 2
  }
]
```
