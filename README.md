# Readme

Dependencies include JDK version 7 or higher

We used the [Play Framework](https://www.playframework.com/) (v 2.3.7) to create our application. In order to run the application, download and install [Activator](https://typesafe.com/activator/docs).


## Getting Started: Running the Application Locally

Once Activator is installed:

0. Open your terminal
1. Go to the root directory of the Dermatology Scheduling application
2. Run Activator:<br />
`$ activator`
3. Run the tests to see that they pass:<br />
`[emory-dermatology-scheduler] $ test`
4. Run the application locally in development mode:<br />
`[emory-dermatology-scheduler] $ run`
5. In a browser, go to [http://localhost:9000](http://localhost:9000) to view the application.
6. Use the following credentials to sign in as an admin (Definitely change this when deploying the full version):<br/>
        Email: admin@emory.edu<br/>
        Password: secret


## Basic Usage

Note: Within the application, view the "Support" pages (accessible after logging in) to view detailed user documentation

### Register a Coordinator

1. Click "Don't have an account? Sign up" on the login screen and complete the form
2. Your information will have to be approved by an admin before you can continue***
3. Once the admin approves you as a coordinator, you will receive an email with a link. Use the url to create a password.
4. Use your newly created account to sign in as a coordinator.

***To approve a user, sign in as an admin and navigate to the "Coordinators" page. There is a list of coordinators pending approval. The admin can either approve or deny a coordinator. 


### Add a Learner

1. From the coordinator dashboard, click on the "Students" tab in the navigation bar. 
2. Select "Add Student"
3. Fill in all fields and click "Submit"


## Documentation

User documentation is located in the Support tab of both the Coordinator and Admin dashboards.

Developer documentation is located in the [`docs/`](docs/) folder.  Open a file in the browser to navigate the documentation.


## Before Deploying

### Change the Default Admin Account

By default, the application is initialized with a single admin user with the following information:

  * First Name: Admin
  * Last Name: User
  * Email: admin@emory.edu
  * Password: secret

Change this email and password by editing the appropriate fields in the file [`app/Global.java`].

### Add Admin Accounts

To add more admin accounts, edit [`app/Global.java`] and follow the existing convention for a new `User`.

[`app/Global.java`]: app/Global.java
[app/Global.java]: app/Global.java

### Configure Email Variables

The file [`app/util/Tags.java`] contains several variables to quickly configure the "from" and "subject" fields of emails sent from the system.  The most important of these to configure is the `ADMIN_EMAIL` field.

* `ADMIN_EMAIL`:  The email address from which all app-related emails are sent from (i.e. the "from" field in the email).
* `EMAIL_SUBJECT_RESET_PASSWORD`:  The subject line for emails that are sent to users when a password reset is requested.
* `EMAIL_SUBJECT_COORDINATOR_APPROVAL`:  The subject line for emails that are sent to coordinators after they are approved.
* `EMAIL_SUBJECT_LEARNER_SCHEDULE`:  The subject line for emails that are sent to learners when they receive their schedule.
* `EMAIL_SUBJECT_SESSION_CANCELLED`:  The subject line for emails that are sent to learners/coordinators when one of their sessions is cancelled.

### Configure the Site URL

The file [`app/util/Tags.java`] also contains a variable called `SITE_BASE_URL`.
It is important to configure this to be the full domain name that the site will have once it is deployed.
Make sure to omit trailing slashes.

[`app/util/Tags.java`]: app/util/Tags.java
[app/util/Tags.java]: app/util/Tags.java

### Setup SMTP Server Settings (for Sending Emails)

Our app uses the [Play Mailer](https://github.com/playframework/play-mailer) plugin to handle sending emails from the application.
Please refer to their documentation for configuring the settings properly.

This will require editing the "Email" section of the [`conf/application.conf`] file.

**Important: If this is not properly configured, or the default settings are left unchanged, the application will _not_ be able to send any emails out to users!**

### Setup Database Server Settings

Our app will require an SQL database (MySQL, PostgreSQL, etc.) to store data, so the application must be configured in order to properly communicate with the database server.
See the following resource for more information:

  * [Play Framework: Accessing an SQL Database](https://www.playframework.com/documentation/2.3.7/JavaDatabase)

This will require editing the "Database configuration" section of the [`conf/application.conf`] file.

**Important: By default, the application is configured to use an in-memory database.
While the application will appear to function correctly in this default configuration, this is not safe to use in production (because data will be lost if the application stops running for any reason)!
So, be sure to configure the application to use a proper database server (MySQL, PostgreSQL, etc.)**


## Deploying the Application

[Heroku](https://www.heroku.com/) is the suggested platform for deploying the application.

Resources for deploying to Heroku:

  * [Play Framework: Deploying to Heroku](https://www.playframework.com/documentation/2.3.7/ProductionHeroku)

Other resources which may (or may not) be helpful in deploying the application:

  * [Play Framework: Deploying to a Cloud Service](https://www.playframework.com/documentation/2.3.7/DeployingCloud)
    * contains links to instructions for deploying on other cloud sources
  * [Play Framework: Deploying Your Application](https://www.playframework.com/documentation/2.3.7/Production)
    * first of six pages from Play Framework's documentation for deploying an application
    * some of the information from these pages is likely irrelevant if you are using third party cloud service




[`conf/application.conf`]: conf/application.conf
[conf/application.conf]: conf/application.conf
