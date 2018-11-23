package com.unicommerce.cache;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Test class which contains various TCs around the functionality of ExpirableCacheStackTest.
 * TC`s names are self-explanatory.
 *
 * Created by vsaini on 11/19/18.
 */
public class ExpirableCacheStackTest {

    private ExpirableCacheStack expirableCacheStack;
    private long age = 10;
    private TimeUnit unit = TimeUnit.SECONDS;

    @Before
    public void setUp() throws Exception {
        expirableCacheStack = new ExpirableCacheStack(age, unit);
    }

    @After
    public void tearDown() throws Exception {
        expirableCacheStack.clear();
    }

    @Test
    public void checkCacheIsNotExpired() throws Exception {
        boolean isExpired = expirableCacheStack.isExpired();
        Assert.assertFalse(isExpired);
    }

    @Test
    public void addSingleElementToCache() throws Exception {
        boolean result = expirableCacheStack.add(2);
        Assert.assertTrue(result);
    }

    @Test
    public void removeElementFromCache() throws Exception {
        expirableCacheStack.add(23);
        boolean result = expirableCacheStack.remove(23);
        Assert.assertTrue(result);
    }

    @Test
    public void checkRemovedElementInCache() throws Exception {
        expirableCacheStack.add(23);
        expirableCacheStack.remove(23);
        boolean result = expirableCacheStack.contains(23);
        Assert.assertFalse(result);

    }

    @Test
    public void peekLatestElement() throws Exception {
        expirableCacheStack.add(50);
        expirableCacheStack.add(51);
        expirableCacheStack.add(52);
        Integer result = expirableCacheStack.peek();
        Assert.assertEquals(52, result.intValue());
    }

    @Test
    public void peekThenCheckForElement() throws Exception {
        expirableCacheStack.add(50);
        expirableCacheStack.add(51);
        expirableCacheStack.add(52);
        Integer result = expirableCacheStack.peek();
        boolean isPresent = expirableCacheStack.contains(result);
        Assert.assertEquals(52, result.intValue());
        Assert.assertTrue(isPresent);
    }

    @Test
    public void takeLatestElement() throws Exception {
        expirableCacheStack.add(50);
        expirableCacheStack.add(51);
        expirableCacheStack.add(52);
        Integer result = expirableCacheStack.take();
        Assert.assertEquals(52, result.intValue());
    }

    @Test
    public void takeThenCheckForElement() throws Exception {
        expirableCacheStack.add(50);
        expirableCacheStack.add(51);
        expirableCacheStack.add(52);
        Integer result = expirableCacheStack.take();
        boolean isPresent = expirableCacheStack.contains(result);
        Assert.assertEquals(52, result.intValue());
        Assert.assertFalse(isPresent);
    }

    @Test
    public void checkCacheIsExpiredAfterAgeElapsed() throws Exception {
        // adding delay in execution to let cache expire.
        _delayer(15000);
        boolean isExpired = expirableCacheStack.isExpired();
        System.out.printf("isExpired" + isExpired);
        Assert.assertTrue(isExpired);
    }

    private void _delayer(long total_delay_in_millis) {
        try {
            Thread.sleep(total_delay_in_millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}