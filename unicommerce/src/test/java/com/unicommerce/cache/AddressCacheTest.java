package com.unicommerce.cache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Test class which contains various TCs around the functionality of AddressCache.
 * TC`s names are self-explanatory.
 *
 * Created by vsaini on 11/19/18.
 */
public class AddressCacheTest {

    private AddressCache expirableCacheStack;
    private long age = 10;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Before
    public void setUp() throws Exception {
        expirableCacheStack = new AddressCache(age, unit);
    }

    @After
    public void tearDown() throws Exception {
        expirableCacheStack.printCache();
        expirableCacheStack.clear();
    }

    @Test
    public void checkCacheIsNotExpired() throws Exception {
        boolean isExpired = expirableCacheStack.isExpired();
        Assert.assertFalse(isExpired);
    }

    @Test
    public void addSingleElementToCache() throws Exception {
        boolean result = expirableCacheStack.add(getInetAddress());
        Assert.assertTrue(result);
    }

    @Test
    public void removeElementFromCache() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(address);
        final boolean result = expirableCacheStack.remove(address);
        Assert.assertTrue(result);
    }

    @Test
    public void checkRemovedElementInCache() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(address);
        expirableCacheStack.remove(address);
        boolean result = expirableCacheStack.contains(address);
        Assert.assertFalse(result);

    }

    @Test
    public void peekLatestElement() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(address);
        InetAddress result = expirableCacheStack.peek();
        Assert.assertEquals(address, result);
    }

    @Test
    public void peekThenCheckForElement() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(address);
        InetAddress result = expirableCacheStack.peek();
        boolean isPresent = expirableCacheStack.contains(result);
        Assert.assertEquals(address, result);
        Assert.assertTrue(isPresent);
    }

    @Test
    public void takeLatestElement() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(address);
        InetAddress result = expirableCacheStack.take();
        Assert.assertEquals(address, result);
    }

    @Test
    public void takeThenCheckForElement() throws Exception {
        final InetAddress address = getInetAddress();
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(getInetAddress());
        expirableCacheStack.add(address);
        InetAddress result = expirableCacheStack.take();
        boolean isPresent = expirableCacheStack.contains(result);
        Assert.assertEquals(address, result);
        Assert.assertFalse(isPresent);
    }

    @Test
    public void checkCacheIsExpiredAfterAgeElapsed() throws Exception {
        // adding delay in execution to let cache expire.
        _delayer(15000);
        boolean isExpired = expirableCacheStack.isExpired();
        Assert.assertTrue(isExpired);
    }

    /**
     * Just a supportive method to put delay in one TC.
     * required to test the expiration of cache.
     * @param total_delay_in_millis
     */
    private void _delayer(long total_delay_in_millis) {
        try {
            Thread.sleep(total_delay_in_millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * A utility kind of method to generate random ip-address on runtime.
     * @return
     */
    private InetAddress getInetAddress() {
        Random random = new Random();
        InetAddress inetAddress = null;
        String randomIp = random.nextInt(256)+"."+random.nextInt(256)+"."+
                random.nextInt(256)+"."+random.nextInt(256);
        try {
            inetAddress = InetAddress.getByName(randomIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println("inet : "+inetAddress.getHostAddress());
        return inetAddress;
    }

}