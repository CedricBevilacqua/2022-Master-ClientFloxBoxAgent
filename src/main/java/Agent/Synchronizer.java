package Agent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import Commander.DistCommander;
import Commander.LocalCommander;

/**
 * Classe représentant la synchronisation de chaque serveur, elle contient tous les éléments liés à la gestion de la 
 * synchronisation locale et distante et les méthodes permettant de déclencher une action.
 *
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */
public class Synchronizer {
	
	private String name;
	private String path;
	private Comparer comparaison;
	private DistCommander commandeDistance;
	private LocalCommander commandeLocale;
	
	/**
	 * Constructeur initialisant et instanciant tous les éléments permettant de gérer le serveur et sa synchronisation
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param name : Nom du serveur
	 * @param path : Emplacement local du serveur
	 * @param username : Login du serveur
	 * @param password : Mot de passe du serveur
	 */
	public Synchronizer(String name, String path, String username, String password) throws UnirestException, JSONException, ParseException, IOException {
    	this.name = name;
    	this.path = path;
    	this.commandeDistance = new DistCommander(username, password, name);
    	this.commandeLocale = new LocalCommander(path + "/" + name);
    	this.comparaison = new Comparer(commandeDistance, commandeLocale);
    	Import();
    }
	
	/**
	 * Méthode démarrant la synchrnisation du serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 */
	public void Sync() throws UnirestException, JSONException, ParseException, IOException {
		comparaison.UpdateTables(commandeLocale, commandeDistance);
		//Synchronisation des nouveaux fichiers du dépôt local vers le dépôt distant
		Pair<List<String>, List<String>> newLocal = comparaison.CompareLocalNew();
		for(String file : newLocal.getValue1()) {
			commandeDistance.AddFolder(file.substring(path.length()+1, file.length()));
		}
		for(String file : newLocal.getValue0()) {
			commandeDistance.AddFile(file, file.substring(path.length()+1, file.length()));
		}
		//Synchronisation des fichiers supprimés du dépôt local avec le dépôt distant
		Pair<List<String>, List<String>> removedLocal = comparaison.CompareLocalDeletions();
		for(String folderPath : removedLocal.getValue1()) {
			String composedPath = name + "/deleted";
			Boolean ignoreFirstFlag = false;
			for(String folder : folderPath.split("/")) {
				if(ignoreFirstFlag) {
					composedPath += "/" + folder;
					commandeDistance.AddFolder(composedPath);
				} else {
					ignoreFirstFlag = true;
				}
			}
		}
		for(String file : removedLocal.getValue0()) {
			commandeDistance.MoveTrash(file);
		}
		for(String file : removedLocal.getValue0()) {
			commandeDistance.Delete(file);
		}
		Collections.reverse(removedLocal.getValue1());
		for(String folder : removedLocal.getValue1()) {
			commandeDistance.Delete(folder);
		}
		//Synchronisation des fichiers modifiés
		Pair<List<String>, List<String>> changes = comparaison.CompareChanges();
		for(String file : changes.getValue0()) {
			commandeDistance.AddFile(file, file.substring(path.length()+1, file.length()));
		}
		for(String file : changes.getValue1()) {
			commandeDistance.GetFile(file, path);
		}
    }
	
	/**
	 * Méthode visionnant la corbeille du serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 */
	public void Trash() throws JSONException, ParseException, UnirestException {
		System.out.println(commandeDistance.GetDeleted());
	}
	
	/**
	 * Méthode restorant un fichier depuis la corbeille du serveur si il correspond au serveur.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param name : Nom du serveur concerné
	 * @param inputAdress : Adresse du fichier à restaurer
	 * @param outputAdress : Adresse ou placer le fichier
	 */
	public void Restore(String name, String inputAdress, String outputAdress) throws UnirestException, IOException {
		if(name.equals(this.name)) {
			commandeDistance.RestoreFile("/deleted/" + inputAdress, "/" + outputAdress, path);
		}
	}
	
	/**
	 * Déclenche l'importation initiale de tous les fichiers du serveur en local.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 */
	private void Import() throws UnirestException, IOException {
		Pair<List<String>, List<String>> importTable = comparaison.CompareLocalDeletions();
		for(String file : importTable.getValue1()) {
			Path pathFolder = Paths.get(path + "/" + name + file);
			Files.createDirectory(pathFolder);
		}
		for(String file : importTable.getValue0()) {
			commandeDistance.GetFile(file, path);
		}
    }
    
}