package com.example.myapplication.factory

@Suppress("UNCHECKED_CAST")
class MoonCakesFactory {
    companion object{


        fun <T : MoonCakes> makeMoonCakes(clz: Class<T>): T {
            return Class.forName(clz.name).newInstance() as T
        }

        fun makeMoonCakes(type: String): MoonCakes? {
            var moonCakes:MoonCakes? = null
            when(type){
                "FiveKernel"->{
                   moonCakes = FiveKernel()
                }
                "Beansand"->{
                   moonCakes = Beansand()
                }
                else -> {
                    println(type)
                }
            }
            return moonCakes
        }

    }
    fun test(status:Status){
        println(status)
    }
}
enum class Status constructor(val value:Int){
    LOADING(1),FINISH(2)
}
fun main() {
    val makeMoonCakes = MoonCakesFactory.makeMoonCakes(FiveKernel::class.java)
//    makeMoonCakes.eat()
    val moonCakesFactory = MoonCakesFactory()
    moonCakesFactory.test(Status.LOADING)
    moonCakesFactory.test(Status.FINISH)
}