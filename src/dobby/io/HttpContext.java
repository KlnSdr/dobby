package dobby.io;

import dobby.io.request.Request;
import dobby.io.response.Response;
import dobby.session.Session;

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
        response.setCookie("DOBBY_SESSION", "", -1);
    }
}
