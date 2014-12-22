package org.skife.gather.old;

public class DefaultFactory implements Factory
{

    public <T> T instantiate(Class<T> clazz) throws InstantiationException, IllegalAccessException
    {
        return clazz.newInstance();
    }
}
