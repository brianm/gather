package org.skife.timebox;

import java.util.function.Function;

public @interface SuperGuard
{
    Function<?, ?> value();
}
