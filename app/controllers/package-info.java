/**
 * The controllers package contains two kinds of classes to handle client requests.
 * <p>
 *     <h3>Controller Classes</h3>
 *     <p>Controller methods handle actions according to the conf/routes file.</p>
 *     <p>Controller classes named after classes in the {@link models} package
 *     are concerned with general CRUD operations:
 *     <ul>
 *         <li>{@link controllers.LearnerController}</li>
 *         <li>{@link controllers.SessionController}</li>
 *         <li>{@link controllers.TemplateController}</li>
 *     </ul>
 *     </p>
 *     <p>Controller classes named after user types
 *     ({@link controllers.AdminController} and {@link controllers.CoordinatorController})
 *     are concerned with actions that these users can perform via their respective views.
 *     </p>
 *     <p>The {@link controllers.Application} class handles actions related
 *     to creating/modifying user credentials.</p>
 * </p>
 * <p>
 *     <h3>Security Classes</h3>
 *     <p>Security classes ({@link controllers.Secured} and {@link controllers.SecuredAdminAction})
 *     restrict access to controller actions based on the kind of user that is logged in.
 *     </p>
 * </p>
 */
package controllers;