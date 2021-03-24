package com.example.gof

interface TvState {

    fun nextChannel() {
    }

    fun prevChannel() {
    }

    fun turnUp() {}

    fun turnDown() {
    }
}

class PowerOffState : TvState {

}

class PowerOnState : TvState {
    override fun nextChannel() {
        super.nextChannel()
        println("nextChannel")
    }

    override fun prevChannel() {
        super.prevChannel()
        println("prevChannel")
    }

    override fun turnDown() {
        super.turnDown()
        println("turnDown")
    }

    override fun turnUp() {
        super.turnUp()
        println("turnDown")
    }
}

interface PowerController {
    fun powerOn() {

    }

    fun powerOff() {

    }
}

class TvController : PowerController {

    private var mTvState: TvState? = null

    override fun powerOn() {
        super.powerOn()
        mTvState = PowerOnState()
    }

    override fun powerOff() {
        super.powerOff()
        mTvState = PowerOffState()
    }

    fun nextChannel() {
        mTvState?.nextChannel()
    }

    fun prevChannel() {
        mTvState?.prevChannel()
    }

    fun turnUp() {
        mTvState?.turnUp()
    }

    fun turnDown() {
        mTvState?.turnDown()
    }

}