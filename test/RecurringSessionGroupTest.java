import models.Learner;
import models.Session;
import models.RecurringSessionGroup;
import models.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class RecurringSessionGroupTest {

    private static final String COORDINATOR_EMAIL = "admin@gmail.com";

    @Before
    public void setup() {
        start(fakeApplication(inMemoryDatabase(), fakeGlobal()));

        new User("Coordinator", "User", COORDINATOR_EMAIL, "adminpassword", false).save();
    }

    @Test
    public void generateNewOccurrences_Weekly_Succeeds() {
        Learner learner = new Learner("learner@gmail.com", "first", "last", COORDINATOR_EMAIL);
        learner.save();

        RecurringSessionGroup recGroup =
                new RecurringSessionGroup(RecurringSessionGroup.REC_TYPE_WEEKLY);
        recGroup.save();

        Calendar baseCal = Calendar.getInstance();
        baseCal.set(2015, 5, 1);
        Session base = new Session("title", baseCal.getTime(), true);
        base.assignedLearner = learner.email;
        base.recurringGroupId = recGroup.id;
        base.save();


        recGroup.generateNewOccurrences(2);

        List<Session> sessions = Session.find.all();
        assertThat(sessions.size()).isEqualTo(3);

        assertThat(sessions.get(1).title).isEqualTo(base.title);
        assertThat(sessions.get(2).title).isEqualTo(base.title);

        assertThat(sessions.get(1).isAM).isEqualTo(base.isAM);
        assertThat(sessions.get(2).isAM).isEqualTo(base.isAM);

        assertThat(sessions.get(1).physician).isEqualTo(base.physician);
        assertThat(sessions.get(2).physician).isEqualTo(base.physician);

        assertThat(sessions.get(1).assignedLearner).isNotNull();
        assertThat(sessions.get(2).assignedLearner).isNotNull();

        Calendar actualCal1 = Calendar.getInstance();
        Calendar actualCal2 = Calendar.getInstance();
        actualCal1.setTime(sessions.get(1).date);
        actualCal2.setTime(sessions.get(2).date);

        assertThat(actualCal1.get(Calendar.DAY_OF_MONTH)).isEqualTo(baseCal.get(Calendar.DAY_OF_MONTH) + 7);
        assertThat(actualCal2.get(Calendar.DAY_OF_MONTH)).isEqualTo(baseCal.get(Calendar.DAY_OF_MONTH) + 14);
    }

    @Test
    public void testFindLastSession() {
        RecurringSessionGroup recGroup =
                new RecurringSessionGroup(RecurringSessionGroup.REC_TYPE_WEEKLY);
        RecurringSessionGroup.create(recGroup);

        Session lastSession = new Session("lastSession", new Date(10), true);
        Session firstSession = new Session("firstSession", new Date(9), true);
        Session unrelatedSession = new Session("unrelated", new Date(11), true);

        lastSession.recurringGroupId = recGroup.id;
        firstSession.recurringGroupId = recGroup.id;

        Session.create(lastSession);
        Session.create(firstSession);
        Session.create(unrelatedSession);

        assertThat(recGroup.findLastSession().id).isEqualTo(lastSession.id);
    }
}
