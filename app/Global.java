import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jobs.RecurringSessionJob;
import jobs.SessionThawingJob;
import models.Learner;
import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
import models.UnapprovedUser;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;


public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        // Check if the database is empty
        if (User.find.findRowCount() == 0) {
            
            // This is the Admin User.  If you wish to have more, follow the following convention
            //                   First    Last    Email              Password
            User.create(new User("Admin", "User", "admin@emory.edu", "secret", true));
        }

        ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Json.setObjectMapper(mapper);

        SessionThawingJob thawJob = new SessionThawingJob(Duration.create(1, TimeUnit.MINUTES));
        thawJob.schedule(Duration.create(0, TimeUnit.MILLISECONDS));
    }
}
