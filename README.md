# Readme 

Dependencies include JDK version 7 or higher

We used the Play Framework to create our application. In order to run the application, download and install Activator, which can be found <a href=https://typesafe.com/activator/docs>here</a>.

Once Activator is installed:

1. Go to the root directory of the Dermatology Scheduling application
2. Type "activator"
3. Type "test" to see that the tests pass
4. Type "run" and go to localhost:9000 to run the application To sign in as a coordinator, use the following credentials:
		email = bob@gmail.com
		password = secret
To sign in as an admin, use the following credentials:
		email = sharon@gmail.com
		password = kitty


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

To create new sessions/events as an admin:

1. Go to localhost:9000/sessions
2. Fill in the fields and click "Create". All fields are mandatory. ID should be an integer. All IDs must be unique.
