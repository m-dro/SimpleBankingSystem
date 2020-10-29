package banking;

import java.sql.*;

/**
 * Course: JetBrains Academy, Java Developer Track
 * Project: Simple Banking System
 * Purpose: A console-based program to simulate operations in a bank.
 *
 * DBManager is responsible for managing connections with database,
 * and all database operations (insert, update, delete) and querying.
 *
 * @author Mirek Drozd
 * @version 1.1
 */
public class DBManager {
    String db;

    /**
     * Reads database details from program arguments,
     * and passes these to a method that creates database table,
     * if it hasn't been created yet.
     *
     * @param args Program arguments with database details.
     */
    public void setup(String[] args) {
        getFileName(args);
        createNewTable(db);
    }

    /**
     * Reads name of database file from program arguments.
     *
     * @param args Program arguments with filename.
     * @return Name of the file.
     */
    public String getFileName(String[] args) {
        String fileName = "";
        try {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-fileName")) {
                    fileName = args[i+1];
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(fileName.isEmpty()) {
            System.out.println("DATABASE SETUP FAILED");
        } else {
            this.db = fileName;
        }
        return fileName;
    }

    /**
     * Creates database table for user accounts.
     *
     * @param db Name of the database file.
     */
    public void createNewTable(String db) {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS card (\n" +
                "        id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "        number  TEXT,\n" +
                "        pin     TEXT,\n" +
                "        balance INTEGER DEFAULT 0\n" +
                "      );";

        try (Connection conn = this.connect(db);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Establishes connection with the database.
     *
     * @param db Name of the database file.
     * @return Database connection.
     */
    private Connection connect(String db) {
        // SQLite connection string
        String url = "jdbc:sqlite:C:\\Users\\Mirek\\IdeaProjects\\Simple Banking System\\Simple Banking System\\task\\" + db;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    /**
     * Queries the database for card, based on card number.
     *
     * @param card Number of the card to find in the database.
     * @return ID of the card in the database.
     */
    public int selectIDByCard(String card){
        String sql = "SELECT id FROM card WHERE number = ?";
        int ID = 0;
        try (Connection conn = this.connect(db);
             PreparedStatement pstmt  = conn.prepareStatement(sql)){
            pstmt.setString(1, card);
            ResultSet rs    = pstmt.executeQuery();
            // loop through the result set
            while (rs.next()) {
                ID = rs.getInt("id");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return ID;
    }

    /**
     * Inserts new card into the database.
     *
     * @param number Card number.
     * @param pin Card's PIN.
     * @param balance Card's balance.
     */
    public void insert(String number, int pin, int balance) {
        String sql = "INSERT INTO card(number,pin,balance) VALUES(?,?,?)";

        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setInt(2, pin);
            pstmt.setInt(3, balance);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Updates card's balance.
     *
     * @param id Card's ID in the database.
     * @param newBalance Updated balance on the card.
     */
    public void update(int id, int newBalance) {
        String sql = "UPDATE card SET balance = ?"
                + "WHERE id = ?";

        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, newBalance);
            pstmt.setInt(2, id);
            // update
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Deletes card from the database.
     *
     * @param id Card's ID.
     */
    public void delete(int id) {
        String sql = "DELETE FROM card WHERE id = ?";
        System.out.println(sql);
        try (Connection conn = this.connect(db);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // set the corresponding param
            pstmt.setInt(1, id);
            // delete
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
