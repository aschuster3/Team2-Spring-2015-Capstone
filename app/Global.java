import com.avaje.ebean.Ebean;

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
            Ebean.save(new User("Bob", "Lob", "bob@gmail.com", "secret", false));
            Ebean.save(new User("Sharon", "Norahs", "sharon@gmail.com", "kitty", true));
            Ebean.save(new User("Frank", "Knarf", "frank@gmail.com", "allyourbase", false));
        }
    }
}
