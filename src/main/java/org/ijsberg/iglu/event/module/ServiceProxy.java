package org.ijsberg.iglu.event.module;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ServiceProxy implements InvocationHandler {

    private Object implementation;

    public ServiceProxy(Object implementation) {
        this.implementation = implementation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(implementation, args);
    }
}
