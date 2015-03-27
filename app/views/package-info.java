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
 * The two calendar views ({@link views.html.calendarAdmin} and {@link views.html.calendarCoordinator})
 * are unique in that they use AngularJS to display content.  Content for other views is supplied using
 * the default Scala template engine that comes with the Play! Framework.
 */
package views;