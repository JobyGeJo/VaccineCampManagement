package org.myapplication.modules;

import org.myapplication.database.*;
import org.myapplication.enumerate.Role;
import org.myapplication.exceptions.AuthenticationFailedException;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.JsonModel;
import org.myapplication.models.UserModel;
import org.myapplication.tools.Hashing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserModule {

    private final static Table table = new Table("users");

    enum UserColumns {

        USER_ID("user_id"),
        USERNAME("user_name"),
        PASSWORD("password"),
        AADHAR_NUMBER("aadhar_number"),
        FIRST_NAME("first_name"),
        LAST_NAME("last_name"),
        PHONE_NUMBER("phone_number"),
        DATE_OF_BIRTH("date_of_birth"),
        ROLE("role");

        private final Column column;

        UserColumns(String column) {
            this.column = new Column(column, table);
        }

        public Column getColumn() {
            return column;
        }

        public Column getColumn(String alias) {
            return Column.setAlias(column, alias);
        }

        public Column getColumn(String alias, Functions delimeter) {
            return Column.setAliasDelimiter(column, alias, delimeter);
        }
    }

    public static void registerUser(UserModel userDetails, String password) {
        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(
                    new QueryBuilder("safedose_v2")
                            .insertInto("users",
                                    "user_name", "aadhar_number", "password", "first_name",
                                    "last_name", "phone_number", "date_of_birth")
                            .returning("user_id"),

                    userDetails.getUsername(),
                    userDetails.getAadharNumber(),
                    Hashing.hashWithSaltAndPepper(password),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getPhoneNumber(),
                    userDetails.getDateOfBirth()
            );

            try (ResultSet rs = db.executeQuery()){
                if (!rs.next()) {
                    throw new DataBaseException("Something went wrong");
                }

                userDetails.setUserId(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // SQLState code for unique violation
                throw new InvalidRequestException(
                        "The given " +
                                (e.getMessage().contains("aadhar_number") ? "aadhar_number" :  "user_name") +
                                " is already in use"
                );
            }
            throw new InvalidRequestException(e.getMessage());
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static UserModel loginUser(String username, String password) throws DataBaseException, AuthenticationFailedException {

        Condition condition = new Condition(Operator.EQUALS, UserColumns.USERNAME.getColumn(), username);
        Query query = new Query(table);
        query.addCondition(condition);
        query.select();

        try (DataBaseConnection db = new DataBaseConnection()){
            new UserModel().setUsername(username);

            db.setQuery(query);

            try (ResultSet rs = db.executeQuery()) {

                if (!rs.next()) {
                    throw new DataBaseException("User not found");
                }

                if (Hashing.verifyPassword(password, rs.getString("password"))) {
                    return getUser(rs.getInt("user_id"), db);
                }

                throw new AuthenticationFailedException("Wrong password");

            }

        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }

    }

    public static UserModel getUser(int userId) throws DataBaseException {
        try (DataBaseConnection db = new DataBaseConnection()){
            return getUser(userId, db);
        }
    }

    public static UserModel getUser(int userId, DataBaseConnection db) throws DataBaseException {

        Query query = new Query(table);
        Condition condition = new Condition(Operator.EQUALS, UserColumns.USER_ID.getColumn(), userId);
        query.addCondition(condition);
        query.select();

        db.setQuery(query);

        return getUserModel(db);
    }

    private static UserModel getUserModel(DataBaseConnection db) throws DataBaseException {
        try (ResultSet rs = db.executeQuery();) {

            if (!rs.next()) {
                throw new DataBaseException("User not found");
            }
            UserModel user = new UserModel();
            return getUserModelFromResultSet(user, rs);

        } catch (SQLException e) {

            throw new DataBaseException(e.getMessage());
        }
    }

    private static UserModel getUserModelFromResultSet(UserModel user, ResultSet rs) throws SQLException {
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("user_name"));
        user.setAadharNumber(rs.getString("aadhar_number"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setDateOfBirth(rs.getString("date_of_birth"));
        user.setRole(Role.fromString(rs.getString("role")));
        return user;
    }

    public static UserModel fetchUser(String aadharNumber){
        Condition condition = new Condition(Operator.EQUALS, UserColumns.AADHAR_NUMBER.getColumn(), aadharNumber);
        Query query = new Query(table);
        query.addCondition(condition);
        query.select();
        try (DataBaseConnection db = new DataBaseConnection()) {
            db.setQuery(query);
            return getUserModel(db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static UserModel[] getAdmins() {
        ArrayList<UserModel> admins = new ArrayList<>();

        Condition condition = new Condition(Operator.IS_NOT_NULL, UserColumns.ROLE.getColumn());

        Query query = new Query(table);
        query.addCondition(condition);

        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(query);

            try (ResultSet rs = db.executeQuery()) {

                while (rs.next()) {
                    UserModel user = new UserModel();

                    getUserModelFromResultSet(user, rs);
                    user.setRole(Role.fromString(rs.getString("role")));

                    admins.add(user);
                }

            }

            return admins.toArray(new UserModel[0]);

        } catch (SQLException | DataBaseException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void updateUser(int userId, JsonModel newDetails) {
        QueryBuilder qb = new QueryBuilder("safedose_v2").update("users");
        String[] columns = newDetails.getKeys();

        if (columns.length == 0) {
            throw new InvalidRequestException("No Data to Change");
        }

        Object[] values = new Object[columns.length + 1];
        values[columns.length] = userId;

        int i = 0;

        for (i = 0; i < columns.length; i++) {

            qb.set(columns[i]);
            UserModel validator = new UserModel();

            switch (columns[i]){
                case "password":
                    values[i] = Hashing.hashWithSaltAndPepper((String) newDetails.get("password"));
                    break;

                case "date_of_birth":
                    validator.setDateOfBirth((String) newDetails.get("date_of_birth"));
                    values[i] = validator.getDateOfBirth();
                    break;

                case "first_name":
                    validator.setFirstName((String) newDetails.get("first_name"));
                    values[i] = validator.getFirstName();
                    break;

                case "last_name":
                    validator.setLastName((String) newDetails.get("last_name"));
                    values[i] = validator.getLastName();
                    break;

                case "phone_number":
                    validator.setPhoneNumber((String) newDetails.get("phone_number"));
                    values[i] = validator.getPhoneNumber();
                    break;

                case "aadhar_number":
                    validator.setAadharNumber((String) newDetails.get("aadhar_number"));
                    values[i] = validator.getAadharNumber();
                    break;

                case "user_name":
                    validator.setUsername((String) newDetails.get(columns[i]));
                    values[i] = validator.getUsername();
                    break;

                case "user_id":
                case "role":
                    throw new IllegalArgumentException("Restricted column to update: " + columns[i]);
                default:
                    throw new IllegalArgumentException("Unknown column to update: " + columns[i]);
            }
        }
        qb.where("user_id = ?");

        try (DataBaseConnection db = new DataBaseConnection()){

            db.beginTransaction();
            db.setQuery(qb, values);
            db.executeUpdate();

            db.commitTransaction();

        } catch (DataBaseException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (ClassCastException e) {
            throw new InvalidRequestException("Invalid Type Found: " + columns[i]);
        }
    }

    public static Role getCurrentRole(int userId) throws DataBaseException {
        try (DataBaseConnection db = new DataBaseConnection()){
            return getCurrentRole(userId, db);
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public static Role getCurrentRole(int userId, DataBaseConnection db) throws DataBaseException, SQLException {

        Query query = new Query(table);
        query.addCondition(new Condition(Operator.EQUALS, UserColumns.ROLE.getColumn(), userId));
        query.select();

        db.setQuery(query);

        ResultSet rs = db.executeQuery();
        if (rs.next()) {
            String roleString = rs.getString("role");
            return Role.fromString(roleString);
        }

        throw new DataBaseException("User not found or role is null.");
    }

    public static String upgradeUserRole(int toUserId, int fromUserId, Role fromUserRole) throws DataBaseException {

        if (toUserId == fromUserId) {
            throw new DataBaseException("Self Promotion Not Allowed");
        }

        try (DataBaseConnection db = new DataBaseConnection()) {
            Role newRole = Role.getNextRole(getCurrentRole(toUserId, db));

            if (newRole.getPriorityCode() > fromUserRole.getPriorityCode()) {
                throw new DataBaseException("You are not allowed to promote this user");
            }

            db.beginTransaction();
            changeRole(toUserId, newRole, db);

            if (newRole.equals(Role.OWNER)) {
                changeRole(fromUserId, Role.ADMIN, db);
            }

            db.commitTransaction();

            return newRole.toString();

        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }
    }

    public static String downgradeUserRole(int toUserId, int fromUserId, Role fromUserRole) throws DataBaseException {

        if (toUserId == fromUserId) {
            throw new DataBaseException("Can't update role by yourself");
        }

        try (DataBaseConnection db = new DataBaseConnection()) {
            Role newRole = Role.getPrevoiusRole(getCurrentRole(toUserId, db));

            if (fromUserRole.getPriorityCode() < 2) {
                throw new DataBaseException("You are not allowed to demote this user");
            }

            db.setQuery(
                    new QueryBuilder()
                            .update("users")
                            .set("role")
                            .where("user_id = ?"),

                    newRole,
                    toUserId
            );

            db.beginTransaction();

            db.beginTransaction();
            changeRole(toUserId, newRole, db);

            db.commitTransaction();

            if (newRole.equals(Role.NULL)) {
                return "User";
            }

            return newRole.toString();

        } catch (SQLException e) {
            throw new DataBaseException(e.getMessage());
        }

    }

    private static void changeRole(int userId, Role setRole, DataBaseConnection db) throws DataBaseException, SQLException {
        db.setQuery(
                new QueryBuilder()
                        .update("users")
                        .set("role")
                        .where("user_id = ?"),

                setRole,
                userId
        );

        db.executeUpdate();
    }

    public static void main(String[] args) throws DataBaseException, SQLException {
        System.out.println(getCurrentRole(1));
    }

}
