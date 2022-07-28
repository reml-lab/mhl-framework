import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserManager {
    
    Connection connection = null;
    String dbPath = null;
    
    public UserManager(String dbPath){
        try{
            // create a database connection
            this.dbPath     = dbPath ;
            this.connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbPath);
        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
    }
    
    
    public boolean isKnownUser(String research_token){
        try{
            Statement statement = this.connection.createStatement();
            statement.setQueryTimeout(1);  // set timeout to 1 sec.
            
            String query  = "select * from usersapp_userdetails where research_token=\""+research_token + "\"";
            ResultSet rs = statement.executeQuery(query);
            return rs.next();
        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
        return false;
            
    }
    
    public String getBadgeID(String research_token){
        try{
            Statement statement = this.connection.createStatement();
            statement.setQueryTimeout(1);  // set timeout to 1 sec.
            
            String query  = "select * from usersapp_userdetails where research_token=\""+research_token + "\"";
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            return rs.getString("badge_id");
            
        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
        return null;
    }
    
    
    public int getConfigVersion(String research_token){
        try{
            Statement statement = this.connection.createStatement();
            statement.setQueryTimeout(1);  // set timeout to 1 sec.
            
            String query  = "select * from usersapp_userdetails where research_token=\""+research_token + "\"";
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            return rs.getInt("config_version");
        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
        return -1;        
    }
    
    public JSONObject getConfig(String research_token){
        
        try{
            Statement statement = this.connection.createStatement();
            statement.setQueryTimeout(1);  // set timeout to 1 sec.
            
            String query  = "select * from usersapp_userdetails where research_token=\""+research_token + "\"";
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            
            JSONObject config = new JSONObject();
            config.put("type","config");
            config.put("config-version",rs.getInt("config_version"));
            config.put("study-name",rs.getString("study_name"));
            config.put("research-token",rs.getString("research_token"));
            config.put("badge-id",rs.getString("badge_id"));
            config.put("start-date",rs.getString("study_start_date"));
            config.put("stop-date",rs.getString("study_end_date"));
               
            return config;

        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
        return null;   
    }
    
    
    public void showUsers(){
    
        try{
            // create a database connection
            Statement statement = this.connection.createStatement();
            statement.setQueryTimeout(1);  // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("select * from usersapp_userdetails");
            while(rs.next()){
                // read the result set
                System.out.println("id = " + rs.getString("id") + " research_token = " + rs.getString("research_token") +  " badge_id = " + rs.getString("badge_id"));
                System.out.println();
              }
        }
        catch(SQLException e){
              // if the error message is "out of memory",
              // it probably means no database file is found
              System.err.println(e.getMessage());
        }
    }
    
    public void close(){
        try{
          if(connection != null)
            connection.close();
        }
        catch(SQLException e){
          // connection close failed.
          System.err.println(e.getMessage());
        }
    }
        
       
}