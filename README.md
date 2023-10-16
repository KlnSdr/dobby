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
import dobby.Dobby;

class MyServer {
    public static void main(String[] args) {
        Dobby server = Dobby.startApplication(MyServer.class);
    }
}
```

Routes and Filters are loaded automatically when calling `Dobby.startApplication(...)`.

## Routes
Routes are Methods annotated with `@Get`, `@Post`, `@Put`, and `@Delete` annotations defined in `dobby.annotations`.
These annotations can be used on any method that takes a single `HttpContext` parameter and returns `void`. The method will be called
when the server receives a request matching the annotation's path. All annotations are loaded automatically on the 
first call to `Dobby.startApplication()`. No manual registration is required.
```java
import dobby.io.HttpContext;

public class MyRoutes {
    @Get("/hello")
    public void hello(HttpContext ctx) {
        ctx.getResponse().setBody("Hello World");
    }
}
```

## Filters
Filters can be used to analyze and modify the request and response objects before and after the request is handled 
by the server. All filters implement to `Filter` interface defined in `dobby.filter`. Filters are discovered on 
server start up and don't need to be registered manually.

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
## Configuration
Dobby can be configured using a `application.json` file in the resource folder. The following properties are 
available:
- `dobby.port`: The port the server will listen on. Default: `3000`
- `dobby.threads`: The number of threads the server will use to handle requests. Default: `10`
- `dobby.disableFilters`: Disables all filters. Default: `false`
- `dobby.staticContentDir`: The directory relative to the resource folder to serve static content from. Default: `./`
- `dobby.disabelStaticContent`: Disables serving static content. Default: `false`
- `application.name`: The name of the application
- `application.version`: The version of the application
The configuration is available from everywhere in the application using `Config.getInstance().getString(<key>)`, 
  `Config.getInstance().getInt(<key>)`,`Config.getInstance().getBoolean(<key>)` and `Config.getInstance().getJson
  (<key>)`. It is suggested to put application specific configuration under `application.data`.
