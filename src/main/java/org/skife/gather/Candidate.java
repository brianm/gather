package org.skife.gather;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class Candidate<T>
{
    private final Method m;
    private final Integer priority;
    private final Object target;
    private final List<Binding> bindings;

    Candidate(final Class<T> type, Object target, final Method method)
    {
        validateCandidateMethodSignature(type, method);
        this.target = target;
        this.m = method;
        priority = m.getAnnotation(Priority.class).value();
        ImmutableList.Builder<Binding> b = ImmutableList.builder();
        for (int i = 0, c = method.getParameterCount(); i < c; i++) {
            b.add(new Binding(i));
        }
        bindings = b.build();
    }

    @Override
    public int hashCode()
    {
        return m.hashCode();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Candidate candidate = (Candidate) o;
        return m.equals(candidate.m);

    }

    private static void validateCandidateMethodSignature(final Class<?> resultType, final Method method)
    {
        if (void.class != method.getReturnType() && !resultType.isAssignableFrom(method.getReturnType())) {
            throw new IllegalArgumentException(String.format("Method %s is annotated with @Priority " +
                                                             "but does not return %s or void",
                                                             method.getName(),
                                                             resultType.getName()));
        }
    }

    Integer getPriority()
    {
        return priority;
    }

    boolean isSatisfiedBy(final Set<Value> values)
    {
        for (Binding binding : bindings) {
            if (!binding.bind(values).isPresent()) {
                return false;
            }
        }
        return true;
    }

    T invoke(final Set<Value> values)
    {
        List<Object> args = new ArrayList<>(bindings.size());
        for (Binding binding : bindings) {
            args.add(binding.bind(values).get());
        }
        try {
            return (T) m.invoke(target, args.toArray());
        }
        catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


    private class Binding
    {
        private final Class<?> paramType;

        Binding(int index)
        {
            paramType = m.getParameterTypes()[index];
        }

        Optional<Object> bind(Set<Value> values)
        {
            return values.stream()
                         .map(Value::getActual)
                         .filter((o) -> paramType.isAssignableFrom(o.getClass()))
                         .findFirst();
        }
    }
}
