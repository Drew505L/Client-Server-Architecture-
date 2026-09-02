package ClientServerArchitureProject;

import java.sql.SQLException;

public class User {
    private String username;

    private DBOperations dbc = new DBOperations();

    public User(String username){
        this.username = username;
    }

    public boolean logout() throws SQLException {
        return dbc.logout(getUsername());
    }

    private String getUsername(){
        return username;
    }

}
