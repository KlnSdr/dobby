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
These annotations can be used on any method that takes a single `HttpContext` parameter and returns `void`. The method
will be called
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
import common.logger.Logger;

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
- `dobby.staticContent.directory`: The directory relative to the resource folder to serve static content from. 
  Default: 
  `./`
- `dobby.staticContent.externalDocRoot`: A directory located on the computer to server static content from. 
- `dobby.staticContent.disable`: Disables serving static content. Default: `false`
- `dobby.staticContent.cleanUpInterval`: The interval in minutes in which the staticFile manager will cleanup old 
  files from the cache.
- `dobby.staticContent.maxFileAge`: The maximum time in hours a file is allowed to stay in the cache.
- `dobby.mode`: either `http` or `pure`. Default: `http`.
    - `http`: The server processes incoming requests as HTTP requests. Filters and RouteHandler are available. This is
      the default mode.
    - `pure`: The server accepts incoming requests but does not process them. The raw request stream is available in
      the handler. Filters and RouteHandler are NOT called.
- `dobby.session.cleanUpInterval`: The interval in minutes in which the session manager will clean up expired sessions.
  Default: `30`
- `dobby.session.maxAge`: The maximum age of a session in hours. Default: `24`
- `dobby.session.store`: The fully qualified class name of the session store implementation. The implementation MUST implement the `ISessionStore` interface. Default: 
  `dobby.session.DefaultSessionStore`
- `dobby.scheduler.disable`: Disables all schedulers. Default: `false`
- `application.name`: The name of the application
- `application.version`: The version of the application

The configuration is available from everywhere in the application using `Config.getInstance().getString(<key>)`,
`Config.getInstance().getInt(<key>)`,`Config.getInstance().getBoolean(<key>)` and `Config.getInstance().getJson
(<key>)`. It is suggested to put application specific configuration under `application.data`.

## Additional Information about pure mode

In pure mode, the server does not process incoming requests. Instead, the raw request stream is available in the
handler. Filters and RouteHandler are NOT called. Instead, the `onRequest` method of a class implementing
`PureRequestHandler` is called. Multiple classes implementing `PureRequestHandler` will cause the server to refuse
to start.

In this handler method it is not needed to close the socket connection manually. This is done automatically after
the handler method returns. However, writing to and flushing the output stream is required.
