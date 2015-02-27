import com.avaje.ebean.Ebean;

import models.Learner;
import models.User;
import play.Application;
import play.GlobalSettings;
import play.Logger;


public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        // Check if the database is empty
        if (User.find.findRowCount() == 0) {
            Logger.info("Data added");
            User bahb = new User("Bob", "Lob", "bob@gmail.com", "secret", false, "Emory Internal Medicine");
            
            Ebean.save(bahb);
            Ebean.save(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
            Ebean.save(new User("Frank", "Knarf", "frank@gmail.com", "allyourbase", false, "Emory Allergy"));
            
            Ebean.save(new Learner("Harry", "Cat", bahb));
            Ebean.save(new Learner("Monkey", "Octopus", bahb));
            Ebean.save(new Learner("Iamnot", "arapper", bahb));
        }
    }
}
