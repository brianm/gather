package org.skife.gather;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gather<T>
{
    private final SettableFuture<T> result = SettableFuture.create();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Object lock = new Object();

    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final Executor executor;

    private final Set<Candidate> topCandidates;
    private final List<Set<Candidate>> otherCandidates;

    public Gather(final Class<T> resultType,
                  final ScheduledExecutorService scheduler,
                  final Duration timeout,
                  final Executor executor,
                  final Object target)
    {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.executor = executor;

        // extract candidates
        for (Method method : target.getClass().getMethods()) {
            if (method.isAnnotationPresent(Priority.class)) {
                if (void.class != method.getReturnType() && !resultType.isAssignableFrom(method.getReturnType())) {
                    throw new IllegalArgumentException(String.format("Method %s is annotated with @Priority " +
                                                                     "but does not return %s or void",
                                                                     method.getName(),
                                                                     resultType.getName()));
                }
            }
        }
    }

    public ListenableFuture<T> start()
    {
        if (!running.getAndSet(true)) {
            scheduler.schedule(this::timeout, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return result;
    }

    private void timeout()
    {
        // test top candidates

        // test other candidates

        // fallback to timeout exception
    }

    private void considerNewValue(String name, Object value)
    {
        synchronized (lock) {
            // only evaluate if still running
            if (running.get()) {

            }
        }
    }

    public void provide(final Object value)
    {
        provide(null, value);
    }

    public void provide(final String name, final Object value)
    {
        executor.execute(() -> considerNewValue(name, value));
    }
}
