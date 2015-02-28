import java.util.Date;

import models.Session;

import org.junit.*;

import com.google.common.collect.ImmutableMap;

import play.mvc.Result;
import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

public class SessionControllerTest {

	@Before
	public void setup(){
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
			
	}
	
	@Test
    public void completeSessionCreation() {
		Session event1 = new Session("Event1", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        Session.create(event1);
        
        Session event2 = new Session("Event2", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()));
        String event1ID = event1.id;

        assertThat(Session.find.byId(event1ID)).isNotNull();
        
        Result result = callAction(
                controllers.routes.ref.SessionController.createSession(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "id", "1001",
                        "title", "firstSession",
                        "starts_at", "2015-3-1",
                        "ends_at", "2015-3-2",
                        "isFree", "yes"))
        );
        
        assertThat(303).isEqualTo(status(result));
		assertThat("Event1").isEqualTo(event1.title);
		assertThat(Session.getAll().contains(event1)).isEqualTo(true);
		assertThat(Session.getAll().size()).isEqualTo(2);
		assertThat(Session.getAll().contains(event2)).isEqualTo(false);
	}
	
	@Test
	public void sessionCreationFailedOnUsedID() {
		Session event1 = new Session("102", "Event1", new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()), true);
		Session.create(event1);
		
		Result result = callAction(
                controllers.routes.ref.SessionController.createSession(),
                fakeRequest().withFormUrlEncodedBody(ImmutableMap.of(
                        "id", "102",
                        "title", "firstSession",
                        "starts_at", "2015-3-1",
                        "ends_at", "2015-3-2",
                        "isFree", "yes"))
        );
		
		assertThat(400).isEqualTo(status(result));
		assertThat(Session.getAll().contains(event1)).isEqualTo(true);
		assertThat(Session.getAll().size()).isEqualTo(1);
	}
}
