package no.nav.hm.finnhjelpemiddelnews

import io.micronaut.runtime.Micronaut
import kotlin.jvm.javaClass

object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.finnhjelpemiddelnews")
            .mainClass(Application.javaClass)
            .start()
    }
}
