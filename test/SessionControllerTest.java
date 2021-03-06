import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import models.Learner;
import models.Session;
import models.ScheduleTemplate;
import models.SessionTemplate;

import models.User;
import org.junit.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.base.Joiner;

import play.libs.Json;
import play.mvc.Result;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class SessionControllerTest {

    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String COORDINATOR_EMAIL = "coordinator@gmail.com";
    private static final String LEARNER_EMAIL = "learner@gmail.com";

    @Before
	public void setup(){
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        User.create(new User("Admin", "User", ADMIN_EMAIL, "adminpassword", true));
        User.create(new User("Coordinator", "User", COORDINATOR_EMAIL, "coordinatorpassword", false));
        new Learner(LEARNER_EMAIL, "Learner", "nonuser", COORDINATOR_EMAIL).save();
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
    public void deleteSession_doesNotFailOnBadSessionID() {
        Session session = new Session("1", "session-to-delete", new Date(0));
        Session.create(session);

        Result result = callAction(
                routes.ref.SessionController.deleteSession("2"),
                fakeRequest().withSession("email", ADMIN_EMAIL)
        );

        assertThat(status(result)).isEqualTo(204);
        assertThat(Session.getAll().size()).isEqualTo(1);
    }

    @Test
    public void updateSession_existingSession_Succeeds() {
        Session session = new Session("1", "old-title", new Date(0));
        Session.create(session);
        JsonNode jsonForUpdatedSession = Json.toJson(
                new Session("1", "new-title", new Date(0)));
        
        assertThat(jsonForUpdatedSession).isNotNull();
        
        
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
    public void updateSession_forNonexistantSession_Fails() {
        JsonNode jsonForNewSession = Json.toJson(
                new Session("1", "new-title", new Date(0)));

        Result result = callAction(
                routes.ref.SessionController.updateSession("1"),
                fakeRequest()
                        .withSession("email", ADMIN_EMAIL)
                        .withJsonBody(jsonForNewSession)
        );
        Session newSession = Session.find.byId("1");

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        assertThat(newSession).isNull();
    }

    @Test
    public void getSession_validID_ReturnsSession() {
        Session session = new Session("id", "title", new Date(0));
        Session.create(session);

        Result result = callAction(
                routes.ref.SessionController.getSession("id"),
                fakeRequest().withSession("email", COORDINATOR_EMAIL)
        );

        assertThat(status(result)).isEqualTo(OK);
    }

    @Test
    public void getSession_nonexistantID_ReturnsBadRequest() {
        Session session = new Session("id", "title", new Date(0));
        Session.create(session);

        Result result = callAction(
                routes.ref.SessionController.getSession("wrong_id"),
                fakeRequest().withSession("email", COORDINATOR_EMAIL)
        );

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
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
        session.assignedLearner = LEARNER_EMAIL;

        JsonNode json = Json.toJson(session);

        assertThat(json.path("type").asText()).isEqualTo(Session.TYPE_TAKEN);
    }
    
    @Test 
    public void createScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1", "subi");
    	scheduleTemp.save();
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.uuid)).isNotNull();
    }
    
    @Test
    public void addSessionToLearner() {
        Session session = new Session("20", "something", new Date(0));
        Session.create(session);
        session.assignedLearner = LEARNER_EMAIL;
        JsonNode jsonForUpdatedSession = Json.toJson(
                session);
    
        Result result = callAction(
                routes.ref.SessionController.updateSession("20"),
                fakeRequest()
                    .withSession("email", COORDINATOR_EMAIL)
                    .withJsonBody(jsonForUpdatedSession)
        );
        Session updatedSession = Session.find.byId("20");
    
        assertThat(status(result)).isEqualTo(204);
        assertThat(updatedSession.assignedLearner).isEqualTo(LEARNER_EMAIL);
    }

    @Test
    public void addSessionToLearner_asCoordinator_failsWhenSessionHasALearner() {
        Session session = new Session("id", "title", new Date(0));
        session.assignedLearner = LEARNER_EMAIL;
        Session.create(session);

        String otherLearnerEmail = "otherlearner@gmail.com";
        Session sessionWithOtherLearnerAssigned = new Session("id", "title", new Date(0));
        sessionWithOtherLearnerAssigned.assignedLearner = otherLearnerEmail;
        JsonNode jsonForUpdatedSession = Json.toJson(sessionWithOtherLearnerAssigned);

        Result result = callAction(
                routes.ref.SessionController.updateSession("id"),
                fakeRequest()
                    .withSession("email", COORDINATOR_EMAIL)
                    .withJsonBody(jsonForUpdatedSession)
        );
        Session updatedSession = Session.find.byId("id");

        assertThat(status(result)).isEqualTo(BAD_REQUEST);
        assertThat(updatedSession.assignedLearner).isEqualTo(LEARNER_EMAIL);
    }

    @Test
    public void removeSessionFromLearner() {
        Session session = new Session("id", "title", new Date(0));
        session.assignedLearner = LEARNER_EMAIL;
        Session.create(session);

        Session sessionWithoutLearner = new Session("id", "title", new Date(0));
        session.assignedLearner = null;
        JsonNode jsonForUpdatedSession = Json.toJson(sessionWithoutLearner);

        Result result = callAction(
                routes.ref.SessionController.updateSession("id"),
                fakeRequest()
                .withSession("email", COORDINATOR_EMAIL)
                .withJsonBody(jsonForUpdatedSession)
        );
        Session updatedSession = Session.find.byId("id");

        assertThat(status(result)).isEqualTo(NO_CONTENT);
        assertThat(updatedSession.assignedLearner).isEqualTo(null);
    }

    @Test
    public void testGetSupportedLearnerTypes() {
        Session session = new Session("session", new Date(0), true);
        session.supportedLearnerTypesAsString = "type1,type2";

        Set<String> typesSet = session.getSupportedLearnerTypes();

        assertThat(typesSet.size()).isEqualTo(2);
        assertThat(typesSet.contains("type1")).isTrue();
        assertThat(typesSet.contains("type2")).isTrue();
    }

    @Test
    public void sessionCanHoldAllSupportedLearnerTypes() {
        Session sessionForAllTypes = new Session("session", "title", new Date(0));

        Joiner joiner = Joiner.on(',').skipNulls();
        sessionForAllTypes.supportedLearnerTypesAsString = joiner.join(Learner.LEARNER_TYPES);
        int expectedLength = sessionForAllTypes.supportedLearnerTypesAsString.length();
        Session.create(sessionForAllTypes);

        Session resultSession = Session.find.byId("session");
        int actualLength = resultSession.supportedLearnerTypesAsString.length();

        assertThat(actualLength).isEqualTo(actualLength);
    }

    @Test
    public void addSessionToScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1", "subi");
    	scheduleTemp.save();
    	SessionTemplate sessionTemp = new SessionTemplate("Emory", "Bob", 1, 1, true);
    	sessionTemp.save();
 
    	assertThat(scheduleTemp.addSession(sessionTemp)).isTrue();
    }
    
    @Test
    public void createSessionsFromScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1", "subi");
    	scheduleTemp.save();
    	for (int week = 0; week<3; week++){
    		for(int day = 0; day<5; day++){
    			String name = "Week" + week + "Day" + day;
    			SessionTemplate sessionTempAM = new SessionTemplate("Emory", name + "AM", week, day, true);
    			SessionTemplate sessionTempPM = new SessionTemplate("VA", name + "PM", week, day, false);
    			scheduleTemp.addSession(sessionTempAM);
    			scheduleTemp.addSession(sessionTempPM);
    		}
    	}
    	scheduleTemp.save();

        Map<String, Date> bodyMap = ImmutableMap.of("startDate", new Date(0));
        JsonNode bodyJson = Json.toJson(bodyMap);
    	
    	Result result = callAction(
                routes.ref.SessionController.createScheduleSessions(scheduleTemp.uuid),
                fakeRequest()
                        .withSession("email", COORDINATOR_EMAIL)
                        .withJsonBody(bodyJson)
        );
    	assertThat(status(result)).isEqualTo(CREATED);
		assertThat(Session.getAll().size()).isEqualTo(30);
        assertThat(Session.getAll().get(0).scheduleGroupId).isEqualTo(Session.getAll().get(1).scheduleGroupId);
        assertThat(Session.getAll().get(0).scheduleGroupId).isNotEqualTo(scheduleTemp.uuid);
    }

    @Test
    public void sessionThawSucceeds_SupportsAnyLearnerType() {
        Session session = new Session(null, "title", new Date(0));
        session.thaw();
        assertThat(session.supportsAnyLearnerType).isEqualTo(true);
    }

    @Test
    public void sessionThaw_OnSessionFromTemplate_FreesSessionFromGroup() {
        Session session = new Session(null, "title", new Date(0));
        session.scheduleGroupId = UUID.randomUUID().toString();
        session.scheduleTitle = "Schedule Title";

        session.thaw();

        assertThat(session.scheduleGroupId).isNull();
        assertThat(session.scheduleTitle).isNull();
    }

    @Test
    public void sessionThaw_failsIfPreventThawIsSet() {
        Session session = new Session(null, "title", new Date(0));
        session.preventThawing = true;

        session.thaw();

        assertThat(session.supportsAnyLearnerType).isFalse();
    }
    
}
