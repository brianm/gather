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
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Multimaps.newSetMultimap;

public class Gather<T>
{
    private final SettableFuture<T> result = SettableFuture.create();
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Set<Value> values = Sets.newConcurrentHashSet();
    private final Object lock = new Object();

    private final ScheduledExecutorService scheduler;
    private final Duration timeout;
    private final Executor executor;

    private final Set<Candidate> topCandidates;
    private final List<Collection<Candidate>> otherCandidates;

    public Gather(final Class<T> resultType,
                  final ScheduledExecutorService scheduler,
                  final Duration timeout,
                  final Executor executor,
                  final Object target)
    {
        this.scheduler = scheduler;
        this.timeout = timeout;
        this.executor = executor;

        SetMultimap<Integer, Candidate> levels = newSetMultimap(newTreeMap(Comparator.<Integer>reverseOrder()),
                                                                Sets::newHashSet);
        Arrays.stream(target.getClass().getMethods())
              .filter((m) -> m.isAnnotationPresent(Priority.class))
              .forEach((m) -> {
                  Candidate c = new Candidate(resultType, m);
                  levels.put(c.getPriority(), c);
              });
        Preconditions.checkArgument(!levels.isEmpty(), "No candidate methods found!");
        Iterator<Map.Entry<Integer, Collection<Candidate>>> itty = levels.asMap().entrySet().iterator();
        Map.Entry<Integer, Collection<Candidate>> first = itty.next();
        topCandidates = ImmutableSet.copyOf(first.getValue());
        otherCandidates = ImmutableList.copyOf(Iterators.transform(itty, Map.Entry::getValue));
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
            values.add(new Value(name, value));
            // only evaluate if still running
            if (running.get()) {
                for (Candidate candidate : topCandidates) {
                    if (candidate.isSatisfied(values)) {
                        // finished!
                    }
                }
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
