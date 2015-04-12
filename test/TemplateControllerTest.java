

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.*;
import static play.test.Helpers.callAction;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeGlobal;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.start;
import static play.test.Helpers.status;

import java.util.ArrayList;
import java.util.List;

import models.Learner;
import models.ScheduleTemplate;
import models.SessionTemplate;
import models.User;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import play.mvc.Result;

import controllers.TemplateController;

public class TemplateControllerTest {

    @Before
	public void setup(){
		start(fakeApplication(inMemoryDatabase(), fakeGlobal()));
	}
    
	@Test
    public void createSessionTemplate(){
		
		User.create(new User("Admin", "User", "admin@gmail.com", "adminpassword", true));
		ScheduleTemplate st = new ScheduleTemplate("test");
		st.save();
		
		Result result = callAction(
	                controllers.routes.ref.TemplateController.createSessionTemplate(st.title),
	                fakeRequest().withSession("email", "admin@gmail.com")
	                			.withFormUrlEncodedBody(ImmutableMap.of("title", "Clinic with Dr. Bob",
	                					"week", "1",
	                					"day", "2",
	                					"isAM", "true"))
	            );
		assertThat(status(result)).isEqualTo(303);
		assertThat(SessionTemplate.find.where().eq("title", "Clinic with Dr. Bob").eq("week", 1)
				.eq("day", 2).eq("isAM", true)).isNotNull();
    	assertThat(SessionTemplate.find.all().size()).isEqualTo(1);
	        
    	result = callAction(
                controllers.routes.ref.TemplateController.createSessionTemplate(st.title),
                fakeRequest().withSession("email", "admin@gmail.com")
                			.withFormUrlEncodedBody(ImmutableMap.of("title", "Clinic with Dr. Bob",
                					"week", "1",
                					"day", "2",
                					"isAM", "true"))
            );
    	assertThat(status(result)).isEqualTo(400);
    }
	
	@Test
    public void createScheduleTemplate(){
		User.create(new User("Admin", "User", "admin@gmail.com", "adminpassword", true));
		
		Result result = callAction(
	                controllers.routes.ref.TemplateController.createScheduleTemplate(),
	                fakeRequest().withSession("email", "admin@gmail.com")
	                			.withFormUrlEncodedBody(ImmutableMap.of("title", "subi1"))
	            );
	        
	        assertThat(303).isEqualTo(status(result));
	        ScheduleTemplate st = ScheduleTemplate.find.byId("subi1");
	        assertThat(st).isNotNull();
	        assertThat(st.title).isEqualTo("subi1");
	        assertThat(ScheduleTemplate.find.all().size()).isEqualTo(1);
	        
	     result = callAction(
	                controllers.routes.ref.TemplateController.createScheduleTemplate(),
	                fakeRequest().withSession("email", "admin@gmail.com")
	                			.withFormUrlEncodedBody(ImmutableMap.of("title", "subi1"))
	            );
	        
	        assertThat(400).isEqualTo(status(result));
	        assertThat(ScheduleTemplate.find.all().size()).isEqualTo(1);
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
    
    @Test
	public void removeSession(){
    	SessionTemplate st1 = new SessionTemplate("clinic", 1, 1, true);
    	st1.save();
    	SessionTemplate st2 = new SessionTemplate("clinic", 1, 2, true);
    	st2.save();
    	
    	assertThat(SessionTemplate.find.all().size()).isEqualTo(2);
    	
    	assertThat(SessionTemplate.delete(st1)).isEqualTo(true);
    	
    	assertThat(SessionTemplate.find.all().size()).isEqualTo(1);
    	
    	assertThat(SessionTemplate.delete(new SessionTemplate("clinic", 2, 1, false))).isEqualTo(false);
    }
    
    @Test
    public void updateSession(){
    	SessionTemplate st1 = new SessionTemplate("clinic", 1, 2, true);
    	st1.save();
    	
    	assertThat(SessionTemplate.find.byId(st1.id).title).isEqualTo("clinic");
    	assertThat(SessionTemplate.find.byId(st1.id).week).isEqualTo(1);
    	assertThat(SessionTemplate.find.byId(st1.id).day).isEqualTo(2);
    	assertThat(SessionTemplate.find.byId(st1.id).isAM).isEqualTo(true);
    	
    	st1.updateTitle("Appointment");
    	assertThat(SessionTemplate.find.byId(st1.id).title).isEqualTo("Appointment");
    	assertThat(SessionTemplate.find.byId(st1.id).week).isEqualTo(1);
    	assertThat(SessionTemplate.find.byId(st1.id).day).isEqualTo(2);
    	assertThat(SessionTemplate.find.byId(st1.id).isAM).isEqualTo(true);
    	
    	st1.updateWeek(3);
    	st1.updateDay(4);
    	st1.updateAM(false);
    	
    	assertThat(SessionTemplate.find.byId(st1.id).title).isEqualTo("Appointment");
    	assertThat(SessionTemplate.find.byId(st1.id).week).isEqualTo(3);
    	assertThat(SessionTemplate.find.byId(st1.id).day).isEqualTo(4);
    	assertThat(SessionTemplate.find.byId(st1.id).isAM).isEqualTo(false);

    }
    
    @Test
	public void removeSessionFromSchedule(){
    	ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi2");
    	SessionTemplate clinic1 = new SessionTemplate("Clinic1", 1, 1, true);
    	SessionTemplate clinic2 = new SessionTemplate("Clinic2", 1, 2, true);

    	scheduleTemp.addSession(clinic1);
    	scheduleTemp.addSession(clinic2);
    	scheduleTemp.save();
    	
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.title)).isNotNull();
    	assertThat(SessionTemplate.find.byId(clinic1.id)).isNotNull();
    	assertThat(SessionTemplate.find.byId(clinic2.id)).isNotNull();
    	
    	List<SessionTemplate> sessions = ScheduleTemplate.find.byId(scheduleTemp.title).sessions;

    	assertThat(sessions).isNotNull();
    	assertThat(sessions.size()).isEqualTo(new Integer(2));
    	
    	assertThat(scheduleTemp.deleteSession(clinic1)).isEqualTo(true);
    	assertThat(ScheduleTemplate.find.byId(scheduleTemp.title).sessions.size()).isEqualTo(1);
    	
    	assertThat(SessionTemplate.find.all().size()).isEqualTo(1);
    }
}
