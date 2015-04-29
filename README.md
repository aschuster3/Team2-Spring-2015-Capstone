# Readme 

Dependencies include JDK version 7 or higher

We used the Play Framework to create our application. In order to run the application, download and install Activator, which can be found <a href=https://typesafe.com/activator/docs>here</a>.

If deploying a full version on a webserver and have a custom admin account, go to app/Global.java and change the 
starting account.  When first deploying the application, it will be blank except for one Admin user.

Once Activator is installed:

1. Go to the root directory of the Dermatology Scheduling application
2. Type "activator"
3. Type "test" to see that the tests pass
4. Type "run" and go to localhost:9000 to run the application To sign in as a coordinator, use the following credentials to sign in as an admin (Definitely change this when deploying the full version):
		email = admin@emory.edu
		password = secret


To register as a coordinator:

1. Click "Don't have an account? Sign up" on the Login Screen and enter a name, email and department
2. Your information will have to be approved by an admin before you can continue***
3. Once the admin aporoves you as a coordinator, you will receive an email with a link. Use the url to create a password.
4. Use your newly created account to sign in as a coordinator.


***To approve a user, sign in as an admin and navigate to the "Coordinators" page. There is a list of coordinators pending approval. The admin can either approve or deny a coordinator. 

To add new learners as a coordinator:

1. From the coordinator dashboard, click on the "Students" tab in the navigation bar. 
2. Select "Add Student"
3. Fill in all fields and click "Submit"


The email address from which all app related emails are sent from is set in `app/util/Tags.java` under the variable titled ADMIN_EMAIL.  Change this to change the email "from" field in all emails.

User documentation is located in the Support tab of both the Coordinator and Admin dashboards.

Developer documentation is located in the docs/ folder.  Open a file in the browser to navigate the documentation.