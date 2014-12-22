package org.skife.gather.old;

public interface Factory
{
    public <T> T instantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException;
}
