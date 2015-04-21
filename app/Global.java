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

            User bahb = new User("Bob", "Roberson", "bob@gmail.com", "secret", false, "Emory Internal Medicine", "770-820-1063");
            User frank = new User("Frank", "Brady", "frank@gmail.com", "allyourbase", false, "Emory Allergy", "770-241-2206");
            User horatio = new User("Horatio", "Fernandez", "horatio@gmail.com", "vivalaresistance", false, "Emory Allergy", "770-867-5309");
            
            User.create(bahb);
            User.create(new User("Sharon", "Whey", "sharon@gmail.com", "kitty", true));
            User.create(frank);
            User.create(horatio);

            final String LEARNER_TYPE_A = Learner.LEARNER_TYPES.get(0);
            final String LEARNER_TYPE_B = Learner.LEARNER_TYPES.get(2);
            final String LEARNER_TYPE_C = Learner.LEARNER_TYPES.get(3);

            final String LEARNER_TYPES_A = LEARNER_TYPE_A;
            final String LEARNER_TYPES_AB = LEARNER_TYPE_A + "," + LEARNER_TYPE_B;
            final String LEARNER_TYPES_BC = LEARNER_TYPE_C + "," + LEARNER_TYPE_B;
            final String LEARNER_TYPES_ABC = LEARNER_TYPES_A + "," + LEARNER_TYPE_B + "," + LEARNER_TYPE_C;

            Ebean.save(new Learner("catty@emory.edu", "Catherine", "McBride", bahb.email, LEARNER_TYPE_A));
            Ebean.save(new Learner("marciabrady@emory.edu", "Marcia", "Brady", bahb.email, LEARNER_TYPE_B));
            Ebean.save(new Learner("football@emory.edu", "Peyton", "Manning", frank.email, LEARNER_TYPE_C));
            Ebean.save(new Learner("student@emory.edu", "Jeff", "Johnson", frank.email, LEARNER_TYPE_A));
            Ebean.save(new Learner("student2@emory.edu", "George", "Johnston", frank.email, LEARNER_TYPE_C));
            Ebean.save(new Learner("student3@emory.edu", "Judy", "Pringles", bahb.email, LEARNER_TYPE_A));
            Ebean.save(new Learner("student4@emory.edu", "Ginger", "Ale", horatio.email, LEARNER_TYPE_B));
            Ebean.save(new Learner("student5@emory.edu", "Pam", "Franklyn", bahb.email, LEARNER_TYPE_A));
            Ebean.save(new Learner("student6@emory.edu", "Nick", "Grimes", horatio.email, LEARNER_TYPE_C));
            Ebean.save(new Learner("student7@emory.edu", "Kishan", "Crumpler", frank.email, LEARNER_TYPE_A));
            
            
            // today    
            Calendar date = new GregorianCalendar();
            // reset hour, minutes, seconds and millis
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);

            
            Ebean.save(new Session("1", "Emory Clinic", date.getTime(), "Dr. Payne", true, LEARNER_TYPES_A));
            Ebean.save(new Session("2", "VA Clinic", date.getTime(), "Dr. Grey", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("88", "VA Clinic", date.getTime(), "Dr. Finch", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("99", "Grady Clinic", date.getTime(), "Dr. Carson", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("12", "VA Clinic", date.getTime(), "Dr. Judy", true, LEARNER_TYPE_C));

            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("3", "Grady Clinic", date.getTime(), "Dr. Professor Patrick", true, LEARNER_TYPES_BC));
            Ebean.save(new Session("4", "Emory Clinic", date.getTime(), "Dr. Cox", false, LEARNER_TYPES_ABC));
            Ebean.save(new Session("21", "VA Clinic", date.getTime(), "Dr. Skelton", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("22", "Emory Clinic", date.getTime(), "Dr. Brown", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("25", "Emory Clinic", date.getTime(), "Dr. Black", false, LEARNER_TYPES_AB));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("5", "VA Clinic", date.getTime(), "Dr. Schuster", true, LEARNER_TYPES_A));
            Ebean.save(new Session("6", "Emory Clinic", date.getTime(), "Dr. Howser", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("32", "Grady Clinic", date.getTime(), "Dr. Roberts", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("39", "Grady Clinic", date.getTime(), "Dr. Virgil", true, LEARNER_TYPES_BC));
            Ebean.save(new Session("41", "VA Clinic", date.getTime(), "Dr. O'Hara", false, LEARNER_TYPE_C));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("7", "Grady Clinic", date.getTime(), "Dr. Horrible", true, LEARNER_TYPES_ABC));
            Ebean.save(new Session("8", "VA Clinic", date.getTime(), "Dr. DeBruler", false, LEARNER_TYPES_BC));
            Ebean.save(new Session("112", "VA Clinic", date.getTime(), "Dr. Jade", true, LEARNER_TYPE_B));
            Ebean.save(new Session("221", "VA Clinic", date.getTime(), "Dr. Judge", true, LEARNER_TYPE_A));
            Ebean.save(new Session("2321", "VA Clinic", date.getTime(), "Dr. Samson", false, LEARNER_TYPES_AB));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("9", "Emory Clinic", date.getTime(), "Dr. Dorian", true, LEARNER_TYPES_A));
            Ebean.save(new Session("10", "Grady Clinic", date.getTime(), "Dr. House", false, LEARNER_TYPES_AB));
            
            // next month
            date.add(Calendar.MONTH, 1);
            
            Ebean.save(new Session("1234", "Emory Clinic", date.getTime(), "Dr. Pickles", true, LEARNER_TYPES_A));
            Ebean.save(new Session("5678", "Grady Clinic", date.getTime(), "Dr. Empanada", false, LEARNER_TYPES_AB));
            Ebean.save(new Session("234", "VA Clinic", date.getTime(), "Dr. Manuel", true, LEARNER_TYPES_A));
            Ebean.save(new Session("1515", "VA Clinic", date.getTime(), "Dr. Picasso", false, LEARNER_TYPE_C));
            
            // next day
            date.add(Calendar.DAY_OF_MONTH, 1);
            
            Ebean.save(new Session("435", "VA Clinic", date.getTime(), "Dr. Flowers", true, LEARNER_TYPES_A));
            Ebean.save(new Session("59", "Grady Clinic", date.getTime(), "Dr. Quesorito", false, LEARNER_TYPES_A));
            Ebean.save(new Session("1100", "Grady Clinic", date.getTime(), "Dr. Margarita", true, LEARNER_TYPES_A));
            Ebean.save(new Session("1111", "VA Clinic", date.getTime(), "Dr. Pizza", false, LEARNER_TYPE_C));
            
            
            ScheduleTemplate scheduleTemp = new ScheduleTemplate("Subi-1", LEARNER_TYPE_A);
            
            for (int week = 1; week<4; week++){
                for(int day = 1; day<5; day++){
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

        SessionThawingJob thawJob = new SessionThawingJob(Duration.create(1, TimeUnit.MINUTES));
        thawJob.schedule(Duration.create(0, TimeUnit.MILLISECONDS));
    }
}
