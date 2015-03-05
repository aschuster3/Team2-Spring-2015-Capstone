package controllers;

import models.Learner;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Created by max on 3/5/15.
 */

@Security.Authenticated(Secured.class)
public class LearnerController extends Controller {

    public static Result getLearners() {
        String email = session().get("email");
        User user = User.find.byId(email);
        if (user.isAdmin) {
            return status(OK, Json.toJson(Learner.getAll()));
        } else {
            return status(OK, Json.toJson(Learner.getAllOwnedBy(email)));
        }
    }
}