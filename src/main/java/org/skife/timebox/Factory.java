package org.skife.timebox;

public interface Factory
{
    public <T> T instantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException;
}
