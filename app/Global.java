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
            Ebean.save(new User("bob@gmail.com", "secret", "Bob", false));
            Ebean.save(new User("sharon@gmail.com", "kitty", "Sharon", true));
            Ebean.save(new User("frank@gmail.com", "allyourbase", "Frank", false));
        }
    }
}
