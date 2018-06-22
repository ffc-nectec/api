package ffc.airsync.api

import org.junit.Test

class EtcTest {


    class TESSS(val str: String) {
        var ii = 0

        init {
            println(str + " $ii")
            ii++
        }

        fun printDD() {
            println("DD")
        }
    }

    @Test
    fun Test001() {
        val objTest1 = TESSS("aaa")
        objTest1.printDD()
        objTest1.printDD()
    }
}
