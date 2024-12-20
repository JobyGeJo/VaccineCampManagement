package org.myapplication.modules;

import org.myapplication.database.DataBaseConnection;
import org.myapplication.database.QueryBuilder;
import org.myapplication.enumerate.Status;
import org.myapplication.exceptions.DataBaseException;
import org.myapplication.exceptions.InvalidRequestException;
import org.myapplication.models.VaccineModel;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class InventoryModule {

    public static VaccineModel[] getStock(int campId) {
        try (DataBaseConnection db = new DataBaseConnection()){
            return getStock(campId, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static VaccineModel[] getStock(int campId, DataBaseConnection db) throws DataBaseException {

        db.setQuery(
                new QueryBuilder()
                        .select("inventory",
                                "vaccine_id", "vaccine_name", "SUM(stock) AS stock")
                        .where("expiry_date > CURRENT_DATE")
                        .where("camp_id = ?")
                        .groupby("vaccine_id"),

                campId
        );

        try (ResultSet rs = db.executeQuery()) {

            ArrayList<VaccineModel> vaccines = new ArrayList<>();
            VaccineModel vaccine;

            while (rs.next()) {
                vaccine = new VaccineModel();

                vaccine.setVaccineId(rs.getInt("vaccine_id"));
                vaccine.setVaccineName(rs.getString("vaccine_name"));
                vaccine.setStock(rs.getInt("stock"));

                vaccines.add(vaccine);
            }

            return vaccines.toArray(new VaccineModel[0]);

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    public static int getVaccineStock(int campId, int vaccineId) {
        try (DataBaseConnection db = new DataBaseConnection()){
            return getVaccineStock(campId, vaccineId, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static int getVaccineStock(int campId, int vaccineId, DataBaseConnection db) throws DataBaseException {

        db.setQuery(
                new QueryBuilder()
                        .select("inventory", "SUM(stock) AS stock")
                        .where("expiry_date > CURRENT_DATE")
                        .where("camp_id = ?")
                        .where("vaccine_id = ?"),

                campId,
                vaccineId
        );

        try (ResultSet rs = db.executeQuery()) {

            if (!rs.next()) {
                throw new DataBaseException("Vaccine Not Found");
            }

            return rs.getInt("stock");

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    public static int getAvailableStock(int campId, int vaccineId) {
        try (DataBaseConnection db = new DataBaseConnection()){
            return getAvaiableStock(campId, vaccineId, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static int getAvaiableStock(int campId, int vaccineId, DataBaseConnection db) throws DataBaseException {
        db.setQuery(
                new QueryBuilder()
                    .select()
                        .openGroup()
                        .write(
                            new QueryBuilder()
                                .select("inventory", "SUM(stock)")
                                .where("camp_id = ?")
                                .where("vaccine_id = ?")
                                .where("expiry_date >= CURRENT_DATE")
                        )
                        .closeGroup()
                        .write("-")
                        .openGroup()
                        .write(
                            new QueryBuilder()
                                .select("appointments", "COUNT(vaccine_id)")
                                .where("camp_id = ?")
                                .where("vaccine_id = ?")
                                .where("status = ?")
                                .where("date_of_vaccination >= CURRENT_DATE")
                        )
                        .closeGroup("available_stock"),

                campId, vaccineId,
                campId, vaccineId,
                Status.PENDING
        );

        try (ResultSet avaialbeStock = db.executeQuery()) {
            if (!avaialbeStock.next()) {
                throw new DataBaseException("Vaccine Not Found");
            }

            return avaialbeStock.getInt("available_stock");

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }

    }

    public static void useOneVaccine(int campId, int vaccineId) {
        try (DataBaseConnection db = new DataBaseConnection()){
            useOneVaccine(campId, vaccineId, db);
        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void useOneVaccine(int campId, int vaccineId, DataBaseConnection db) throws DataBaseException {
        db.setQuery(
            new QueryBuilder()
                .update("inventory")
                .set("stock", "stock - 1")
                .where("batch_id = ")
                .openGroup()
                .write(
                    new QueryBuilder()
                        .select("inventory","batch_id")
                        .where("camp_id = ?")
                        .and("vaccine_id = ?")
                        .and("expiry_date >= CURRENT_DATE")
                        .and("stock > 0")
                        .orderby("expiry_date")
                        .limit(1)
                ).closeGroup(),

            campId,
            vaccineId
        );

        db.beginTransaction();

        if (db.executeUpdate() != 1) {
            db.rollbackTransaction();
            throw new DataBaseException("Vaccine or Camp Not Found");
        }

        db.commitTransaction();
    }

    public static void addStock(int campId, int vaccineId, int stock, String date) {
        try {
            addStock(campId, vaccineId, stock, Date.valueOf(date));
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void addStock(int campId, int vaccineId, int stock, Date expiryDate) {
        try (DataBaseConnection db = new DataBaseConnection()) {

            db.setQuery(
                    new QueryBuilder()
                            .insertInto("inventory",
                                    "camp_id", "vaccine_id", "stock", "expiry_date"),

                    campId, vaccineId, stock, expiryDate
            );

            db.beginTransaction();

            if (db.executeUpdate() != 1) {
                db.rollbackTransaction();
                throw new DataBaseException("Something went wrong");
            }

            db.commitTransaction();

        } catch (DataBaseException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void main(String[] args) throws DataBaseException {
        useOneVaccine(2, 1);
    }

}
