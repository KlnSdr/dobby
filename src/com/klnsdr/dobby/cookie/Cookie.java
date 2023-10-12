package com.klnsdr.dobby.cookie;

/**
 * Cookie class.
 * <p>
 * This class is used to create a cookie.
 * <br>
 * The default path is "/".
 * <br>
 * The default maxAge is 30 days.
 * <br>
 * The default secure is true.
 * <br>
 * The default httpOnly is true.
 * <br>
 * The default expires is null.
 * <br>
 * You can use setPath(), setMaxAge(), setSecure(), setHttpOnly(), setExpires() to change the default value.
 * <br>
 * You can use toString() to get the cookie string.
 * <br>
 * Example:
 * <pre>
 *         Cookie cookie = new Cookie("name", "value");
 *         cookie.setPath("/path");
 *         cookie.setMaxAge(60 * 60);
 *         cookie.setSecure(false);
 *         cookie.setHttpOnly(false);
 *         cookie.setExpires("Thu, 01 Jan 1970 00:00:00 GMT");
 *         System.out.println(cookie.toString());
 *         // name=value; Path=/path; Max-Age=3600; Expires=Thu, 01 Jan 1970 00:00:00 GMT
 *         // If you want to set the cookie to the response header, you can use:
 *         response.setHeader("Set-Cookie", cookie.toString());
 *     </pre>
 * <br>
 * If you want to get the cookie from the request header, you can use:
 */
public class Cookie {
    private final String name;
    private final String value;
    private boolean secure = true;
    private boolean httpOnly = true;
    private String path = "/";
    private int maxAge = 30 * 24 * 60 * 60;
    private String expires;

    /**
     * Create a cookie with default path, maxAge, secure and httpOnly.
     *
     * @param name  cookie name
     * @param value cookie value
     */
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if (path != null) {
            this.path = path.toLowerCase();
        } else {
            this.path = null;
        }
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value);
        if (path != null) {
            sb.append("; Path=").append(path);
        }
        if (maxAge > 0) {
            sb.append("; Max-Age=").append(maxAge);
        }
        if (expires != null) {
            sb.append("; Expires=").append(expires);
        }
        if (secure) {
            sb.append("; Secure");
        }
        if (httpOnly) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }
}
