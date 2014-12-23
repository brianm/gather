package org.skife.gather;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Multimaps.newSetMultimap;

public class Gather<T>
{
    private final SettableFuture<T> result = SettableFuture.create();
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final Set<Value> values = Sets.newConcurrentHashSet();
    private final Object lock = new Object();

    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final Executor executor;

    private final Set<Candidate<T>> topCandidates;
    private final List<Collection<Candidate<T>>> otherCandidates;

    public Gather(final Class<T> resultType,
                  final ScheduledExecutorService scheduler,
                  final Duration timeout,
                  final Executor executor,
                  final Object target)
    {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.executor = executor;

        SetMultimap<Integer, Candidate<T>> levels = newSetMultimap(newTreeMap(Comparator.<Integer>reverseOrder()),
                                                                   Sets::newHashSet);
        Arrays.stream(target.getClass().getMethods())
              .filter((m) -> m.isAnnotationPresent(Priority.class))
              .forEach((m) -> {
                  Candidate c = new Candidate(resultType, target, m);
                  levels.put(c.getPriority(), c);
              });
        Preconditions.checkArgument(!levels.isEmpty(), "No candidate methods found!");
        Iterator<Map.Entry<Integer, Collection<Candidate<T>>>> itty = levels.asMap().entrySet().iterator();
        Map.Entry<Integer, Collection<Candidate<T>>> first = itty.next();
        topCandidates = ImmutableSet.copyOf(first.getValue());
        otherCandidates = ImmutableList.copyOf(Iterators.transform(itty, Map.Entry::getValue));
    }


    public ListenableFuture<T> start()
    {
        if (!started.getAndSet(true)) {
            scheduler.schedule(this::timeout, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return result;
    }

    public void provide(final Object value)
    {
        provide(null, value);
    }

    public void provide(final String name, final Object value)
    {
        executor.execute(() -> {
            synchronized (lock) {
                __newValue(Optional.ofNullable(name), value);
                __evaluateCandidates(Consider.TOP);
            }
        });
    }

    private void timeout()
    {
        executor.execute(() -> {
            synchronized (lock) {
                __evaluateCandidates(Consider.ALL);
                if (!finished.get()) {
                    __finish(new TimeoutException("No candidates matched in time"), null);
                }
            }
        });
    }

    /**
     * Calls to this must be made with lock
     */
    private void __newValue(Optional<String> name, Object value)
    {
        if (!finished.get()) {
            values.add(new Value(name, value));
        }
    }

    private void __evaluateCandidates(Consider consider)
    {
        if (!finished.get()) {
            // always consider top candidates
            for (Candidate<T> candidate : topCandidates) {
                if (candidate.isSatisfiedBy(values)) {
                    T r = candidate.invoke(values);
                    __finish(null, r);
                    return;
                }
            }
            if (consider == Consider.ALL) {
                for (Collection<Candidate<T>> level : otherCandidates) {
                    for (Candidate<T> candidate : level) {
                        if (candidate.isSatisfiedBy(values)) {
                            T r = candidate.invoke(values);
                            __finish(null, r);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void __finish(Exception e, T value)
    {
        if (e != null) {
            result.setException(e);
        }
        else {
            result.set(value);
        }
        finished.set(true);
    }

    private static enum Consider
    {
        TOP, ALL
    }
}
