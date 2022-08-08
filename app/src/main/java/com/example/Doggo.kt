package com.example

data class Doggo(val name:String,val breed:String,val rating:Int =11)

fun main() {
    val doggo = Doggo("1", "1")
    val (name,breed) = doggo
    println(name+breed)
}