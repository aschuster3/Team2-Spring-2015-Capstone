package jobs;

import models.Session;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SessionThawingJob extends AkkaJob {

    public static final long THAWING_THRESHOLD_IN_DAYS = 14;

    public SessionThawingJob(FiniteDuration frequency) {
        super(frequency);
    }

    @Override
    public void run() {
        for (Session session: Session.find.all()) {
            if (shouldThawSession(session, new Date())) {
                session.thaw();
                session.save();
            }
        }
    }

    public boolean shouldThawSession(Session session, Date currentDate) {
        if (session.assignedLearner != null) {
            return false;
        }

        long currentMillis = new Date().getTime();
        long startDateMillis = session.date.getTime();
        long deltaMillis = startDateMillis - currentMillis;

        long daysUntilStart = TimeUnit.MILLISECONDS.toDays(deltaMillis);
        return daysUntilStart <= THAWING_THRESHOLD_IN_DAYS;
    }
}
