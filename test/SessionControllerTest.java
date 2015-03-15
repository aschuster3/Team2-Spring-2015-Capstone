import java.util.Date;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import models.Learner;
import models.Session;

import models.User;
import org.junit.*;

import com.google.common.collect.ImmutableMap;

import play.libs.Json;
import play.mvc.Result;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class SessionControllerTest {

    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Before
	public void setup(){
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        new User("Admin", "User", ADMIN_EMAIL, "adminpassword", true).save();
	}
	
	@Test
    public void completeSessionCreation() {
		Session event1 = new Session("Event1", new Date(System.currentTimeMillis()), true);
        Session.create(event1);
        
        Session event2 = new Session("Event2", new Date(System.currentTimeMillis()), true);
        String event1ID = event1.id;

        assertThat(Session.find.byId(event1ID)).isNotNull();
        
        Result result = callAction(
                controllers.routes.ref.SessionController.createSession(),
                fakeRequest().withSession("email", ADMIN_EMAIL).withFormUrlEncodedBody(ImmutableMap.of(
                        "id", "1001",
                        "title", "firstSession",
                        "date", "2015-3-1",
                        "isAM", "true"))
        );
        
        assertThat(303).isEqualTo(status(result));
		assertThat("Event1").isEqualTo(event1.title);
		assertThat(Session.getAll().contains(event1)).isEqualTo(true);
		assertThat(Session.getAll().size()).isEqualTo(2);
		assertThat(Session.getAll().contains(event2)).isEqualTo(false);
	}
	
	@Test
	public void sessionCreationFailedOnUsedID() {
		Session event1 = new Session("102", "Event1", new Date(System.currentTimeMillis()));
		Session.create(event1);
		
		Result result = callAction(
                controllers.routes.ref.SessionController.createSession(),
                fakeRequest().withSession("email", ADMIN_EMAIL).withFormUrlEncodedBody(ImmutableMap.of(
                        "id", "102",
                        "title", "firstSession",
                        "date", "2015-3-1",
                        "isAM", "true"))
        );
		
		assertThat(400).isEqualTo(status(result));
		assertThat(Session.getAll().contains(event1)).isEqualTo(true);
		assertThat(Session.getAll().size()).isEqualTo(1);
	}

    @Test
    public void deleteSessionSucceeds() {
        Session session = new Session("1", "session-to-delete", new Date(0));
        Session.create(session);

        Result result = callAction(
                routes.ref.SessionController.deleteSession("1"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(204).isEqualTo(status(result));
        assertThat(Session.getAll().isEmpty()).isTrue();
    }

    @Test
    public void deleteSession_failsOnBadSessionID() {
        Session session = new Session("1", "session-to-delete", new Date(0));
        Session.create(session);

        Result result = callAction(
                routes.ref.SessionController.deleteSession("2"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        assertThat(Session.getAll().size()).isEqualTo(1);
    }

    @Test
    public void updateSession_existingSession_Succeeds() {
        Session session = new Session("1", "old-title", new Date(0));
        Session.create(session);
        JsonNode jsonForUpdatedSession = Json.toJson(
                new Session("1", "new-title", new Date(0)));

        Result result = callAction(
                routes.ref.SessionController.updateSession("1"),
                fakeRequest()
                    .withSession("email", ADMIN_EMAIL)
                    .withJsonBody(jsonForUpdatedSession)
        );
        Session updatedSession = Session.find.byId("1");

        assertThat(status(result)).isEqualTo(204);
        assertThat(updatedSession.title).isEqualTo("new-title");
    }

    @Test
    public void updateSession_forNewSession_Succeeds() {
        JsonNode jsonForNewSession = Json.toJson(
                new Session("1", "new-title", new Date(0)));

        Result result = callAction(
                routes.ref.SessionController.updateSession("1"),
                fakeRequest()
                        .withSession("email", ADMIN_EMAIL)
                        .withJsonBody(jsonForNewSession)
        );
        Session newSession = Session.find.byId("1");

        assertThat(status(result)).isEqualTo(201);
        assertThat(newSession).isNotNull();
    }

    @Test
    public void jsonCreateSession_Succeeds() {
        JsonNode jsonForNewSession = Json.toJson(
                new Session("1", "new-title", new Date(0)));

        Result result = callAction(
                routes.ref.SessionController.jsonCreateSession(),
                fakeRequest()
                        .withSession("email", ADMIN_EMAIL)
                        .withJsonBody(jsonForNewSession)
        );
        Session newSession = Session.find.byId("1");

        assertThat(status(result)).isEqualTo(201);
        assertThat(newSession).isNotNull();
    }

    @Test
    public void ensureFreeTypeIsSerializedToJSON() {
        JsonNode json = Json.toJson(
                new Session("1", "new-title", new Date(0)));

        assertThat(json.path("type").asText()).isEqualTo(Session.TYPE_FREE);
    }

    @Test
    public void ensureTakenTypeIsSerializedToJSON() {
        Session session = new Session("1", "title", new Date(0));
        Learner learner = new Learner("email", "first", "last", ADMIN_EMAIL);
        session.assignedLearner = learner;

        JsonNode json = Json.toJson(session);

        assertThat(json.path("type").asText()).isEqualTo(Session.TYPE_TAKEN);
    }
}
