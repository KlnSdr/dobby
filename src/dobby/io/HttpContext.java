package dobby.io;

import dobby.cookie.Cookie;
import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.session.ISession;
import dobby.session.Session;

/**
 * The HttpContext class is used to pass information between filters and handlers
 */
public class HttpContext {
    private Request request;
    private Response response;
    private ISession session;

    public HttpContext() {
    }

    /**
     * Get the request object
     *
     * @return Request object
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Set the request object
     *
     * @param request Request object
     */
    public void setRequest(Request request) {
        this.request = request;
    }

    /**
     * Get the response object
     *
     * @return Response object
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Set the response object
     *
     * @param response Response object
     */
    public void setResponse(Response response) {
        this.response = response;
    }

    /**
     * Get the session object
     *
     * @return Session object
     */
    public ISession getSession() {
        return session;
    }

    /**
     * Set the session object
     *
     * @param session Session object
     */
    public void setSession(ISession session) {
        this.session = session;
    }

    /**
     * Destroy the session
     */
    public void destroySession() {
        session.destroy();

        Cookie cookie = new Cookie("DOBBY_SESSION", "");
        cookie.setMaxAge(-1);
        response.setCookie("DOBBY_SESSION", cookie);
    }
}
