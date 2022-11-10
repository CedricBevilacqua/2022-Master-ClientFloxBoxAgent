package Commander;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Classe permettant de déclencher des actions sur le serveur distant en envoyant des requêtes sur l'API.
 *
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */
public class DistCommander {
	
	private String username;
	private String password;
	private String name;
	protected String tree;
	
	/**
	 * Constructeur initialisant les attributs requis et créant au besoin le dossier de la corbeille
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param username : Nom d'utilisateur du serveur
	 * @param password : Mot de passe du serveur
	 * @param name : Nom du serveur
	 */
	public DistCommander(String username, String password, String name) {
    	this.username = username;
    	this.password = password;
    	this.name = name;
    	try {
			AddFolder(name + "/deleted");
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/**
	 * Envoi un fichier sur le serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param pathFrom : Adresse du fichier à envoyer
	 * @param pathTo : Adresse où stocker le fichier en ligne
	 */
	public void AddFile(String pathFrom, String pathTo) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.post("http://localhost:8080/v1/servers/" + pathTo)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .field("file", new File(pathFrom))
		  .asString();
		//String answer = response.getBody();
		//System.out.println("Send: " + "http://localhost:8080/v1/servers/" + pathTo);
    }
	
	/**
	 * Crée un dossier sur le serveur
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param path : Emplacement du dossier sur le serveur
	 */
	public void AddFolder(String path) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.post("http://localhost:8080/v1/servers/" + path)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .header("Content-Type", "application/x-www-form-urlencoded")
		  .field(" ", "")
		  .asString();
		//String answer = response.getBody();
		//System.out.println("Folder: " + "http://localhost:8080/v1/servers/" + path);
    }
	
	/**
	 * Télécharge un fichier depuis le serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param distPath : Adresse du fichier sur le serveur
	 * @param localToPut : Adresse locale où enregistrer le fichier
	 */
	public void GetFile(String distPath, String localToPut) throws UnirestException, IOException {
    	//Réception du fichier
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.get("http://localhost:8080/v1/servers/download/" + name + distPath)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .asString();
		//Ecriture du fichier
		File file = new File(localToPut + "/" + name + distPath);
		InputStream input = response.getRawBody();
		FileOutputStream target = new FileOutputStream(file);
		input.transferTo(target);
		//System.out.println("Receive: " + localToPut + "/" + name + distPath);
    }
	
	/**
	 * Déplace un fichier à la corbeille.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param path : Adresse du fichier à mettre à la corbeille
	 */
	public void MoveTrash(String path) throws UnirestException {
		String deleteLocation = "/deleted/" + path;
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.put("http://localhost:8080/v1/servers/" + name + path)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .header("Content-Type", "application/x-www-form-urlencoded")
		  .field("newName", deleteLocation)
		  .asString();
		//System.out.println("Trash: " + "http://localhost:8080/v1/servers/" + name + path);
    }
	
	/**
	 * Supprime un fichier du serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param path : Adresse du fichier à supprimer
	 */
	public void Delete(String path) throws UnirestException {
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.delete("http://localhost:8080/v1/servers/" + name + path)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .asString();
		//System.out.println("Delete: " + "http://localhost:8080/v1/servers/" + name + path);
	}
	
	/**
	 * Méthode permettant de lancer la génération de l'arborescence du serveur et de le retourner.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Liste des éléments et sous listes de l'arborescence du serveur
	 */
	public List<Object> GetTable() throws JSONException, ParseException, UnirestException {
		List<String> pathList = new ArrayList<String>();
		pathList.add("");
		return Arborescence("", pathList);
    }
	
	/**
	 * Méthode recursive de parcours en profondeur générant l'arborescence du serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param upName : Nom du dossier actuel, utilisé par la récursion
	 * @param path : Liste des dossiers dans lequel on se trouve pour obtenir l'adresse actuelle
	 * @return Liste des éléments et sous listes de l'arborescence du serveur
	 */
	private List<Object> Arborescence(String upName, List<String> path) throws JSONException, ParseException, UnirestException {
		List<Object> arborescence = new ArrayList<Object>();
		arborescence.add(upName); //Nom du dossier
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.get("http://localhost:8080/v1/servers/" + this.name + GetPath(path))
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .asString();
		String fileJson = response.getBody();
		if(!fileJson.equals("[]")) {
			fileJson = fileJson.replace("}, {", "}<>{");
			String jsonByFile[] = fileJson.split("<>");
			jsonByFile[0] = jsonByFile[0].substring(1, jsonByFile[0].length());
			jsonByFile[jsonByFile.length - 1] = jsonByFile[jsonByFile.length - 1].substring(0, jsonByFile[jsonByFile.length - 1].length() - 1);
			for(String json : jsonByFile) {
				JSONObject parsedJson = new JSONObject(json);
				if(!parsedJson.getString("name").equals("deleted")) { //On exclue la corbeille de la synchronisation
					if(parsedJson.getString("type").equals("Directory")) { //Gestion d'un nouveau dossier suivi d'un appel récursif
						path.add(parsedJson.getString("name"));
						arborescence.add(Arborescence(parsedJson.getString("name"), path));
						path.remove(path.size() - 1);
					} else { //Gestion d'un fichier
						arborescence.add(new Pair<String, Long>(parsedJson.getString("name"), Long.parseLong(parsedJson.getString("date"))));
					}
				}
			}
		}
		return arborescence;
	}
	
	/**
	 * Obtient l'adresse complète à partir d'une liste de dossiers
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param path : Liste de chaque dossier dans lequel on se trouve
	 * @return Adresse complète
	 */
	protected String GetPath(List<String> path) {
		String completePath = "";
		for(String element : path) {
			completePath += element;
			completePath += "/";
		}
		completePath = completePath.substring(0, completePath.length()-1);
		return completePath;
	}
	
	/**
	 * Méthode permettant de déclencher la récupération des éléments supprimés et leur représentation sous forme d'arbre 
	 * textuel.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Chaine de caractères contenant la représentation textuelle de l'arborescence de la corbeille
	 */
	public String GetDeleted() throws JSONException, ParseException, UnirestException {
		List<String> pathList = new ArrayList<String>();
		pathList.add("/deleted");
		List<Object> trashArborescence = Arborescence("", pathList);
		CalcTree(trashArborescence, 0);
		return name + "\n" + tree.substring(4);
    }
	
	/**
	 * Méthode recursive parcourant en profondeur la corbeille du serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param arborescence : Arborescence à parcourir
	 * @param profondeur : Profondeur actuel du parcours, utilisé pour la recursion
	 * @return
	 */
	protected void CalcTree(List<Object> arborescence, int profondeur) {
		Boolean firstPass = true;
		for(Object element : arborescence) {
			if(!firstPass) {
				if(element instanceof Pair) {
					this.tree += GetLine(((Pair)element).getValue0().toString(), profondeur);
				} else if(element instanceof List<?>) {
					this.tree += GetLine(((List<Object>)element).toArray()[0].toString(), profondeur);
					CalcTree((List<Object>)element, profondeur + 1);
				}
			} else {
				firstPass = false;
			}
		}
	}
	
	/**
	 * Méthode générant la ligne suivante de la représentation textuelle.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param name : Nom de l'élément
	 * @param profondeur : Profondeur de l'élément
	 * @return Ligne de l'arbre textuel
	 */
	protected String GetLine(String name, int profondeur) {
		String line = "";
		for(int boucle = 0; boucle < profondeur; boucle++) {
			line += "|  ";
		}
		line += "|___" + name;
		line += "\n";
		return line;
	}

	/**
	 * Méthode restaurant un fichier de la corbeille.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param trashPath : Emplacement du fichier dans la corbeille
	 * @param putPath : Emplacement sur lequel restaurer le fichier
	 * @param location : Emplacement des fichiers locaux
	 */
	public void RestoreFile(String trashPath, String putPath, String location) throws UnirestException, IOException {
		//Réception du fichier de la corbeille
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response = Unirest.get("http://localhost:8080/v1/servers/download/" + name + trashPath)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .asString();
		//Ecriture du fichier en local
		File file = new File(location + "/" + name + putPath);
		InputStream input = response.getRawBody();
		FileOutputStream target = new FileOutputStream(file);
		input.transferTo(target);
		//Déplacement du fichier hors de la corbeille
		Unirest.setTimeouts(0, 0);
		HttpResponse<String> response2 = Unirest.put("http://localhost:8080/v1/servers/" + name + trashPath)
		  .header("username", Agent.CredentialHolder.GetUsername())
		  .header("password", Agent.CredentialHolder.GetPassword())
		  .header("FTPUsername", username)
		  .header("FTPPassword", password)
		  .header("Content-Type", "application/x-www-form-urlencoded")
		  .field("newName", putPath)
		  .asString();
		System.out.println("Restore: " + "http://localhost:8080/v1/servers/download/" + name + trashPath);
    }
    
}