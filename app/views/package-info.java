/**
 * Views fall under one of three categories:
 * <p>
 *     <ul>
 *         <li>Admin views</li>
 *         <li>Coordinator views</li>
 *         <li>Login-related views (registration, password setup, login etc.)</li>
 *     </ul>
 * </p>
 *
 * <p>
 * The two calendar views ({@link views.html.calendarAdmin} and {@link views.html.calendarCoordinator})
 * are unique in that they use AngularJS to display content.  Content for other views is supplied using
 * the default Scala template engine that comes with the Play! Framework.
 * </p>
 *
 * <p>
 * Scripts used for the Angular views can be found in the <code>public/javascripts</code> folder.
 * Some of the remaining views are also updated dynamically upon certain requests using AJAX calls.
 * Scripts used for these remaining views are inline so that we can take advantage of the Scala
 * template engine to generate correct URLs for AJAX calls.  All routes related to the Angular views
 * (templateUrls, $http requests, etc.) are hard-coded in the javascript files.
 * </p>
 *
 * <p>
 * An alternative to using inline scripts is to use Play! Framework's Javascript routes
 * (see <a href="http://stackoverflow.com/questions/11133059/play-2-x-how-to-make-an-ajax-request-with-a-common-button">here</a> for more information)
 * </p>
 */
package views;