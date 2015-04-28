package models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.SecuredAdminAction;
import play.db.ebean.Model;
import play.data.validation.Constraints.*;
import play.mvc.Result;
import play.mvc.With;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * This is a class for generating templates of schedules.  All schedule templates have 
 * a title, learner type, id, and list of sessions within the template. Only admins can create
 * schedule templates. 
 * 
 * @author Julia Rapoport
 *
 */
@Entity
public class ScheduleTemplate extends Model {

	@Column(unique=true)
	@Required
	public String title;
	
	@Required
	public String learnerType;
	
	@Id
    public String uuid;
	
	@Required
	@OneToMany(mappedBy="schedule", cascade=CascadeType.ALL)
	@JsonIgnore
	public List<SessionTemplate> sessions;
	
	public ScheduleTemplate(String title, String learnerType){
		this.title = title;
		this.learnerType = learnerType;
		this.sessions = new ArrayList<SessionTemplate>();
        this.uuid = UUID.randomUUID().toString();
	}
	
	/**
	 * Adds a session to the list of sessions for this template. If this session already 
	 * exists in this template, a duplicate will not be added. 
	 * 
	 * @param session
	 * @return true if the session was successfully added to this template
	 */
	public boolean addSession(SessionTemplate session){
		if(!sessions.contains(session)){
			return sessions.add(session);
		}
		else return false;
	}
	
	public boolean deleteSession(SessionTemplate session){
		boolean result = sessions.remove(session);
		if(result){
			return SessionTemplate.delete(session);
		}
		return result;
	}
	
	public static void create(String title, String learnerType){
		ScheduleTemplate st = new ScheduleTemplate(title, learnerType);
		st.save();
	}
	
	public boolean updateTitle(String title){
		if(ScheduleTemplate.find.byId(title) == null){
			this.title = title;
			this.save();
			return true;
		}
		return false;
	}
	
	public void updateLearnerType(String learnerType){
		this.learnerType = learnerType;
		this.save();
	}
	
	 public static Finder<String, ScheduleTemplate> find = new Finder<String, ScheduleTemplate>(
	            String.class, ScheduleTemplate.class);

	@Transient
	public int getStartDay() {
		Collections.sort(sessions);
		return sessions.get(0).day;
	}
	
	public static Boolean delete(ScheduleTemplate st){
		if(ScheduleTemplate.find.byId(st.uuid)!=null){
			st.delete();
			return true;
		}
		return false;
	}
	
	
}
