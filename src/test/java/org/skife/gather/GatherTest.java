package org.skife.gather;

import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.skife.gather.old.Priority;

import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class GatherTest
{
    @Test
    public void testApi() throws Exception
    {
        ExecutorService es = MoreExecutors.newDirectExecutorService();
        Gather<String> g = new Gather(String.class, es, 1, TimeUnit.SECONDS, new Object()
        {
            @Priority(3)
            public String both(Cat _c, Dog _d)
            {
                return "woof meow";
            }

            @Priority(2)
            public String dog(@Named("Bean") Dog _d)
            {
                return "woof";
            }

            @Priority(1)
            public String cat(Cat _c)
            {
                return "meow";
            }
        });

        Future<String> f = g.start();
        g.provide("Bean", new Dog());
        g.provide(new Cat());

        assertThat(f.isDone()).describedAs("Future isDone").isTrue();
    }
}
