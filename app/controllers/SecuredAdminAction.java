package controllers;

import models.User;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Authentication for actions that should only be executed for Admin users.
 */
public class SecuredAdminAction extends Action.Simple {

    @Override
    public Promise<Result> call(Context ctx) throws Throwable {
        String email = ctx.session().get("email");
        
        if (email == null) {
            return Promise.pure(redirect(routes.Application.login()));
        }
        
        User user = User.find.byId(email);
        if (user != null && user.isAdmin) {
            ctx.request().setUsername(email);
            return delegate.call(ctx);
        } else {
            return Promise.pure(redirect(routes.Application.index()));
        }
    }

}
