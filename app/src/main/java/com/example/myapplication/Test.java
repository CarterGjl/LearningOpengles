//package com.example.myapplication;
//
//public class Test {
//
//    public synchronized void a(int i){
//        System.out.println(Thread.currentThread().getName()+"a"+i);
//    }
//    public synchronized void b(int b){
//        System.out.println(Thread.currentThread().getName()+"b"+b);
//    }
//    public static void main(String[] args) {
//
//        Test test = new Test();
//        new Thread("A"){
//            @Override
//            public void run() {
//                super.run();
//                for (int i = 0; i < 10; i++) {
//                    test.a(i);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//            }
//        }.start();
//        new Thread("B"){
//            @Override
//            public void run() {
//                super.run();
//                for (int i = 0; i < 10; i++) {
//                    test.b(i);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }.start();
//
//    }
//}
