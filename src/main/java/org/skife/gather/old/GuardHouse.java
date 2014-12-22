package org.skife.gather.old;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public interface GuardHouse
{
    Predicate<Object[]> buildMethodPredicate(Annotation a, Object handler, Method m);
    Predicate<Object> buildArgumentPredicate(Annotation a, Object handler, Method m, int argumentIndex);
    Predicate<Object> buildGatherPredicate(Annotation a, Object handler, Method m, Class expectedType, int argumentIndex);
}
