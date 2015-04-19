import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jobs.RecurringSessionJob;
import jobs.SessionThawingJob;
import models.Learner;
import models.ScheduleTemplate;
import models.Session;
import models.SessionTemplate;
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
            Logger.info("Data added");

            User bahb = new User("Bob", "Lob", "bob@gmail.com", "secret", false, "Emory Internal Medicine", "770-820-1063");
            User frank = new User("Frank", "Knarf", "frank@gmail.com", "allyourbase", false, "Emory Allergy", "770-241-2206");
            
            User.create(bahb);
            User.create(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
            User.create(frank);

            final String LEARNER_TYPE_A = Learner.LEARNER_TYPES.get(0);
            final String LEARNER_TYPE_B = Learner.LEARNER_TYPES.get(2);
            final String LEARNER_TYPE_C = Learner.LEARNER_TYPES.get(3);

            final String LEARNER_TYPES_A = LEARNER_TYPE_A;
            final String LEARNER_TYPES_AB = LEARNER_TYPE_A + "," + LEARNER_TYPE_B;
            final String LEARNER_TYPES_BC = LEARNER_TYPE_C + "," + LEARNER_TYPE_B;
            final String LEARNER_TYPES_ABC = LEARNER_TYPES_A + "," + LEARNER_TYPE_B + "," + LEARNER_TYPE_C;

            Ebean.save(new Learner("harry@cat.meow", "Harry", "Cat", bahb.email, LEARNER_TYPE_A));
            Ebean.save(new Learner("ooooo@octopus.noise", "Monkey", "Octopus", bahb.email, LEARNER_TYPE_B));
            Ebean.save(new Learner("fire@Ispit.it", "Peyton", "Manning", frank.email, LEARNER_TYPE_C));
            
            
            // today    
            Calendar date = new GregorianCalendar();
            // reset hour, minutes, seconds and millis
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            
            Ebean.save(new Session("1", "Emory Clinic", date.getTime(), "Dr. Payne", true, LEARNER_TYPES_A));
            Ebean.save(new Session("2", "Grady Clinic", date.getTime(), "Dr. Grey", false, LEARNER_TYPES_AB));

            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("3", "Grady Clinic", date.getTime(), "Dr. Professor Patrick", true, LEARNER_TYPES_BC));
            Ebean.save(new Session("4", "Another Clinic", date.getTime(), "Dr. Cox", false, LEARNER_TYPES_ABC));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("5", "Yet Another Clinic", date.getTime(), "Dr. McDreamy", true, LEARNER_TYPES_A));
            Ebean.save(new Session("6", "Emory Clinic", date.getTime(), "Dr. Howser", false, LEARNER_TYPES_AB));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("7", "Sacred Heart Clinic", date.getTime(), "Dr. Horrible", true, LEARNER_TYPES_ABC));
            Ebean.save(new Session("8", "90210 Clinic", date.getTime(), "Dr. Doctor", false, LEARNER_TYPES_BC));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("9", "Emory Clinic", date.getTime(), "Dr. Dorian", true, LEARNER_TYPES_A));
            Ebean.save(new Session("10", "Grady Clinic", date.getTime(), "Dr. House", false, LEARNER_TYPES_AB));
            
            ScheduleTemplate scheduleTemp = new ScheduleTemplate("subi1", "subi");
            
            for (int week = 1; week<4; week++){
                for(int day = 0; day<5; day++){
                    String name = "Week" + week + "Day" + day;
                    SessionTemplate sessionTempAM = new SessionTemplate("Emory", name + "AM", week, day, true);
                    SessionTemplate sessionTempPM = new SessionTemplate("VA", name + "PM", week, day, false);
                    scheduleTemp.addSession(sessionTempAM);
                    scheduleTemp.addSession(sessionTempPM);
                }
            }
            scheduleTemp.save();
        }

        ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Json.setObjectMapper(mapper);

        SessionThawingJob thawJob = new SessionThawingJob(Duration.create(10, TimeUnit.MINUTES));
        thawJob.schedule(Duration.create(0, TimeUnit.MILLISECONDS));
    }
}
