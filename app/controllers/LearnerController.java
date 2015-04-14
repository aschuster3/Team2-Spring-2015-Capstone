package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Learner;
import models.Session;
import models.User;
import play.data.Form;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.List;


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

    /**
     * NOTE: This method can change the learner's email,
     * even though the Learner schema uses it as an ID!
     *
     * Changes will be propogated to the learner's sessions.
     *
     * On success, returns the entire Learner entity as JSON.
     */
    @BodyParser.Of(BodyParser.Json.class)
    public static Result updateLearner(String learnerUUID) {
        JsonNode learnerJson = request().body().asJson();
        Learner updatedLearnerData = Json.fromJson(learnerJson, Learner.class);
        Learner existingLearner = Learner.find.where().eq("uuid", learnerUUID).findUnique();

        if (existingLearner == null) {
            return badRequest("Learner ID does not exist");
        }

        // we don't allow changing ownership of a learner
        //
        // we don't allow changing uuid of a learner
        //
        // (also, we don't currently pass these as JSON)
        updatedLearnerData.ownerEmail = existingLearner.ownerEmail;
        updatedLearnerData.uuid = learnerUUID;

        String errorMessage = updatedLearnerData.validate();
        if (errorMessage != null) {
            return badRequest(errorMessage);
        }

        String learnerOriginalEmail = existingLearner.email;
        if (updatedLearnerData.email.equals(learnerOriginalEmail)) {
            updatedLearnerData.update();
            return ok(Json.toJson(Learner.find.byId(updatedLearnerData.email)));
        }

        existingLearner.delete();
        updatedLearnerData.save();
        List<Session> schedule = Session.getLearnerSchedule(learnerOriginalEmail);
        for (Session session: schedule) {
            session.assignedLearner = updatedLearnerData.email;
            session.save();
        }
        return ok(Json.toJson(Learner.find.byId(updatedLearnerData.email)));
    }

    private static boolean learnerEmailUnchanged(String updatedEmail, String originalEmail) {
        return updatedEmail == null || updatedEmail.equals(originalEmail);
    }

    /**
     * This does not perform a cascading delete on sessions.
     * Instead, it frees the learner's sessions.
     */
    public static Result ajaxDeleteLearner(String learnerUUID) {
        Learner learnerToDelete = Learner.find.where().eq("uuid", learnerUUID).findUnique();
        if (learnerToDelete != null) {
            Learner.deleteLearnerButFreeTheirSessions(learnerToDelete);
        }
        return status(NO_CONTENT);
    }
}