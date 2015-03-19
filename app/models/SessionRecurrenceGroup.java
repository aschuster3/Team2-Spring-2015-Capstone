package models;

import org.springframework.beans.factory.annotation.Required;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
public class SessionRecurrenceGroup extends Model {

    public static final int REC_TYPE_WEEKLY = 1;

    @Id
    public Long id;

    public int recurrenceType;

    /**
     * @param recurrenceType choose from REC_TYPE_XXX constants
     */
    public SessionRecurrenceGroup(int recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public static Model.Finder<Long, SessionRecurrenceGroup> find =
            new Model.Finder<>(Long.class, SessionRecurrenceGroup.class);

    public static void create(SessionRecurrenceGroup group) {
        group.save();
    }

    public Session findLastSession() {
        return Session.find
                .where().eq("recurrenceGroupId", this.id)
                .orderBy().desc("date")
                .findList().get(0);
    }

    /**
     * Generates new Session objects by cloning the "last" session associated
     * with this recurrence group.
     *
     * The "last" session is the session in the recurrence group with the latest
     * start date.
     * i.e. the return value from "findLastSession()"
     *
     * @return created sessions
     */
    public List<Session> generateNewOccurrences(int count) {
        Session latest = this.findLastSession();
        List<Session> createdSessions = new ArrayList<>(count);

        if (latest == null) {
            return new ArrayList<>();
        }

        Date latestDate = latest.date;
        for (int i = 0; i < count; i++) {
            Session next = latest.clone();
            next.date = nextOccurrenceDate(latestDate);

            Session.create(next);
            createdSessions.add(next);

            latestDate = next.date;
        }

        return createdSessions;
    }

    private Date nextOccurrenceDate(Date baseDate) {
        if (this.recurrenceType == REC_TYPE_WEEKLY) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(baseDate);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return cal.getTime();
        }
        return null;
    }

    public List<Session> allSessions() {
        List<Session> allSessions =
                Session.find.where().eq("recurrenceGroupId", this.id).findList();

        return allSessions;
    }
}
