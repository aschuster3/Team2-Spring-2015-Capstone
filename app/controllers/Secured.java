package controllers;

import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security.Authenticator;

import java.util.Date;

public class Secured extends Authenticator {

    private static final String TIMESTAMP_KEY = "timestamp";

    public static final long TIMEOUT_MINUTES = 10;
    public static final long TIMEOUT_MILLIS = TIMEOUT_MINUTES * 60 * 1000;

    @Override
    public String getUsername(Context ctx) {
        Http.Session session = ctx.session();
        String email = session.get("email");
        String currTimestamp = Long.toString(new Date().getTime());

        if (email == null) {
            return null;
        } else if (sessionExpired(session.get(TIMESTAMP_KEY), currTimestamp)) {
            ctx.session().clear();
            return null;
        } else {
            session.put(TIMESTAMP_KEY, currTimestamp);
            return email;
        }
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.login());
    }

    public boolean sessionExpired(String prevTimestamp, String currTimestamp) {
        // user just logged in
        if (prevTimestamp == null || prevTimestamp.isEmpty()) {
            return false;
        }

        // error input
        if (currTimestamp == null || currTimestamp.isEmpty()) {
            return true;
        }

        long prev, curr;
        try {
            prev = Long.parseLong(prevTimestamp);
            curr = Long.parseLong(currTimestamp);
        } catch (NumberFormatException e) {
            return true;
        }

        return curr - prev >= TIMEOUT_MILLIS || curr - prev < 0;
    }
}
