import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.routes;
import models.Learner;
import models.Session;
import models.ScheduleTemplate;
import models.SessionTemplate;
import controllers.SessionController;

import models.User;
import org.junit.*;

import com.google.common.collect.ImmutableMap;

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
        session.assignedLearner = LEARNER_EMAIL;

        JsonNode json = Json.toJson(session);

        assertThat(json.path("type").asText()).isEqualTo(Session.TYPE_TAKEN);
    }
    
    @Test 
    public void createScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1");
    	scheduleTemp.save();
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.title)).isNotNull();
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
    public void testGetSupportedLearnerTypes() {
        Session session = new Session("session", new Date(0), true);
        session.supportedLearnerTypesAsString = "type1,type2";

        Set<String> typesSet = session.getSupportedLearnerTypes();

        assertThat(typesSet.size()).isEqualTo(2);
        assertThat(typesSet.contains("type1")).isTrue();
        assertThat(typesSet.contains("type2")).isTrue();
    }

    @Test
    public void addSessionToScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1");
    	scheduleTemp.save();
    	SessionTemplate sessionTemp = new SessionTemplate("Clinic", 1, 1, true);
    	sessionTemp.save();
 
    	assertThat(scheduleTemp.addSession(sessionTemp)).isTrue();
    }
    
    @Test
    public void createSessionsFromScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1");
    	scheduleTemp.save();
		//SessionTemplate sessionTemp = new SessionTemplate("session", 1, 1, true);
		//scheduleTemp.addSession(sessionTemp);
    	for (int week = 0; week<3; week++){
    		for(int day = 0; day<5; day++){
    			String name = "Week" + week + "Day" + day;
    			SessionTemplate sessionTempAM = new SessionTemplate(name + "AM", week, day, true);
    			SessionTemplate sessionTempPM = new SessionTemplate(name + "PM", week, day, false);
    			scheduleTemp.addSession(sessionTempAM);
    			scheduleTemp.addSession(sessionTempPM);
    		}
    	}
    	scheduleTemp.save();
    	
    	Result result = SessionController.createScheduleSessions(scheduleTemp.title, "2015/03/30");
    	assertThat(status(result)).isEqualTo(200);
		assertThat(Session.getAll().size()).isEqualTo(30);
    } 
    
}
