package ClientServerArchitureProject;

import java.sql.*;
import java.util.Arrays;

public class DBOperations {

    // -- objects to be used for user database access
    private Connection userConnection = null;
    private Statement userStatement = null;
    private ResultSet resultset = null;

    private final String usertable = "usertable";
    // -- root/admin
    // -- connect to the world database
    // -- this is the connector to the database, default port is 3306
    //    ApplicationData and CSC335 are schemas (databases) I created using the MySQL workbench
    private String userdatabaseURL = "jdbc:mysql://localhost:3306/csc335?useSSL=false";

    // -- this is the username/password, created during installation and in MySQL Workbench
    //    When you add a user make sure you give them the appropriate Administrative Roles
    //    (DBA sets all which works fine)
    private String user = "root";
    private String password = "022505Ollie#25";

    public DBOperations() {
        String sqlcmd;

        // -- first try the user database
        try {
            // -- make the connection to the database
            //    performs functionality of SQL: use CSC335;
            userConnection = DriverManager.getConnection(userdatabaseURL, user, password);

            // -- These will be used to send queries to the database
            userStatement = userConnection.createStatement();

            // -- simple SQL strings as they would be typed into the workbench
            sqlcmd = "SELECT VERSION()";
            resultset = userStatement.executeQuery(sqlcmd);

            if (resultset.next()) {
                System.out.println("MySQL Version: " + resultset.getString(1));
            }


        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }

    }


    public boolean checkUsername(String username) throws SQLException {
        boolean real = false;
        String sqlcmd = "select username from usertable;";
        resultset = userStatement.executeQuery(sqlcmd);
        ResultSetMetaData rsmd = resultset.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                if(resultset.getString(i).equals(username)){
                    real = true;
                }
            }
        }
        return real;
    }

    private boolean checkPassword(String password) throws SQLException {
        boolean real = false;
        String sqlcmd = "select password from usertable;";
        resultset = userStatement.executeQuery(sqlcmd);
        ResultSetMetaData rsmd = resultset.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                if(resultset.getString(i).equals(password)){
                    real = true;
                }
            }
        }
        return real;
    }

    public int login(String username, String password) throws SQLException {
        int result = 0;
        int attempts = 0;
        PreparedStatement ps;
        if(checkUsername(username)){
            String sqlcmd = "select attempts from usertable where username = '"+ username + "';";
            resultset = userStatement.executeQuery(sqlcmd);
            ResultSetMetaData rsmd = resultset.getMetaData();
            int numberOfColumns = rsmd.getColumnCount();
            while (resultset.next()) {
                for (int i = 1; i <= numberOfColumns; i++) {
                    attempts = resultset.getInt(i);
                }
            }
            if (attempts >= 3){
                result = 1;
                return result;
            }
            if(checkPassword(password)){
                sqlcmd = "UPDATE usertable" + " SET loggedIn = '1' WHERE username = ?;";
                ps = userConnection.prepareStatement(sqlcmd);
                ps.setString(1, username);
                ps.executeUpdate();
                result = 2;
                return result;
            }else{
                attempts += 1;
                sqlcmd = "UPDATE usertable " + " SET attempts = ? " + " WHERE username = ?;";
                ps = userConnection.prepareStatement(sqlcmd);
                ps.setInt(1, attempts);
                ps.setString(2, username);
                ps.executeUpdate();
                if (attempts >= 3){
                    result = 1;
                    return result;
                }

            }
            result = 3;
        }else{
            result = 4;
        }
        return result;
    }

    public boolean logout(String username) throws SQLException {
        PreparedStatement ps;
        String sqlcmd = "UPDATE usertable" + " SET loggedIn = '0' WHERE username = ?;";
        ps = userConnection.prepareStatement(sqlcmd);
        ps.setString(1, username);
        ps.executeUpdate();
        return false;
    }

    public int newUserRegistration(String username, String password, String email) throws SQLException {
        int created = 5;
        if (checkUsername(username)){
            //System.out.println("Username already exists");
            return created;
        }
        //System.out.println("Adding user");
        try{
            String sqlcmd = "INSERT INTO `csc335`.`usertable` (`username`, `password`, `emailAddress`, `attempts`, `loggedIn`) " +
                    "VALUES (?, ?, ?, '0', '0');";
            PreparedStatement ps = userConnection.prepareStatement(sqlcmd);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.executeUpdate();
            created = 6;
        } catch (SQLException e) {
            return created;
        }
        return created;

    }

    public String[] getStats() throws SQLException {

        String[] stats = new String[5];
        String sqlcmd = "SELECT * FROM " + usertable + ";";
        resultset = userStatement.executeQuery(sqlcmd);

        ResultSetMetaData rsmd = resultset.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();

        int users = 0;
        int loggedIn = 0;
        while (resultset.next()) {
            // -- loop through the columns of the ResultSet
            for (int i = 1; i <= numberOfColumns; ++i) {
                if(i == 5) {
                    if(resultset.getString(i).equals("1")){
                        loggedIn += 1;
                    }
                }
            }
            users += 1;
        }

        stats[0] = "Register users: "+ users;
        stats[1] = "Logged in users: "+ loggedIn;

        sqlcmd = "select username from usertable where loggedIn = 1;";
        resultset = userStatement.executeQuery(sqlcmd);
        rsmd = resultset.getMetaData();
        numberOfColumns = rsmd.getColumnCount();
        stats[2] = "Logged in users: ";
        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                stats[2] += resultset.getString(1) + " ";
            }
        }

        sqlcmd = "select username from usertable where attempts = 3;";
        resultset = userStatement.executeQuery(sqlcmd);
        rsmd = resultset.getMetaData();
        numberOfColumns = rsmd.getColumnCount();
        stats[3] = "Locked out users: ";
        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                stats[3] += resultset.getString(1) + " ";

            }
        }
        return stats;
    }

    public String getPassword(String username) throws SQLException {
        String password = "";
        String sqlcmd = "SELECT password FROM usertable where username = '" + username + "'";
        resultset = userStatement.executeQuery(sqlcmd);
        ResultSetMetaData rsmd = resultset.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                password = resultset.getString(i);
            }
        }
        return password;
    }

    public String getEmail(String username) throws SQLException {
        String email = "";
        String sqlcmd = "SELECT emailAddress FROM usertable where username = '" + username + "'";
        resultset = userStatement.executeQuery(sqlcmd);
        ResultSetMetaData rsmd = resultset.getMetaData();
        int numberOfColumns = rsmd.getColumnCount();
        while (resultset.next()) {
            for (int i = 1; i <= numberOfColumns; i++) {
                email = resultset.getString(i);
            }
        }
        return email;
    }

    public void resetLoginAttempts(String username) throws SQLException {
        String sqlcmd = "UPDATE usertable SET attempts = 0 WHERE username = ?;";
        PreparedStatement ps = userConnection.prepareStatement(sqlcmd);
        ps.setString(1, username);
        ps.executeUpdate();
    }




    public static void main(String[] args) {

        DBOperations dbc = new DBOperations();
        try{
            System.out.println(Arrays.toString(dbc.getStats()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
