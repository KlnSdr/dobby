package dobby.io;

import dobby.cookie.Cookie;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.session.Session;

/**
 * The HttpContext class is used to pass information between filters and handlers
 */
public class HttpContext {
    private Request request;
    private Response response;
    private Session session;

    public HttpContext() {
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void destroySession() {
        session.destroy();

        Cookie cookie = new Cookie("DOBBY_SESSION", "");
        cookie.setMaxAge(-1);
        response.setCookie("DOBBY_SESSION", cookie);
    }
}
