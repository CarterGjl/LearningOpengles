package com.example.gof

import android.widget.TextView
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class BusinessHandler(private val realRole: Any) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any? {
        //购买之前的额外评估
        println("购买之前的额外评估...");


        method.invoke(realRole, *args.orEmpty())
        //购买之后的满意度调查
        println("购买之后的满意度调查...");
        return null
    }

    fun <T> getProxy(): T {
        @Suppress("UNCHECKED_CAST")
        return Proxy.newProxyInstance(
            realRole.javaClass.classLoader,
            realRole.javaClass.interfaces,
            this
        ) as T
    }

}

interface IBusiness {
    fun buy()
}

class RealRole : IBusiness {
    override fun buy() {
        println("我要买东西")
    }
}

fun main() {
    val realRole = RealRole()
    val businessHandler = BusinessHandler(realRole = realRole)
////    val newProxyInstance = Proxy.newProxyInstance(
//        businessHandler.javaClass.classLoader, realRole.javaClass.interfaces,
//        businessHandler
//    )
    businessHandler.getProxy<IBusiness>().buy()
}