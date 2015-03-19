package jobs;

import models.SessionRecurrenceGroup;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;

/**
 * Creates new Session instances for recurrence groups.
 */
public class SessionRecurrenceJob extends AkkaJob {

    private final int newSessionsPerRun;

    public SessionRecurrenceJob(FiniteDuration frequency, int newSessionsPerRun) {
        super(frequency);
        this.newSessionsPerRun = newSessionsPerRun;
    }

    @Override
    public void run() {
        List<SessionRecurrenceGroup> recurrenceGroups = SessionRecurrenceGroup.find.all();
        for (SessionRecurrenceGroup group: recurrenceGroups) {
            group.generateNewOccurrences(this.newSessionsPerRun);
        }
    }

}
