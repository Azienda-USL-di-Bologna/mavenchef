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
pecdocumentjoinjob={
        'appID':"rediscli",
        'jobID':"1",
        'jobList':[ {
            'jobN':"job1",
            'jobType':"ExecuteSql",
            'params': {
                    'user': username
                    'id_pec_message': id_pec_message
                    'guid_documento': guid_documento
                    'oggetto': oggetto
                    'numero_documento': numero_documento
                    'descrizione': descrizione
            }
        }],
        'returnQueue':"queueName"
    }


*/
public class PecDocumentJoinWorker implements Chef {
//private static final String configFile=System.getProperty("ChefWorker.configFile","chefWorker.properties");
private static final Logger log = LogManager.getLogger(PecDocumentJoinWorker.class);

private enum TipoOperazione{
    INSERT, UPDATE, DELETE
};

    public PecDocumentJoinWorker() throws WorkerException{}
    
    @Override
    public JSONObject doWork(JSONObject in, int j, JSONArray otherin, File workDir, File tmpDir) throws WorkerException {
        Connection dbConn = null;
        PreparedStatement statement = null;
        
        try {
            
            
            String user = (String) in.get("user");
            String idPecMessage = (String) in.get("id_pec_message");
            String guidDocumento = (String) in.get("guid_documento");
            String oggetto = (String) in.get("oggetto");
            String numeroDocumento = (String) in.get("numero_documento");
            String descrizione = (String) in.get("descrizione");
            String allegatiAccettati = (String) in.get("allegati_accettati");
            String allegatiScartati = (String) in.get("allegati_scartati");
            String tipoDocumento = (String) in.get("tipo_documento");
            TipoOperazione tipoOperazione = TipoOperazione.valueOf((String) in.get("tipo_operazione"));

//            Properties config=Launcher.loadConfig(configFile);
            String databaseUrl = config.getProperty("database.url","jdbc:postgresql://gdml:5432/argo");
            String databasePassword = config.getProperty("database.password.[user]".replace("[user]", user),"la password");
            String tableName = config.getProperty("pecdocumentjoin.tablename", "bds_tools.pec_documenti");
            // conversioner id_pec_message
            int idPecMessageInt = Integer.parseInt(idPecMessage);
            
            String connectionUrl = "[url]?user=[user]&password=[password]";
            connectionUrl = connectionUrl.replace("[url]", databaseUrl);
            connectionUrl = connectionUrl.replace("[user]", user);
            connectionUrl = connectionUrl.replace("[password]", databasePassword);
            dbConn = DriverManager.getConnection(connectionUrl);
            
            String query = "";
            
            if(tipoOperazione==TipoOperazione.UPDATE){
                // effettua aggiornamento basandosi su guid
                
                String updateString = "id_pec_message=?, numero_documento=?";
                
                if(oggetto!= null){
                    updateString+=", oggetto=?";
                }
                if(descrizione!=null){
                    updateString+=", descrizione=?";
                }
                    
                if(allegatiAccettati!=null){
                    updateString+=", allegati_accettati=?";
                }
                if(allegatiScartati!=null){
                    updateString+=", allegati_scartati=?";
                }
                 if(tipoDocumento!=null){
                    updateString+=", tipo_documento=?";
                }
                
                query = "UPDATE " + tableName + " SET " + updateString + "WHERE id_pec_message = ?";
                
                
                
                statement = dbConn.prepareStatement(query);
                
                int indexQuery = 1;
                
                statement.setInt(indexQuery++, idPecMessageInt);
                statement.setString(indexQuery++, numeroDocumento);
                if(oggetto!=null){
                    statement.setString(indexQuery++, oggetto);
                }
                if(descrizione!=null){
                    statement.setString(indexQuery++, descrizione);
                }
                 if(allegatiAccettati!=null){
                    statement.setString(indexQuery++, allegatiAccettati);
                }
                if(allegatiScartati!=null){
                    statement.setString(indexQuery++, allegatiScartati);
                }
                if(tipoDocumento!=null){
                    statement.setString(indexQuery++, tipoDocumento);
                }
                statement.setInt(indexQuery++, idPecMessageInt);
                // esecuzione query
                
                
                System.out.println("Query: "+statement.toString());
                int res = statement.executeUpdate();
                
               
               
                
            }else if(tipoOperazione==TipoOperazione.INSERT){
                // effettua inserimento nuovo record
                
                String updateString = "(id_pec_message, guid_documento, numero_documento";
                String values = "(?, ?, ?";
                
                if(oggetto!=null){
                    updateString += ", oggetto";
                    values +=", ?";
                }
                
                if(descrizione!=null){
                    updateString += ", descrizione";
                    values +=", ?";
                }
                 if(allegatiAccettati!=null){
                    updateString += ", allegati_scartati";
                    values +=", ?";
                }
                if(allegatiScartati!=null){
                    updateString += ", allegati_accettati";
                    values +=", ?";
                }
                if(tipoDocumento!=null){
                    updateString += ", tipo_documento";
                    values +=", ?";
                }
                
                updateString += ")";
                values +=")";
                
                // query da eseguire
                query = "INSERT INTO " + tableName + updateString +" VALUES "+ values;
                
                statement = dbConn.prepareStatement(query);

                int indexQuery = 1;

                statement.setInt(indexQuery++, idPecMessageInt);
                statement.setString(indexQuery++, guidDocumento);
                statement.setString(indexQuery++, numeroDocumento);
                if(oggetto!=null){
                    statement.setString(indexQuery++, oggetto);
                }
                if(descrizione!=null){
                    statement.setString(indexQuery++, descrizione);
                }
                if(allegatiScartati!=null){
                    statement.setString(indexQuery++, allegatiScartati);
                }
                if(allegatiAccettati!=null){
                    statement.setString(indexQuery++, allegatiAccettati);
                }
                if(tipoDocumento!=null){
                    statement.setString(indexQuery++, tipoDocumento);
                }
                
                
                 System.out.println("Query: "+statement.toString());
                // esecuzione query
                int ret = statement.executeUpdate();
                
            }else if(tipoOperazione==TipoOperazione.DELETE){
                // cancella record
                
                query="DELETE FROM "+ tableName +" WHERE id_pec_message=?";
                               
                statement = dbConn.prepareStatement(query);
                statement.setInt(1, idPecMessageInt);
                
		int ris = statement.executeUpdate();
            }
            
            
            
            // quando il risultato non è previsto come output si ritorna un oggetto vuoto
            JSONObject res = new JSONObject();

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

    
    
    @Override
    public final String getJobType() {
        return PecDocumentJoinWorker.class.getSimpleName();
    }
}
