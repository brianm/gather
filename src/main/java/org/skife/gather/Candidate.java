package org.skife.gather;

import java.lang.reflect.Method;

class Candidate
{
    private final Method m;
    private Integer priority;

    public Candidate(final Class<?> type, final Method method)
    {
        validateCandidateMethodSignature(type, method);
        this.m = method;
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

    public Integer getPriority()
    {
        return priority;
    }
}
