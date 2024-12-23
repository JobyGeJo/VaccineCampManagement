/**
 * This package provides Data Access Object (DAO) classes for interacting with
 * the database. These classes abstract database operations, allowing developers
 * to focus on business logic without worrying about the underlying database logic.
 *
 * <p>The following DAOs are included:</p>
 * <ul>
 *   <li>{@link org.myapplication.models.UserModel} - Handles the data of Users.</li>
 *   <li>{@link org.myapplication.models.AppointmentModel} - Handles the data of Appointments.</li>
 *   <li>{@link org.myapplication.models.CampModel} - Handles the data of camps.</li>
 *   <li>{@link org.myapplication.models.VaccineModel} - Handles the data of vaccines.</li>
 *   <li>{@link org.myapplication.models.JsonModel} - This model helps to handle json objects.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 *     UserModel user - new UserModel();
 *     user.setUsername(username); //validates the username and stores it.
 *     user.getUsername(); // Returns the username.
 * </pre>
 *
 * @since 1.0
 * @author Your Name
 */

package org.myapplication.models;