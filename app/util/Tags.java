package util;


public class Tags {

    /**
     * The email address that all emails will appear to be sent from.
     */
    public static final String ADMIN_EMAIL = "admin@emory.edu";

    /**
     * Primary use of this variable is to provide correct URLs for links in emails.
     *
     * For local development, use
     *   "http://localhost:9000"
     *
     * For production, use the domain name for the site, and omit the trailing slash.
     *   Ex:  "http://www.emorydermatologyrotation.com"
     */
    public static final String SITE_BASE_URL = "http://localhost:9000";

    public static final String EMAIL_SUBJECT_RESET_PASSWORD = "Emory Dermatology Rotation - Reset Password";

    public static final String EMAIL_SUBJECT_COORDINATOR_APPROVAL = "Approval to the Emory Dermatology Rotation Application";

    public static final String EMAIL_SUBJECT_LEARNER_SCHEDULE = "Welcome to the Emory Dermatology Rotation";

    public static final String EMAIL_SUBJECT_SESSION_CANCELLED = "Emory Dermatology Rotation - Clinic Cancellation";
}