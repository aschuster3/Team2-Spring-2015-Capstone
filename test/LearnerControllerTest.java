import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.routes;
import models.Session;
import models.User;
import models.Learner;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

import org.junit.*;
import play.libs.Json;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

public class LearnerControllerTest {

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    private static final String COORDINATOR_A_EMAIL = "coord@gmail.com";
    private static final String LEARNER_A_EMAIL = "learner@gmail.com";

    private static final String COORDINATOR_B_EMAIL = "coord2@gmail.com";
    private static final String LEARNER_B_EMAIL = "learnerb@gmail.com";

    @Before
    public void setup(){
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        User.create(new User("Admin", "User", ADMIN_EMAIL, "adminpassword", true));
        User.create(new User("Coordinator", "A", COORDINATOR_A_EMAIL, "password", false));
        User.create(new User("Coordinator", "B", COORDINATOR_B_EMAIL, "password", false));

        new Learner(LEARNER_A_EMAIL, "Learner", "A", COORDINATOR_A_EMAIL, "typeA").save();
        new Learner(LEARNER_B_EMAIL, "Learner", "B", COORDINATOR_B_EMAIL, "typeB").save();
    }

    @Test
    public void getLearners_asAdmin_ReturnsAllLearners() throws Exception {
        Result result = callAction(
                controllers.routes.ref.LearnerController.getLearners(),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        ObjectMapper mapper = new ObjectMapper();
        List<Learner> learners = mapper.readValue(contentAsString(result), new TypeReference<List<Learner>>() { });

        assertThat(learners.size()).isEqualTo(2);
    }

    @Test
    public void getLearners_asCoordinator_ReturnsAssignedLearners() throws Exception {
        Result result = callAction(
                controllers.routes.ref.LearnerController.getLearners(),
                fakeRequest().withSession("email", COORDINATOR_A_EMAIL)
        );

        ObjectMapper mapper = new ObjectMapper();
        List<Learner> learners = mapper.readValue(contentAsString(result), new TypeReference<List<Learner>>() { });

        assertThat(learners.size()).isEqualTo(1);
        assertThat(learners.get(0).email).isEqualTo(LEARNER_A_EMAIL);
    }

    @Test
    public void validationOfLearnerJSON_requiredIsNull_ReturnsErrorMessage() {
        Learner learnerWithoutFirstName =
                new Learner("extra@gmail.com", null, "last", COORDINATOR_A_EMAIL);

        String errorMessage = learnerWithoutFirstName.validate();
        assertThat(errorMessage).isNotNull();
    }

    @Test
    public void validationOfLearnerJSON_requiredIsEmpty_ReturnsErrorMessage() {
        Learner learnerWithoutFirstName =
                new Learner("extra@gmail.com", "", "last", COORDINATOR_A_EMAIL);

        String errorMessage = learnerWithoutFirstName.validate();
        assertThat(errorMessage).isNotNull();
    }

    @Test
    public void validationOfLearnerJSON_invalidEmail_ReturnsErrorMessage() {
        Learner learnerWithoutFirstName =
                new Learner("bad_email", "first", "last", COORDINATOR_A_EMAIL);

        String errorMessage = learnerWithoutFirstName.validate();
        assertThat(errorMessage).isNotNull();
    }

    @Test
    public void validationOfLearnerJSON_validLearner_ReturnsNull() {
        Learner learnerA = Learner.find.byId(LEARNER_A_EMAIL);

        String errorMessage = learnerA.validate();
        assertThat(errorMessage).isNull();
    }

    @Test
    public void updatingLearnerFieldSucceeds() {
        Learner learnerA = Learner.find.byId(LEARNER_A_EMAIL);
        learnerA.learnerType = "Some New Type";

        Result result = callAction(
                routes.ref.LearnerController.updateLearner(learnerA.uuid),
                fakeRequest()
                        .withSession("email", COORDINATOR_A_EMAIL)
                        .withJsonBody(Json.toJson(learnerA))
        );
        Learner updatedLearnerA = Learner.find.byId(LEARNER_A_EMAIL);

        assertThat(status(result)).isEqualTo(OK);
        assertThat(updatedLearnerA.learnerType).isEqualTo("Some New Type");
    }

    @Test
    public void updatingLearner_ChangingEmail_AlsoChangesAssignedSessionEmails() {
        Session session = new Session("IDa", "title", new Date(0));
        Learner learner = Learner.find.byId(LEARNER_A_EMAIL);
        session.supportedLearnerTypesAsString = learner.learnerType;
        session.assignedLearner = learner.email;

        session.save();
        learner.save();

        String oldEmail = learner.email;
        String newEmail = "new_email@gmail.com";
        learner.email = newEmail;
        Result result = callAction(
                routes.ref.LearnerController.updateLearner(learner.uuid),
                fakeRequest()
                        .withSession("email", COORDINATOR_A_EMAIL)
                        .withJsonBody(Json.toJson(learner))
        );
        Learner updatedLearner = Learner.find.byId(newEmail);
        Session updatedSession = Session.find.byId("IDa");

        assertThat(status(result)).isEqualTo(OK);
        assertThat(Learner.find.byId(oldEmail)).isNull();
        assertThat(updatedLearner).isNotNull();
        assertThat(updatedSession.assignedLearner).isEqualTo(newEmail);
    }

    @Test
    public void deletingLearner_validUUID_Succeeds() {
        Learner tempLearner = new Learner("templearner@gmail.com", "first", "last", COORDINATOR_A_EMAIL);
        tempLearner.save();
        String uuid = tempLearner.uuid;
        String email = tempLearner.email;

        Result result = callAction(
                routes.ref.LearnerController.ajaxDeleteLearner(uuid),
                fakeRequest().withSession("email", COORDINATOR_A_EMAIL)
        );

        assertThat(status(result)).isEqualTo(NO_CONTENT);
        assertThat(Learner.find.byId(email)).isNull();
    }

    @Test
    public void deletingLearner_nonexistantUUID_Succeeds() {
        Result result = callAction(
                routes.ref.LearnerController.ajaxDeleteLearner("fake_uuid"),
                fakeRequest().withSession("email", COORDINATOR_A_EMAIL)
        );

        assertThat(status(result)).isEqualTo(NO_CONTENT);
    }
}
