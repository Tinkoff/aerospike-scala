# ProtoBinWrapper

Protobuf serialization is available only for types in bounds: 
```scala
I <: GeneratedMessage with Message[I] with Updatable[I]
```

To do that, you need to get generated models of your data with scalaPB plugin ```https://github.com/scalapb/ScalaPB```

After you've generated everything you needed you'll be able 
to create functions for operating with `brotobuffed` model.

For example, to put protobuffed data:

```scala
def put[K, I <: GeneratedMessage with Message[I] with Updatable[I], R <: ProtoBinWrapper[I]]
   (k: K, bin: SingleBin[I])(implicit kw: KeyWrapper[K],
                             bw: R, e: ExecutionContext,
                             pw: Option[WritePolicy] = None): Future[Unit] = {
     client.callKB[K, I](CallKB.Put, k, bin)(kw, bw, pw)
   }
```

...and to get it:

```scala 
   def get[I <: GeneratedMessage with Message[I] with Updatable[I]]
   (k: String)(implicit kw: KeyWrapper[String], bw: ProtoBinWrapper[I],
               e: ExecutionContext, pw: Option[WritePolicy] = None): Future[Map[String, Option[I]]] =
     absGet[String, I, ProtoBinWrapper[I]](k)(kw, bw, e, pw)
 }
```

, where ```def absGet``` is: 

```scala
def absGet[K, I <: GeneratedMessage with Message[I] with Updatable[I], R <: ProtoBinWrapper[I]]
   (k: K)(implicit kw: KeyWrapper[K], bw: R, e: ExecutionContext,
          pw: Option[WritePolicy] = None): Future[Map[String, Option[I]]] = {
     client.getByKey[K, I](k)(kw, bw, e, pw).map(r => r.map(_._1).getOrElse(throw new Exception("No data found")))
   }
```

After that you can call those functions to operate with Aerospike:

```scala
 val one = Designer("Karl Lagerfeld", 83)
  val many = Designers(List(one, Designer("Diane von Furstenberg", 70), Designer("Donatella Versace", 61)))

  db.put("protoDesigner", SingleBin("pDesigner", one))
  db.put("protoDesigners", SingleBin("pDesigners", many))

  db.get[Designer]("protoDesigner")
  db.get[Designers]("protoDesigners")
```

PS.
 And don't forget to import `import ProtoBinWrapper._` so it can create all wrappers for you

Full example is in `example` module.