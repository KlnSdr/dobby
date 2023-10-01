# DOBBY

---
*master has given dobby a sock(et)*

Dobby is a simple zero-dependency web server implementation based on the java socket server module. It is intended 
to be used for testing purposes only.

## Usage
- include the jar in your project
- add routes and filters
- create a new instance of the server

```java
import dobby.Server;

class MyServer {
    public static void main(String[] args) {
        Server server = Server.getInstance();
    }
}
```

Routes and Filters are loaded automatically on the first call to `Server.getInstance()` from your project.

## Routes
Routes can be added using the `get`, `post`, `put`, and `delete` methods of the server instance.

```java
import dobby.Server;

class MyServer {
    public static void main(String[] args) {
        Server server = Server.getInstance();

        server.get("/hello", (ctx) -> ctx.getResponse().send("Hello World!"));
    }
}
```
However, it is way more convenient to use the `@Get`, `@Post`, `@Put`, and `@Delete` annotations defined in `dobby.annotations`.
These annotations can be used on any method that takes a single `HttpContext` parameter and returns `void`. The method will be called
when the server receives a request matching the annotation's path. All annotations are loaded automatically on the 
first call to `Server.getInstance()`. No manual registration is required.
```java
public class MyRoutes {
    @Get("/hello")
    public void hello(HttpContext ctx) {
        ctx.getResponse().send("Hello World!");
    }
}
```

## Filters
Filters can be added using the `addPostFilter` and `addPreFilter` methods of the server instance. They can be used 
to analyze and modify the request and response objects before and after the request is handled by the server.

```java
// filter.java
import dobby.filter.Filter;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.util.logging.Logger;

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
```java
// main.java
import dobby.Server;

class MyServer {
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.addPreFilter(new TestPreFilter());
    }
}
```
However filters are discovered automatically on server startup. No manual registration is required.
