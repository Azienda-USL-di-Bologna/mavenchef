package it.bologna.ausl.masterchef.workers;

import it.bologna.ausl.masterchef.Chef;
import static it.bologna.ausl.masterchef.Launcher.config;
import it.bologna.ausl.masterchef.errors.WorkerException;
import java.io.File;
import java.sql.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/*
 INPUT
updatebabeljob={
        'appID':"rediscli",
        'jobID':"1",
        'jobList':[ {
            'jobN':"job1",
            'jobType':"ExecuteSql",
            'params': {
                    'user': username
                    'query': query
            }
        }],
        'returnQueue':"queueName"
    }

OUTPUT
{
    'result': "risultato query"
} 
    * Cose da ricordare: 
    * result contiene il risultato se la query lo ritorna
    * result contiene il numero di righe interessate se la query non ritorna nessun risultato

*/
public class ExecuteSqlWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
private static final Logger log = LogManager.getLogger(ExecuteSqlWorker.class);

    public ExecuteSqlWorker() throws WorkerException{
        
    }
    
    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        Connection dbConn = null;
        PreparedStatement statement = null;
        try {
            String user = (String) in.get("user");
            String query = (String) in.get("query");
//            Properties config=Launcher.loadConfig(configFile);
            String executeSqlUrl = config.getProperty("database.url","jdbc:postgresql://gdml:5432/argo");
            String executeSqlPassword = config.getProperty("database.password.[user]".replace("[user]", user),"la password");
            
            String connectionUrl = "[url]?user=[user]&password=[password]";
            connectionUrl = connectionUrl.replace("[url]", executeSqlUrl);
            connectionUrl = connectionUrl.replace("[user]", user);
            connectionUrl = connectionUrl.replace("[password]", executeSqlPassword);
            dbConn = DriverManager.getConnection(connectionUrl);
            statement = dbConn.prepareStatement(query);
            ResultSet resSet = null;
            int rowsAffected = 0;
            boolean resSetReturned = statement.execute();
            if (resSetReturned)
                resSet = statement.getResultSet();
            else
                rowsAffected = statement.getUpdateCount();

            JSONObject res = new JSONObject();
            JSONArray queryRes = null;
            if (resSetReturned) {
                queryRes = resultSetToJSONArray(resSet);
            }
            else {
                queryRes = new JSONArray();
                JSONObject jso = new JSONObject();
                jso.put("rows_affected", rowsAffected);
                queryRes.add(jso);
            }
            res.put("result", queryRes);
            return res;
        }
        catch (SQLException ex) {
            throw new WorkerException(getJobType(), ex);
        }
        finally {
            try {
                statement.close();
                dbConn.close();
            }
            catch (Exception ex) {
            }
            
        }
    }

    private JSONArray resultSetToJSONArray(ResultSet rs) throws SQLException {
    JSONArray json = new JSONArray();
    ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
        int numColumns = rsmd.getColumnCount();
        JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++) {
            String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY) {
                    obj.put(column_name, rs.getArray(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT) {
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN) {
                    obj.put(column_name, rs.getBoolean(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB) {
                    obj.put(column_name, rs.getBlob(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE) {
                    obj.put(column_name, rs.getDouble(column_name)); 
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT) {
                    obj.put(column_name, rs.getFloat(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER) {
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR) {
                    obj.put(column_name, rs.getNString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR) {
                    obj.put(column_name, rs.getString(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT) {
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT) {
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE) {
                    obj.put(column_name, rs.getDate(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP) {
                    obj.put(column_name, rs.getTimestamp(column_name));   
                }
                else {
                    obj.put(column_name, rs.getObject(column_name));
                }
            }
        json.add(obj);
    }
    return json;
  }
    
    @Override
    public final String getJobType() {
        return "ExecuteSql";
    }
}
