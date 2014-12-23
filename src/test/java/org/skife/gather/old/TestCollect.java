package org.skife.gather.old;

import junit.framework.TestCase;
import org.skife.gather.Dog;
import org.skife.gather.Priority;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class TestCollect extends TestCase
{
    public void testUnguardedGather() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void best(@Collect Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            box.provide(new Dog("Bean"));
            box.provide(new Dog("Bouncer"));
            latch.countDown();
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(2, flag.get());
    }

    public void testGuardWithGuardMethod() throws Exception
    {
        final AtomicInteger flag = new AtomicInteger(0);
        final TimeBox box = new TimeBox(new Object()
        {
            @Priority(3)
            public void collectPuppies(@Collect @GuardMethod("isPuppy") Collection<Dog> dogs)
            {
                flag.set(dogs.size());
            }

            public Boolean isPuppy(Dog dog)
            {
                return dog.getAge() < 2;
            }

        });

        final CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            box.provide(new Dog("Bean", 14));
            box.provide(new Dog("Mac", 1));
            latch.countDown();
        }).start();
        latch.await();

        assertTrue(box.react(100, TimeUnit.MILLISECONDS));
        assertEquals(1, flag.get());
    }
}
