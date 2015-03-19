package jobs;

import models.RecurringSessionGroup;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;

/**
 * Creates new Session instances for recurrence groups.
 */
public class RecurringSessionJob extends AkkaJob {

    private final int newSessionsPerRun;

    public RecurringSessionJob(FiniteDuration frequency, int newSessionsPerRun) {
        super(frequency);
        this.newSessionsPerRun = newSessionsPerRun;
    }

    @Override
    public void run() {
        List<RecurringSessionGroup> recurrenceGroups = RecurringSessionGroup.find.all();
        for (RecurringSessionGroup group: recurrenceGroups) {
            group.generateNewOccurrences(this.newSessionsPerRun);
        }
    }

}
