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
            User bahb = new User("Bob", "Lob", "bob@gmail.com", "secret", false);
            User frank = new User("Frank", "Knarf", "frank@gmail.com", "allyourbase", false);
            
            Ebean.save(bahb);
            Ebean.save(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
            Ebean.save(frank);
            
            Ebean.save(new Learner("harry@cat.meow", "Harry", "Cat", bahb.email));
            Ebean.save(new Learner("ooooo@octopus.noise", "Monkey", "Octopus", bahb.email));
            Ebean.save(new Learner("fire@Ispit.it", "Peyton", "Manning", frank.email));
        }
    }
}
