

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;

import java.util.ArrayList;
import java.util.List;

import models.ScheduleTemplate;
import models.SessionTemplate;
import models.User;

import org.junit.Before;
import org.junit.Test;

public class TemplateControllerTest {

    @Before
	public void setup(){
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
	}
    
	@Test
    public void createSessionTemplate(){
    	SessionTemplate sessionTemp = new SessionTemplate("Clinic", 1, 1, true);
    	sessionTemp.save();
    	assertThat(SessionTemplate.find.byId(sessionTemp.id)).isNotNull();
    }
	
	@Test
    public void createScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1");
    	scheduleTemp.save();
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.title)).isNotNull();
    }
    
    @Test
    public void addSessionsToScheduleTemplate(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi2");
    	SessionTemplate clinic1 = new SessionTemplate("Clinic1", 1, 1, true);
    	SessionTemplate clinic2 = new SessionTemplate("Clinic2", 1, 2, true);
 
    	scheduleTemp.save();
    	scheduleTemp.addSession(clinic1);
    	scheduleTemp.save();
    	scheduleTemp.addSession(clinic2);
    	scheduleTemp.save();
    	
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.title)).isNotNull();
    	assertThat(SessionTemplate.find.byId(clinic1.id)).isNotNull();
    	assertThat(SessionTemplate.find.byId(clinic2.id)).isNotNull();
    	
    	List<SessionTemplate> sessions = ScheduleTemplate.find.byId(scheduleTemp.title).sessions;

    	assertThat(sessions).isNotNull();
    	assertThat(sessions.size()).isEqualTo(new Integer(2));
    }
    
    //STILL NEED TO ADD TEST FOR REMOVING SESSIONS FROM A SCHEDULE AND EDITTING SESSIONS
}
