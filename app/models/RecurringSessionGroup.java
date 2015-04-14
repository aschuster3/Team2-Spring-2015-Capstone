package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
public class RecurringSessionGroup extends Model {

    public static final int REC_TYPE_WEEKLY = 1;
    public static final int REC_TYPE_MONTHLY = 2;

    @Id
    public Long id;

    public int recurringType;

    /**
     * @param recurringType choose from REC_TYPE_XXX constants
     */
    public RecurringSessionGroup(int recurringType) {
        this.recurringType = recurringType;
    }

    public static Model.Finder<Long, RecurringSessionGroup> find =
            new Model.Finder<>(Long.class, RecurringSessionGroup.class);

    public static void create(RecurringSessionGroup group) {
        group.save();
    }

    public Session findLastSession() {
        return Session.find
                .where().eq("recurringGroupId", this.id)
                .orderBy().desc("date")
                .findList().get(0);
    }

    /**
     * Generates new Session objects by cloning the "last" session associated
     * with this recurring session group.
     *
     * The "last" session is the session in the recurring group with the latest
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
        if (invalidRecurringType(this.recurringType)) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        if (recurringType == REC_TYPE_WEEKLY) {

        }
        int calendarFieldToIncrement = calendarFieldFromRecurringType();
        cal.add(calendarFieldToIncrement, 1);
        return cal.getTime();
    }

    private boolean invalidRecurringType(int recurringType) {
        return recurringType != REC_TYPE_WEEKLY
                && recurringType != REC_TYPE_MONTHLY;
    }

    private int calendarFieldFromRecurringType() {
        return this.recurringType == REC_TYPE_MONTHLY
                ? Calendar.MONTH
                : Calendar.WEEK_OF_YEAR;
    }

    public List<Session> allSessions() {
        List<Session> allSessions =
                Session.find.where().eq("recurringGroupId", this.id).findList();

        return allSessions;
    }
}
