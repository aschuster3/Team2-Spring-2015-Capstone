import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.User;
import models.Learner;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

import org.junit.*;
import play.libs.Json;
import play.mvc.Result;

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

        new Learner(LEARNER_A_EMAIL, "Learner", "A", COORDINATOR_A_EMAIL).save();
        new Learner(LEARNER_B_EMAIL, "Learner", "B", COORDINATOR_B_EMAIL).save();
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
}
