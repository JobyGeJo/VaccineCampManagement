/**
 * Provides classes and interfaces for managing user accounts,
 * authentication, and authorization.
 *
 * <p>This package includes:</p>
 * <ul>
 *   <li>{@link org.myapplication.controllers.AppointmentsController}: Represents a user in the system.</li>
 *   <li>{@link org.myapplication.controllers.CampsController}: Handles user authentication.</li>
 *   <li>{@link org.myapplication.controllers.UsersController}: Defines user roles for access control.</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 *     AuthService authService = new AuthService();
 *     User user = authService.authenticate("username", "password");
 *     if (user.hasRole(Role.ADMIN)) {
 *         // Perform admin-specific actions
 *     }
 * </pre>
 *
 * @since v1.0
 * @author Joby J
 */
package org.myapplication.controllers;