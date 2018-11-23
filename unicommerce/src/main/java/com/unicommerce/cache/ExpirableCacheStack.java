package com.unicommerce.cache;

import com.unicommerce.cache.exception.CacheExpiredException;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is just another implementation as AddressCache with.
 * Integer objects in cache. Purpose was the ease of testing while implementing.
 * Nothing special.. ;)
 *
 * Created by vsaini on 11/19/18.
 */
public class ExpirableCacheStack {

    private long age;
    private TimeUnit unit;
    private long timeToLive;
    private ArrayDeque<Integer> inetAddressStack;
    private Thread purgeThread;
    private boolean isExpired = false;

    private ReentrantReadWriteLock.WriteLock writeLock = new ReentrantReadWriteLock().writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = new ReentrantReadWriteLock().readLock();
    private Condition waitTillDataArrive = writeLock.newCondition();

    public ExpirableCacheStack(final long age, final TimeUnit unit) {

        this.age = age;
        this.unit = unit;
        this.timeToLive = unit.toMillis(age);

        inetAddressStack = new ArrayDeque<Integer>();

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


    public boolean isExpired() {
        return isExpired;
    }


    private void finalSignalBeforePurgingCache() {
        writeLock.lock();
        waitTillDataArrive.signalAll();
        writeLock.unlock();
    }


    /**
     * add() method must store unique elements only (existing elements must be ignored).
     * This will return true if the element was successfully added.
     *
     * @param address
     * @return
     */
    public boolean add(Integer address) throws CacheExpiredException {
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
    public boolean remove(Integer address) throws CacheExpiredException {
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
    public Integer peek() throws CacheExpiredException {
        Integer found = null;

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
    public Integer take() throws CacheExpiredException {
        Integer found = null;

        if (this.isExpired) {
            throw new CacheExpiredException("Cache already expired!");
        }

        try {
            writeLock.lock();
            while (this.inetAddressStack.isEmpty() && !this.isExpired()) {
                System.out.println("Waiting on condition");
                waitTillDataArrive.await(this.age, this.unit);
                System.out.println("signalled, out from wait");
            }
            found = this.inetAddressStack.pollLast();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            writeLock.unlock();
        }
        return found;
    }

    public void printCache() {
        readLock.lock();
        System.out.println("Cache:::  " + this.inetAddressStack);
        readLock.unlock();
    }

    public void clear() {
        writeLock.lock();
        this.inetAddressStack.clear();
        writeLock.unlock();
    }

    public boolean contains(Integer ele) {
        readLock.lock();
        boolean result = this.inetAddressStack.contains(ele);
        readLock.unlock();
        return result;
    }

}
