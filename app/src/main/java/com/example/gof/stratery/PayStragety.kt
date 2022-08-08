package com.example.gof.stratery

interface PayStragety {
    fun pay()
    fun paySuccess()
    fun payFail()
}

class AlipayStragety : PayStragety{
    override fun pay() {
    }

    override fun paySuccess() {
    }

    override fun payFail() {
    }

}