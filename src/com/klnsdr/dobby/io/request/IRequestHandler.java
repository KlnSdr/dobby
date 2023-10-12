package com.klnsdr.dobby.io.request;

import com.klnsdr.dobby.io.HttpContext;

import java.lang.reflect.InvocationTargetException;

public interface IRequestHandler {
    void handle(HttpContext context) throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException;
}
