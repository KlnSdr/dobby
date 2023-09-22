package dobby.filter.defaultFilter.post;

import dobby.Session;
import dobby.filter.post.PostFilter;
import dobby.io.response.Response;
import dobby.session.SessionService;

public class SessionPostFilter implements PostFilter {
    private final SessionService sessionService = SessionService.getInstance();

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void run(Response in) {
        Session session = in.getRequest().getSession();

        if (session.getId() == null) {
            return;
        }

        sessionService.set(session);
        in.setCookie("DOBBY_SESSION", session.getId(), "/");
    }
}
