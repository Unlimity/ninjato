package com.agoda.ninjato.http

import com.agoda.ninjato.converter.BodyConverter
import com.agoda.ninjato.converter.ConverterFactories
import com.agoda.ninjato.misc.file
import com.agoda.ninjato.misc.formUrlEncoded
import org.junit.Test
import java.lang.reflect.Type

class BodyTest {

    lateinit var body: Body

    val factories = ConverterFactories().apply { this += object : BodyConverter.Factory {
        override fun requestConverter(type: Type): BodyConverter<*, Body>? {
            return object : BodyConverter<Any, Body> {
                override fun convert(instance: Any): Body {
                    return Body("testify_body")
                }
            }
        }

        override fun responseConverter(type: Type): BodyConverter<Body, *>? = null
    } }

    var delegate: Any? by Body.Delegate(factories)

    @Test
    fun testByteArray() {
        // Act
        body = Body(byteArrayOf(0, 1, 2, 3, 4, 5))

        // Assert
        assert(body.contentLength == 6)
        assert(body.asByteArray.contentEquals(byteArrayOf(0, 1, 2, 3, 4, 5)))
        assert(body.mediaType is MediaType.Plain)
    }

    @Test
    fun testString() {
        // Act
        body = Body("012345")

        // Assert
        assert(body.contentLength == 6)
        assert(body.asString == "012345")
        assert(body.mediaType is MediaType.Plain)
    }

    @Test
    fun testDelegate() {
        // Act
        delegate = Body("012345")

        // Assert
        assert((delegate as Body).asString == "012345")

        // Act
        delegate = byteArrayOf(0, 1, 2, 3, 4, 5)

        // Assert
        assert((delegate as Body).asByteArray.contentEquals(byteArrayOf(0, 1, 2, 3, 4, 5)))

        // Act
        delegate = "012345"

        // Assert
        assert((delegate as Body).asString == "012345")

        // Act
        delegate = Any()

        // Assert
        assert((delegate as Body).asString == "testify_body")
    }

    @Test
    fun testExtension() {
        val config = Request.Configurator.WithBody()

        //FormUrlEncoded Extension
        val formUrlEncodedBody = config.formUrlEncoded {
            "a" to "test param !"
            "b" to "c"
        }

        assert(formUrlEncodedBody.asString == "a=test+param+%21&b=c")
        assert(formUrlEncodedBody.mediaType is MediaType.FormUrlEncoded)

        //File Extension
        val temp = createTempFile()
        temp.writeBytes(byteArrayOf(0, 1, 2, 3, 4, 5))

        val fileBody = config.file(temp, MediaType.Gif())
        assert(fileBody.asByteArray.contentEquals(byteArrayOf(0, 1, 2, 3, 4, 5)))
        assert(fileBody.mediaType is MediaType.Gif)
    }
}
