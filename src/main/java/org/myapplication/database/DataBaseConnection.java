package org.myapplication.database;

import org.myapplication.tools.ColoredOutput;
import org.myapplication.exceptions.DataBaseException;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.function.Function;

public class DataBaseConnection implements AutoCloseable {
    private Connection conn;
    private PreparedStatement stmt;

    public DataBaseConnection() throws DataBaseException {
        System.out.println();

        try {
            Class.forName("org.postgresql.Driver");
            String username = "joby-pt7692";
            String password = "root";
            String url = "jdbc:postgresql://localhost:5432/vaccine-camp";
            conn = DriverManager.getConnection(url, username, password);  // Direct connection (no pooling)
            ColoredOutput.println("green","Connected to PostgreSQL database successfully");
        } catch (ClassNotFoundException e) {
            throw new DataBaseException(e.getMessage());
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }


    @Override
    public void close() throws DataBaseException {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close(); // Returns connection back to the pool
            }
            ColoredOutput.println("green", "Connection closed successfully\n");
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    // Transaction management: Start a transaction
    public void beginTransaction() throws DataBaseException {
        try {
            if (conn != null && conn.getAutoCommit()) {
                conn.setAutoCommit(false);
                ColoredOutput.println("yellow", "Transaction started");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    // Transaction management: Commit changes
    public void commitTransaction() throws DataBaseException {
        try {
            if (conn != null) {
                conn.commit();
                ColoredOutput.println("yellow", "Transaction Commited");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    // Transaction management: Rollback changes
    public void rollbackTransaction() throws DataBaseException {
        try {
            if (conn != null) {
                conn.rollback();
                ColoredOutput.println("yellow", "Transaction Cancelled");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public void setQuery(QueryBuilder query, Object... params) throws DataBaseException {
        try {
            if (conn == null) {
                throw new DataBaseException("Not connected");
            }

            //noinspection SqlSourceToSinkFlow
            stmt = conn.prepareStatement(query.build());

            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Enum) {
                    stmt.setObject(i + 1, params[i].toString(), Types.OTHER);
                } else {
                    stmt.setObject(i + 1, params[i]); // Bind other types normally
                }
            }

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public void setQuery(Query query, Object... params) throws DataBaseException {
        try {
            if (conn == null) {
                throw new DataBaseException("Not connected");
            }

            //noinspection SqlSourceToSinkFlow
            stmt = conn.prepareStatement(query.getQuery());

            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof Enum) {
                    stmt.setObject(i + 1, params[i].toString(), Types.OTHER);
                } else {
                    stmt.setObject(i + 1, params[i]); // Bind other types normally
                }
            }

        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }


    // For update queries (INSERT, UPDATE, DELETE)
    public int executeUpdate() throws DataBaseException {
        try {
            if (stmt == null) {
                throw new DataBaseException("Query not set");
            }
            ColoredOutput.println("cyan", "Executing Update: " + stmt);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    // For SELECT queries
    public ResultSet executeQuery() throws DataBaseException {
        try {
            if (stmt == null) {
                throw new DataBaseException("Query not set");
            }
            ColoredOutput.println("cyan", "Executing query: " + stmt);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }
}
