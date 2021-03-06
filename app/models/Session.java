package models;

import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

import com.google.common.base.Joiner;
import play.db.ebean.Model;


@Entity
@SuppressWarnings("serial")
/**
 * This is the class for sessions to be added to the calendar.
 * Admins may create sessions. Both admins and users can view sessions.
 * 
 * @author Julia Rapoport
 */
public class Session extends Model implements Comparable<Session> {

	/* for the front-end calendar */
	public static final String TYPE_TAKEN = "invalid";
	public static final String TYPE_FREE = "info";

	public static final int AM_STARTS_AT = 8;
	public static final int AM_ENDS_AT = 12;
	public static final int PM_STARTS_AT = 13;
	public static final int PM_ENDS_AT = 17;

    @Id
    public String id;

    public String title;

	public boolean isAM;

	public Date date;  // only used for day-month-year

	public String physician;

	// Learner's email address
    public String assignedLearner;

	public Long recurringGroupId;

	public boolean preventThawing;

	/*
	 * Only relevant for sessions that are created from templates
	 */
	public String scheduleGroupId;

	public String scheduleTitle;

	public boolean firstSessionInScheduleGroup;

	/**
	 * This is a comma separated list.
	 *
	 * Becaues Ebean does not support @ElementCollection!
	 */
	@Column(length=500)
	public String supportedLearnerTypesAsString;

	/*
	 * This needs to be a separate flag because we allow
	 * the user to specify any "Other" types,
	 * so creating a list of all accepted types is
	 * not feasible.
	 */
	public boolean supportsAnyLearnerType;



	/*
	 * Following "transients" are only for front-end Javascript objects
	 *
	 * Do not directly assign to these.
	 * These are read-only via getters, only (because they are dependent on other properties).
	 */
	@Transient
	private Set<String> supportedLearnerTypes;

	@Transient
	private String type;

	@Transient
	@JsonProperty("starts_at")
	private Date startsAt;

	@Transient
	@JsonProperty("ends_at")
	private Date endsAt;


	public Session(String id, String title, Date date, String physician, boolean isAM, String supportedLearnerTypesAsString) {
		this.title = title;
		this.date = date;
		this.isAM = isAM;
		this.id = id;
		this.physician = physician;
		this.supportedLearnerTypesAsString = supportedLearnerTypesAsString;
	}

	public Session(String id, String title, Date date, String physician, boolean isAM) {
		this(id, title, date, physician, isAM, "");
	}

	public Session(String id, String title, Date date, boolean isAM) {
		this(id, title, date, "", isAM);
	}

	public Session(String title, Date date, boolean isAM){
		this(null, title, date, isAM);
	}

	/** Creates  an AM session */
	public Session(String id, String title, Date date) {
		this(id, title, date, true);
	}

    public Session(String title, Date date, boolean isAM, String schedule){
    	this(title, date, isAM);
    	this.scheduleGroupId = schedule;
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
    
    public static List<Session> getLearnerSchedule(String learner) {
        List<Session> schedule = Session.find.where().eq("assignedLearner", learner).findList();
		Collections.sort(schedule);
		return schedule;
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

	public Set<String> getSupportedLearnerTypes() {
		if (this.supportedLearnerTypesAsString == null) {
			return new HashSet<>();
		}

		String[] typesArray = this.supportedLearnerTypesAsString.split(",");
		List<String> typesList = Arrays.asList(typesArray);
		Set<String> typesSet = new HashSet<>(typesList);

		return typesSet;
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

	public void thaw() {
		if (this.preventThawing) {
			return;
		}

		this.supportsAnyLearnerType = true;
		Set<String> supportedTypes = this.getSupportedLearnerTypes();
		for (String type: Learner.LEARNER_TYPES) {
			supportedTypes.add(type);
		}
		this.supportedLearnerTypesAsString = Joiner.on(',').join(supportedTypes);
		this.scheduleGroupId = null;
		this.scheduleTitle = null;
		this.firstSessionInScheduleGroup = false;
	}

	@Override
	public int compareTo(Session other) {
		if (other == null) {
			return -1;
		}

		Calendar thisCal = this.dateAsCalendar();
		Calendar otherCal = other.dateAsCalendar();

		if (thisCal.before(otherCal)) {
			return -1;
		} else if (thisCal.after(otherCal)) {
			return 1;
		}

		if (this.isAM && !other.isAM) {
			return -1;
		} else if (!this.isAM && other.isAM) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * @return A Calendar set to the session's date's Year, Month, and Day of Month
	 */
	public Calendar dateAsCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int thisYear = cal.get(Calendar.YEAR);
		int thisMonth = cal.get(Calendar.MONTH);
		int thisDayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

		// clear then reset in order to "normalize" to Year/Month/Day
		cal.clear();
		cal.set(Calendar.YEAR, thisYear);
		cal.set(Calendar.MONTH, thisMonth);
		cal.set(Calendar.DAY_OF_MONTH, thisDayOfMonth);

		return cal;
	}
}
