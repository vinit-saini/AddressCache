package com.unicommerce.cache.runner;

import com.unicommerce.cache.ExpirableCacheStack;
import com.unicommerce.cache.exception.CacheExpiredException;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This class is to test the implementation of Expirable cache. For readability & simplicity, It is made to store the
 * object type of Integer. Rest of the logic has no difference if compared with the AddressCache implementation.
 *
 * This class instantiates multiple threads to test the various multi-threading scenarios. like
 * parallel insertion of data into the cache by two add threads when other set of threads are busy in concurrent
 * read/pop operations and in delete operation.
 *
 * This class is just a plain driver class.
 *
 * Created by vsaini on 11/19/18.
 */
public class Tester {

    private static ExpirableCacheStack expirableCache = null;

    public static void __addDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void testRunner() {
        expirableCache = new ExpirableCacheStack(20, TimeUnit.SECONDS);

        Thread addThread = new Thread(new Runnable() {
            int cycleCount = 50;
            public void run() {

                System.out.println("in add run");
                while(cycleCount > 0) {
                    cycleCount--;

                    __addDelay(5);

                    try {
                        expirableCache.add(cycleCount);
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "Add-Thread");

        Thread peekThread = new Thread(new Runnable() {
            int cycleCount = 80;
            public void run() {
                System.out.println("in peek run");
                while(cycleCount > 0) {
                    cycleCount--;

                    __addDelay(10);

                    Integer out = null;
                    try {
                        out = expirableCache.peek();
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                    System.out.println("\nPeek@ - " + out);
                }
            }
        }, "Peek-Thread");

        Thread addThread2 = new Thread(new Runnable() {
            int cycleCount = 51;
            public void run() {
                System.out.println("in add run 2");
                while(cycleCount < 100) {
                    cycleCount++;
                    __addDelay(3);
                    try {
                        expirableCache.add(cycleCount);
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread peekThread2 = new Thread(new Runnable() {
            int cycleCount = 80;
            public void run() {
                System.out.println("in peek run");
                while(cycleCount > 0) {
                    cycleCount--;

                    __addDelay(10);

                    Integer out = null;
                    try {
                        out = expirableCache.peek();
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                    System.out.println("\nPeek# - " + out);
                }
            }
        }, "Peek-Thread-2");

        Thread removeThread = new Thread(new Runnable() {
            int cycleCount = 50;
            public void run() {
                System.out.println("in remove run");
                while(cycleCount > 0) {
                    cycleCount--;

                    __addDelay(60);

                    int dd = new Random().nextInt(100);
                    boolean out = false;
                    try {
                        out = expirableCache.remove(dd);
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                    System.out.println("\nremoving - " + dd + " :: "+out);
                }
            }
        }, "Remove-Thread");

        Thread takeThread = new Thread(new Runnable() {
            int cycleCount = 51;
            public void run() {
                System.out.println("in take run");
                while(cycleCount > 0) {
                    cycleCount--;

                    __addDelay(50);

                    Integer out = null;
                    try {
                        out = expirableCache.take();
                    } catch (CacheExpiredException e) {
                        e.printStackTrace();
                    }
                    System.out.println("\ntook - "+out);
                }
            }
        }, "Take-Thread");

        addThread2.start();
        addThread.start();
        peekThread.start();

        removeThread.start();
        peekThread2.start();
        takeThread.start();
    }


    public static void main(String[] args) {

        Tester tester = new Tester();
        tester.testRunner();

    }
}
