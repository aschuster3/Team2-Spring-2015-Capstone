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

    public Date endDate;

    public boolean useLastDayOfWeekOccurrence = false;  // for monthly only

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
        switch (this.recurringType) {
            case REC_TYPE_WEEKLY:   return nextOccurrenceDateForWeekly(baseDate);
            case REC_TYPE_MONTHLY:  return nextOccurrenceDateForMonthly(baseDate);
            default:                return null;
        }
    }

    private Date nextOccurrenceDateForWeekly(Date baseDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime();
    }

    private Date nextOccurrenceDateForMonthly(Date baseDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(baseDate);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        // Ex: first Monday of month is the 1st occurrence of Monday in the month
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(baseDate);
        cal2.set(Calendar.DAY_OF_MONTH, 1);

        int dayOfWeekOccurrencesUntilBaseDate = 0;
        do {
            if (cal2.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                dayOfWeekOccurrencesUntilBaseDate++;
            }
            cal2.add(Calendar.DAY_OF_MONTH, 1);
        } while (cal2.get(Calendar.DAY_OF_MONTH) <= dayOfMonth
                    && cal2.get(Calendar.MONTH) == cal.get(Calendar.MONTH));

        if (dayOfWeekOccurrencesUntilBaseDate == 5) {
            this.useLastDayOfWeekOccurrence = true;
        }

        cal.add(Calendar.MONTH, 1);
        setCalendarToFirstOccurrenceInMonth(cal, dayOfWeek);

        /*
         * Here, we check if the month changes because we don't want
         * to suggest a date in the next month.
         *
         * In the case that this month does not have an "nth" occurrence
         * of a particular DAY_OF_WEEK, then default to the last
         * occurrence of that DAY_OF_WEEK.
         */
        int month = cal.get(Calendar.MONTH);
        for (int i = 1; i < dayOfWeekOccurrencesUntilBaseDate || useLastDayOfWeekOccurrence; i++) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            if (month != cal.get(Calendar.MONTH)) {
                cal.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            }
        }

        return cal.getTime();
    }

    private void setCalendarToFirstOccurrenceInMonth(Calendar cal, int dayOfWeek) {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        while (cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    public List<Session> allSessions() {
        List<Session> allSessions =
                Session.find.where().eq("recurringGroupId", this.id).findList();

        return allSessions;
    }
}
