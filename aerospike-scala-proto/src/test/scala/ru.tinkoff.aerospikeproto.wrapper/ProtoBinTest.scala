package ru.tinkoff.aerospikeproto.wrapper

import com.aerospike.client.Value
import com.aerospike.client.Value.BytesValue
import com.trueaccord.lenses.Updatable
import com.trueaccord.scalapb.{GeneratedMessage, Message}
import ru.tinkoff.aerospikeproto.wrapper.ProtoBinWrapper
import org.scalatest.{FlatSpec, Matchers}
import ru.tinkoff.aerospikeproto.designers.designers.{Designer, Designers}

/**
  * @author MarinaSigaeva 
  * @since 03.04.17
  */
class ProtoBinTest extends FlatSpec with Matchers {

  it should "transfer value into BytesValue" in new mocks {

    val expected0 = new BytesValue(one.toByteArray)
    val expected1 = new BytesValue(many.toByteArray)

    value(one) shouldBe expected0
    value(many) shouldBe expected1

  }

  trait mocks {

    val one = Designer("Karl Lagerfeld", 83)
    val many = Designers(List(one, Designer("Diane von Furstenberg", 70), Designer("Donatella Versace", 61)))

    def value[I <: GeneratedMessage with Message[I] with Updatable[I], R <: ProtoBinWrapper[I]]
    (v: I)(implicit bw: R): Value = bw.toValue(v)

  }

}

