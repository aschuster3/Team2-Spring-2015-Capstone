package models;
import java.util.UUID;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.data.validation.Constraints.*;
import javax.persistence.*;

@Entity
public class SessionTemplate extends Model{

	@Id
	public String id;
	
	@Required
	public String title;
	
	@Required
	public int week;
	
	@Required
	public int day;
	
	@Required
	public boolean isAM;
			
	public SessionTemplate(String title, int week, int day, boolean isAM){
    	this.id = UUID.randomUUID().toString();
		this.title = title;
		this.week = week;
		this.day = day;
		this.isAM = isAM;
	}
	
	public SessionTemplate(String title, String week, String day, String isAM){
    	this.id = UUID.randomUUID().toString();
		this.title = title;
		this.week = (int)Integer.valueOf(week);
		this.day = Integer.valueOf(day);
		this.isAM = (isAM.equalsIgnoreCase("true")||isAM.equalsIgnoreCase("yes")||isAM.equalsIgnoreCase("AM"));
	}
	
	public static void create(String title, String week, String day, String isAM){
		SessionTemplate st = new SessionTemplate(title, week, day, isAM);
		st.save();
	}
	
    public static Finder<String, SessionTemplate> find = new Finder<String, SessionTemplate>(
            String.class, SessionTemplate.class);
}
