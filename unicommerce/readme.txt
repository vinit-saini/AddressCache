
------------------------------------------------
Its a java implementation with gradle
java 1.8 compatible
------------------------------------------------

Directory structure is normal java structure

------------
Source files
------------
src/main/java/com/unicommerce/cache
    AddressCache.java (implementation of the given stub)
    ExpirableCacheStack.java (just another implementation of similar cache with Integer datatype instead of inetAddress, made to ease the testing)


src/main/java/com/unicommerce/cache/exception
    CacheExpiredException.java (custom exception class)


src/main/java/com/unicommerce/cache/runner
    Tester.java (Driver class (main) to run the logic & testing)
                 * This class instantiates multiple threads to test the various multi-threading scenarios. like
                 * parallel insertion of data into the cache by two add threads when other set of threads are busy
                 * in concurrent read/pop operations and in delete operation."

------------
JUNIT TESTS
------------

src/test/java/com/unicommerce/cache
    AddressCacheTest.java (Junit TCs for the AddressCache class)
    ExpirableCacheStackTest.java (Junit TCs for the ExpirableCacheStack class)



