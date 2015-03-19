import com.avaje.ebean.Ebean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jobs.RecurringSessionJob;
import models.Learner;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Json;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;


public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        // Check if the database is empty
        if (User.find.findRowCount() == 0) {
            Logger.info("Data added");

            User bahb = new User("Bob", "Lob", "bob@gmail.com", "secret", false, "Emory Internal Medicine");
            User frank = new User("Frank", "Knarf", "frank@gmail.com", "allyourbase", false, "Emory Allergy");
            
            Ebean.save(bahb);
            Ebean.save(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
            Ebean.save(frank);
            
            Ebean.save(new Learner("harry@cat.meow", "Harry", "Cat", bahb.email));
            Ebean.save(new Learner("ooooo@octopus.noise", "Monkey", "Octopus", bahb.email));
            Ebean.save(new Learner("fire@Ispit.it", "Peyton", "Manning", frank.email));
        }

        ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        Json.setObjectMapper(mapper);

        // TODO
        new RecurringSessionJob(Duration.create(7, TimeUnit.DAYS), 1)
                .schedule(Duration.create(0, TimeUnit.MILLISECONDS));
    }
}
