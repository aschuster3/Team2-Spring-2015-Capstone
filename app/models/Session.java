package models;

import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import play.data.validation.Constraints.*;

import javax.persistence.*;

import play.db.ebean.Model;


@Entity
@SuppressWarnings("serial")
/**
 * This is the class for sessions to be added to the calendar.
 * Admins may create sessions. Both admins and users can view sessions.
 * 
 * @author Julia Rapoport
 */
public class Session extends Model {

	/* for the front-end calendar */
	public static final String TYPE_TAKEN = "invalid";
	public static final String TYPE_FREE = "info";

	public static final int AM_STARTS_AT = 8;
	public static final int AM_ENDS_AT = 12;
	public static final int PM_STARTS_AT = 13;
	public static final int PM_ENDS_AT = 17;

    @Id
    public String id;

    @Required
    public String title;

	@Required
	public boolean isAM;

	@Required
	public Date date;  // only used for day-month-year

	public String physician;

	@ManyToOne
    public String assignedLearner;

	public Long recurringGroupId;
	
	public String scheduleTitle;

	/*
	 * Following "transients" are only for front-end Javascript objects
	 *
	 * Do not directly assign to these.
	 * These are read-only via getters, only (because they are dependent on other properties).
	 */
	@Transient
	private String type;

	@Transient
	@JsonProperty("starts_at")
	private Date startsAt;

	@Transient
	@JsonProperty("ends_at")
	private Date endsAt;

	/** Creates  an AM session */
	public Session(String id, String title, Date date) {
		this(id, title, date, true);
	}
    
    public Session(String id, String title, Date date, boolean isAM) {
		this(title, date, isAM);
		this.id = id;
    }
    
    public Session(String id, String title, Date date, String physician, boolean isAM) {
        this(id, title, date, isAM);
        this.physician = physician;
    }

	public Session(String title, Date date, boolean isAM){
		this.title = title;
		this.date = date;
		this.isAM = isAM;
	}
    
    public Session(String id, String title, String date, boolean isAM){
    	this.id = id;
    	this.title = title;
    	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    	
    	try{
    		this.date = formatter.parse(date);
    	}
    	catch(Exception e){
    		
    	}
    }
    
    public Session(String title, Date date, boolean isAM, String schedule){
    	this(title, date, isAM);
    	this.scheduleTitle = schedule;
    }

    public static Finder<String, Session> find = new Finder<String, Session>(
            String.class, Session.class);
    
    public static void create(Session session) {
		if (session.id == null) {
			session.id = UUID.randomUUID().toString();
		}
    	session.save();
    }

    public static List<Session> getAll() {
        return Session.find.all();
    }
    
    public String validate() {
    	Session session = Session.find.byId(this.id);
    	if (session != null) {
    		return "Session " + this.id + " already exists.";
    	}
    	return null;
    }

	private boolean isFree() {
		return this.assignedLearner == null;
	}

	public String getType() {
		return this.isFree() ? TYPE_FREE : TYPE_TAKEN;
	}

	public Date getStartsAt() {
		int startHour = this.isAM ? AM_STARTS_AT : PM_STARTS_AT;
		return getCopyOfDateWithHour(startHour);
	}

	public Date getEndsAt() {
		int endHour = this.isAM ? AM_ENDS_AT : PM_ENDS_AT;
		return getCopyOfDateWithHour(endHour);
	}

	private Date getCopyOfDateWithHour(int hour) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		return cal.getTime();
	}

	/**
	 * @return A clone which has identical fields, except for ID.
	 * transient fields are also ignored.
	 */
	public Session clone() {
		Session clone = new Session(this.title, this.date, this.isAM);
		clone.assignedLearner = this.assignedLearner;
		clone.physician = this.physician;
		clone.recurringGroupId = this.recurringGroupId;
		clone.id = null;

		return clone;
	}
}
