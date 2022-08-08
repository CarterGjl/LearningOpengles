package com;

import java.util.concurrent.Semaphore;

class H2O {
    private Semaphore h = new Semaphore(2);
    private Semaphore o = new Semaphore(0);
    public H2O() {

    }

    public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {

        h.acquire(1);
        // releaseHydrogen.run() outputs "H". Do not change or remove this line.
        releaseHydrogen.run();
        h.release(1);
    }

    public void oxygen(Runnable releaseOxygen) throws InterruptedException {

        o.acquire(2);
        // releaseOxygen.run() outputs "O". Do not change or remove this line.
        releaseOxygen.run();
        o.release(2);
    }

    public static void main(String[] args) throws InterruptedException {
        H2O h2O = new H2O();
        h2O.hydrogen(() -> {
            System.out.println("h");
        });
        h2O.oxygen(() -> {
            System.out.println("o");
        });


        String a = "daf ";
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println(a);
            }
        }).start();
    }
}
