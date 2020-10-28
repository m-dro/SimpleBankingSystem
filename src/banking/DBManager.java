package banking;

import java.sql.*;

public class DBManager {
    String db;

    public void setup(String[] args) {
        db = args[1];
        createNewTable(db);
    }


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
        this.db = fileName;
        return fileName;
    }



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
