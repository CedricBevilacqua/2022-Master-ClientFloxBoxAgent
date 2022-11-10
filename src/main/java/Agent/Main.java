package Agent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.javatuples.Pair;
import org.json.JSONException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Classe main exécutée au démarrage du programme.
 *
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */

public class Main {
	
	private static String syncLocation;
	private static List<Synchronizer> syncedServers = new ArrayList<Synchronizer>();
	
	/**
	 * Méthode main exécutée dès le démarrage du programme, paramètre à partir des arguments et lance l'opération
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param args : Tableau des arguments passés en paramètre au démarrage
	 */
    public static void main(String[] args) throws UnirestException, JSONException, ParseException, IOException, InterruptedException {
    	
        syncLocation = args[1];
        
        String accountUsername = "anonymous";
        if(args.length > 2) {
        	accountUsername = args[2];
        }
        String accountPassword = "anonymous";
        if(args.length > 3) {
        	accountPassword = args[3];
        }
        CredentialHolder.Set(accountUsername, accountPassword);
        
        String fileLocation = "serverlist.txt";
        if(args.length > 4) {
        	fileLocation = args[4];
        }
        Pair<Map<String, String>, Map<String, String>> credentials = ReadCredentials(fileLocation);
        ConnectAPI();
        if(args[0].equals("run")) {
        	SetSynchronizers(credentials.getValue0(), credentials.getValue1());
        	while(true) {
        		System.out.println("*** Starting synchronization ***");
        		for (int i=0; i<syncedServers.size(); i++) {
            		syncedServers.get(i).Sync();
            	}
        		System.out.println("*** End of synchronization ***");
            	Thread.sleep(30000);
        	}
        } else if(args[0].equals("trash")) {
        	SetSynchronizers(credentials.getValue0(), credentials.getValue1());
        	for (int i=0; i<syncedServers.size(); i++) {
        		syncedServers.get(i).Trash();
        	}
        } else if(args[0].equals("restore")) {
        	Scanner scanner = new Scanner(System.in);
        	System.out.println("Nom du serveur concerné : ");
        	String restoreName = scanner.nextLine();
        	System.out.println("Emplacement dans la corbeille du fichier à restaurer : ");
        	String restoreInput = scanner.nextLine();
        	System.out.println("Emplacement dans lequel enregistrer le fichier : ");
        	String restoreOutput = scanner.nextLine();
        	SetSynchronizers(credentials.getValue0(), credentials.getValue1());
        	for (int i=0; i<syncedServers.size(); i++) {
        		syncedServers.get(i).Restore(restoreName, restoreInput, restoreOutput);
        	}
        }
    }
    
    /**
     * Génère les objets de synchronisation pour chaque serveur enregistré et déclaré sur l'API.
     *
     * @author Cédric Bevilacqua, Semae Altinkaynak
     * @param usernameDict : Liste des login pour chaque serveur
     * @param passwordDict : List des mots de passe pour chaque serveur
     */
    public static void SetSynchronizers(Map<String, String> usernameDict, Map<String, String> passwordDict) throws UnirestException, JSONException, ParseException, IOException {
    	Unirest.setTimeouts(0, 0);
    	HttpResponse<String> response = Unirest.get("http://localhost:8080/v1/servers")
    	  .header("username", CredentialHolder.GetUsername())
    	  .header("password", CredentialHolder.GetPassword())
    	  .asString();
    	String[] servers = response.getBody().substring(16, response.getBody().length()-2).replace(" \"", "").replace("\"", "").split(",");
    	for(String server : servers) {
    		System.out.println("Added: " + server);
    		syncedServers.add(new Synchronizer(server, syncLocation, usernameDict.get(server), passwordDict.get(server)));
    	}
    }
    
    /**
     * Crée un compte sur l'API afin de pouvoir l'utiliser
     *
     * @author Cédric Bevilacqua, Semae Altinkaynak
     */
    private static void ConnectAPI() throws UnirestException {
    	Unirest.setTimeouts(0, 0);
    	HttpResponse<String> response = Unirest.post("http://localhost:8080/v1/auths")
    	  .header("Content-Type", "application/x-www-form-urlencoded")
    	  .field("username", CredentialHolder.GetUsername())
    	  .field("password", CredentialHolder.GetPassword())
    	  .asString();
    }
    
    /**
     * Lit le fichier contenant les identifiants de chaque serveur afin de récupérer toutes les informations contenues
     *
     * @author Cédric Bevilacqua, Semae Altinkaynak
     * @param filePath : Emplacement du fichier contenant les identifiants des serveurs
     * @return Deux dictionnaires contenant l'un l'identifiant l'autre le mot de passe associé au nom de chaque serveur
     */
    private static Pair<Map<String, String>, Map<String, String>> ReadCredentials(String filePath) throws FileNotFoundException {
    	 Map<String, String> usernameDict = new HashMap<String, String>();
    	 Map<String, String> passwordDict = new HashMap<String, String>();
    	 FileInputStream file = new FileInputStream(filePath);   
         Scanner scanner = new Scanner(file);  
         while(scanner.hasNextLine())
         {
        	 String name = scanner.nextLine();
        	 usernameDict.put(name, scanner.nextLine());
        	 passwordDict.put(name, scanner.nextLine());
         }
         scanner.close();    
         return new Pair<Map<String, String>, Map<String, String>>(usernameDict, passwordDict);
    }
    
}