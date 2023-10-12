# DOBBY

---
*master has given com.klnsdr.dobby a sock(et)*

Dobby is a simple zero-dependency web server implementation based on the java socket server module. It is intended 
to be used for testing purposes only.

## Usage
- include the jar in your project
- add routes and filters
- create a new instance of the server

```java
import com.klnsdr.dobby.Dobby;

class MyServer {
    public static void main(String[] args) {
        Dobby server = Dobby.startApplication(MyServer.class);
    }
}
```

Routes and Filters are loaded automatically on the first call to `Server.getInstance()` from your project.

## Routes
Routes are Methods annotated with `@Get`, `@Post`, `@Put`, and `@Delete` annotations defined in `com.klnsdr.dobby.annotations`.
These annotations can be used on any method that takes a single `HttpContext` parameter and returns `void`. The method will be called
when the server receives a request matching the annotation's path. All annotations are loaded automatically on the 
first call to `Dobby.startApplication()`. No manual registration is required.

```java
import com.klnsdr.dobby.io.HttpContext;

public class MyRoutes {
    @Get("/hello")
    public void hello(HttpContext ctx) {
        ctx.getResponse().setBody("Hello World");
    }
}
```

## Filters
Filters can be used to analyze and modify the request and response objects before and after the request is handled 
by the server. All filters implement to `Filter` interface defined in `com.klnsdr.dobby.filter`. Filters are discovered on 
server start up and don't need to be registered manually.

```java
// filter.java

import com.klnsdr.dobby.filter.Filter;
import com.klnsdr.dobby.filter.FilterType;
import com.klnsdr.dobby.io.HttpContext;
import com.klnsdr.dobby.util.logging.Logger;

public class TestPreFilter implements Filter {
    private final Logger LOGGER = new Logger(TestPreFilter.class);

    @Override
    public String getName() {
        return "Test pre filter";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(HttpContext ctx) {
        LOGGER.debug("Test pre filter");
        LOGGER.debug(ctx.getRequest().getPath());

        ctx.getRequest().setHeader("X-Test-Pre-Filter", "Test pre filter");
    }
}

```
