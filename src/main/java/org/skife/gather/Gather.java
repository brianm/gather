package org.skife.gather;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Gather<T>
{
    public Gather(final Class<T> resultType,
                  final Executor es,
                  final int timeout,
                  final TimeUnit timeOutUnit,
                  final Object target)
    {


    }

    public static Gather<String> with(final Object target)
    {
        return new Gather<String>(target);
    }

    public Future<T> start()
    {
        throw new UnsupportedOperationException("Not Yet Implemented!");
    }

    public void provide(final Object value)
    {


    }

    public void provide(final String name, final Object value)
    {


    }
}
