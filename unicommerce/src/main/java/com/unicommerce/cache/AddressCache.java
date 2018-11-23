package com.unicommerce.cache;

import com.unicommerce.cache.exception.CacheExpiredException;

import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by vsaini on 11/19/18.
 */
/*
 * The AddressCache has a max age for the elements it's storing, an add method
 * for adding elements, a remove method for removing, a peek method which
 * returns the most recently added element, and a take method which removes
 * and returns the most recently added element.
 */
public class AddressCache {

    private long age;
    private TimeUnit unit;
    private long timeToLive;
    private ArrayDeque<InetAddress> inetAddressStack;
    private Thread purgeThread;
    private boolean isExpired = false;

    private ReentrantReadWriteLock.WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = new ReentrantReadWriteLock().readLock();
    private Condition waitTillDataArrive = writeLock.newCondition();

    public AddressCache(long maxAge, TimeUnit unit) {
        this.age = maxAge;
        this.unit = unit;
        this.timeToLive = unit.toMillis(age);

        inetAddressStack = new ArrayDeque<InetAddress>();


        /*
        * This thread is for counting on the life of the cache.
        * As soon as the TTL elapsed this thread is all set to perform two tasks
        * first - send a final signal to all waiting threads then
        * second - marking this cache as expired and invalidating.
        */
        this.purgeThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(timeToLive);
                    isExpired = true;
                    finalSignalBeforePurgingCache();
                    System.out.println("\nGoing to purge the cache now TTL-expired: ");
                    printCache();
                    inetAddressStack.clear();
                } catch (InterruptedException e) {
                    System.out.println("Error while setting-up cache : TimeToLive given : " + timeToLive);
                    e.printStackTrace();
                }
            }
        });
        // starting the purge thread to count on TTL.
        this.purgeThread.start();
    }

    /**
     * add() method must store unique elements only (existing elements must be ignored).
     * This will return true if the element was successfully added.
     *
     * @param address
     * @return
     */

    public boolean add(InetAddress address) throws CacheExpiredException {
        boolean out = false;

        if (this.isExpired) {
            throw new CacheExpiredException("Cache already expired!");
        }

        try {
            writeLock.lock();
            out = this.inetAddressStack.offerLast(address);
            waitTillDataArrive.signalAll();

        } finally {
            writeLock.unlock();
        }
        return out;
    }


    /**
     * remove() method will return true if the address was successfully removed
     *
     * @param address
     * @return
     */
    public boolean remove(InetAddress address) throws CacheExpiredException {

        boolean result;

        if (this.isExpired) {
            throw new CacheExpiredException("Cache already expired!");
        }

        try {
            writeLock.lock();
            result = this.inetAddressStack.remove(address);
        } finally {
            writeLock.unlock();
        }
        return result;
    }

    /**
     * The peek() method will return the most recently added element,
     * null if no element exists.
     *
     * @return
     */
    public InetAddress peek() throws CacheExpiredException {

        InetAddress found = null;

        if (this.isExpired) {
            throw new CacheExpiredException("Cache already expired!");
        }

        try {
            readLock.tryLock();
            found = inetAddressStack.peekLast();
        } finally {
            readLock.unlock();
        }
        return found;
    }

    /**
     * take() method retrieves and removes the most recently added element
     * from the cache and waits if necessary until an element becomes available.
     *
     * @return
     */
    public InetAddress take() throws CacheExpiredException {

        InetAddress found = null;
        if (this.isExpired) {
            throw new CacheExpiredException("Cache already expired!");
        }

        try {
            writeLock.lock();
            while (this.inetAddressStack.isEmpty() && !this.isExpired()) {
                waitTillDataArrive.await(this.age, this.unit);
            }
            found = this.inetAddressStack.pollLast();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return found;
    }


    /**
     * Method to check if the cache is already expired.
     *
     * @return
     */
    public boolean isExpired() {
        return isExpired;
    }


    /**
     * Method to send the final signal to waiting consumers(take()) before purging the cache.
     */
    private void finalSignalBeforePurgingCache() {
        writeLock.lock();
        waitTillDataArrive.signalAll();
        writeLock.unlock();
    }


    /**
     * Printing the cache elements
     */
    public void printCache() {
        readLock.lock();
        System.out.println("Cache:::  " + this.inetAddressStack);
        readLock.unlock();
    }

    /**
     * Clearing the cache manually
     */
    public void clear() {
        writeLock.lock();
        this.inetAddressStack.clear();
        writeLock.unlock();
    }

    /**
     * Check if the given element is available in Cache.
     * @param ele
     * @return
     */
    public boolean contains(InetAddress ele) {
        readLock.lock();
        boolean result = this.inetAddressStack.contains(ele);
        readLock.unlock();
        return result;
    }


}
