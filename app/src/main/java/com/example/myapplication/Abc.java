package com.example.myapplication;


public class Abc {

    public static class A {

    }
    public static class B extends A{

    }
    public static class C extends B{

    }
    public void test(){
        B b = new B();
        System.out.println("bbb"+(b instanceof A));
        C c = new C();
        System.out.println("cccc"+(c instanceof B));
        System.out.println("ca"+(c instanceof A));

    }

}
