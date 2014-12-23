package org.skife.gather;

import java.util.Optional;

class Value
{
    private final Optional<String> name;
    private final Object actual;

    Value(Optional<String> name, Object actual) {
        this.name = name;
        this.actual = actual;
    }

    Optional<String> getName()
    {
        return name;
    }

    Object getActual()
    {
        return actual;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final Value value = (Value) o;

        if (!actual.equals(value.actual)) { return false; }
        if (!name.equals(value.name)) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + actual.hashCode();
        return result;
    }
}
