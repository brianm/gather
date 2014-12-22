package org.skife.timebox;

import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTimeBox extends TestCase
{

    public void testFirstChoice() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            box.provide(new Dog());
            box.provide(new Cat());
            latch.countDown();
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testSecondChoice() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog());
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testFallback() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(Dog dog, Cat cat)
            {
                flag.set(1);
            }

            @Priority(0)
            public void fallback()
            {
                flag.set(4);
            }
        });

        assertTrue(box.react(1, TimeUnit.NANOSECONDS));
        assertEquals(4, flag.get());
    }

    public void testMultipleProvides() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(dog.getName().equals("Bean") ? 1 : 2);
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable()
        {
            public void run()
            {
                box.provide(new Dog("Bouncer"), 1);
                box.provide(new Dog("Bean"), 10);
                latch.countDown();
            }
        }).start();
        latch.await();

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }

    public void testMinimumAuthority() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Authority(10) Dog dog)
            {
                flag.set(1);
            }


            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer"), 1);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testMinimumAuthorityMet() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Authority(10) Dog dog)
            {
                flag.set(1);
            }

            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        box.provide(new Dog("Bouncer"), 10);

        assertTrue(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }


    public void testBehaviorOnNoFallback() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(2)
            public void okay(Dog dog)
            {
                flag.set(2);
            }
        });

        box.provide(new Cat());

        // should timeout without matching, no fallback
        assertFalse(box.react(10, TimeUnit.MILLISECONDS));
        assertEquals(0, flag.get());
    }
}
