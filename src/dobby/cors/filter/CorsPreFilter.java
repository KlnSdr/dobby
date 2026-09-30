package dobby.cors.filter;

import common.inject.api.Inject;
import common.inject.api.RegisterFor;
import common.logger.Logger;
import dobby.IConfig;
import dobby.filter.Filter;
import dobby.filter.FilterOrder;
import dobby.filter.FilterType;
import dobby.io.HttpContext;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.io.response.ResponseCodes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RegisterFor(CorsPreFilter.class)
public class CorsPreFilter implements Filter {
    private static final Logger LOGGER = new Logger(CorsPreFilter.class);

    private static final String ENABLED_KEY = "dobby.cors.enabled";
    private static final String ALLOWED_ORIGINS_KEY = "dobby.cors.allowedOrigins";
    private static final String ALLOWED_METHODS_KEY = "dobby.cors.allowedMethods";
    private static final String ALLOWED_HEADERS_KEY = "dobby.cors.allowedHeaders";
    private static final String EXPOSED_HEADERS_KEY = "dobby.cors.exposedHeaders";
    private static final String ALLOW_CREDENTIALS_KEY = "dobby.cors.allowCredentials";
    private static final String MAX_AGE_KEY = "dobby.cors.maxAge";

    private static final String WILDCARD = "*";
    private static final String DEFAULT_ALLOWED_ORIGINS = WILDCARD;
    private static final String DEFAULT_ALLOWED_METHODS = "GET,POST,PUT,DELETE,OPTIONS";
    private static final String DEFAULT_ALLOWED_HEADERS = WILDCARD;
    private static final int DEFAULT_MAX_AGE = 3600;

    private final IConfig config;

    @Inject
    public CorsPreFilter(IConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return "cors";
    }

    @Override
    public FilterType getType() {
        return FilterType.PRE;
    }

    @Override
    public int getOrder() {
        return FilterOrder.CORS_PRE_FILTER.getOrder();
    }

    @Override
    public boolean run(HttpContext ctx) {
        if (!config.getBoolean(ENABLED_KEY)) {
            return true;
        }

        final Request req = ctx.getRequest();
        final Response res = ctx.getResponse();

        final String origin = req.getHeader("Origin");
        if (origin == null) {
            return true; // same-origin/non-browser request, nothing to do
        }

        if (!isOriginAllowed(origin)) {
            LOGGER.debug("cross-origin request from disallowed origin, not adding CORS headers: " + origin);
            return true; // let the request continue normally; the browser will reject the response client-side
        }

        applyOriginHeaders(res, origin);

        // a CORS preflight is an OPTIONS request carrying this header; we don't need to look at the
        // request's parsed RequestTypes (which has no OPTIONS entry) to recognize one
        final boolean isPreflight = req.getHeader("Access-Control-Request-Method") != null;
        if (!isPreflight) {
            return true; // actual (simple or already preflighted) request, let it hit the route handler
        }

        applyPreflightHeaders(req, res);
        res.setCode(ResponseCodes.NO_CONTENT);
        return false; // short-circuits the pre-filter chain; FilterManager sends this response as-is
    }

    private boolean isOriginAllowed(String origin) {
        final List<String> allowedOrigins = getAllowedOrigins();
        return allowedOrigins.contains(WILDCARD) || allowedOrigins.stream().anyMatch(o -> o.equalsIgnoreCase(origin));
    }

    private void applyOriginHeaders(Response res, String origin) {
        final boolean allowCredentials = config.getBoolean(ALLOW_CREDENTIALS_KEY);
        final List<String> allowedOrigins = getAllowedOrigins();

        if (allowedOrigins.contains(WILDCARD) && !allowCredentials) {
            res.setHeader("Access-Control-Allow-Origin", WILDCARD);
        } else {
            // wildcard origins can't be combined with credentials per spec, and echoing the
            // actual origin is strictly more correct anyway once an explicit allow list is in play
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Vary", "Origin");
        }

        if (allowCredentials) {
            res.setHeader("Access-Control-Allow-Credentials", "true");
        }

        final String exposedHeaders = config.getString(EXPOSED_HEADERS_KEY, "");
        if (!exposedHeaders.isBlank()) {
            res.setHeader("Access-Control-Expose-Headers", exposedHeaders);
        }
    }

    private void applyPreflightHeaders(Request req, Response res) {
        res.setHeader("Access-Control-Allow-Methods", config.getString(ALLOWED_METHODS_KEY, DEFAULT_ALLOWED_METHODS));

        final String allowedHeadersConfig = config.getString(ALLOWED_HEADERS_KEY, DEFAULT_ALLOWED_HEADERS);
        if (allowedHeadersConfig.trim().equals(WILDCARD)) {
            final String requestedHeaders = req.getHeader("Access-Control-Request-Headers");
            res.setHeader("Access-Control-Allow-Headers", requestedHeaders != null ? requestedHeaders : WILDCARD);
        } else {
            res.setHeader("Access-Control-Allow-Headers", allowedHeadersConfig);
        }

        res.setHeader("Access-Control-Max-Age", String.valueOf(config.getInt(MAX_AGE_KEY, DEFAULT_MAX_AGE)));
    }

    private List<String> getAllowedOrigins() {
        return splitConfigList(config.getString(ALLOWED_ORIGINS_KEY, DEFAULT_ALLOWED_ORIGINS));
    }

    private List<String> splitConfigList(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
