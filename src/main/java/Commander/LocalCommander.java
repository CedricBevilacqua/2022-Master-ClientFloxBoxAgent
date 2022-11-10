package Commander;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Classe gérant les intéractions sur les fichiers locaux.
 *
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */
public class LocalCommander {
	
	private String path;
	
	/**
	 * Constructeur initialisant le dossier du serveur en local et les attributs nécessaires.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param path : Adresse des fichiers locaux
	 */
	public LocalCommander(String path) {
    	this.path = path;
    	File servFolder = new File(path);
    	servFolder.mkdir();
    }
	
	/**
	 * Méthode permettant de générer puis d'obtenir l'arborescence des fichiers locaux.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Liste des fichiers et sous listes de l'arborescence
	 */
	public List<Object> GetTable() {
		List<String> pathList = new ArrayList<String>();
		pathList.add(path);
		return Arborescence(path, pathList);
    }
	
	/**
	 * Méthode recursive de parcours en profondeur générant l'arborescence local.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param upName : Nom du dossier actuel, utilisé par la récursion
	 * @param path : Liste des dossiers dans lequel on se trouve pour obtenir l'adresse actuelle
	 * @return Liste des éléments et sous listes de l'arborescence local
	 */
	private List<Object> Arborescence(String upName, List<String> path) {
		List<Object> arborescence = new ArrayList<Object>();
		arborescence.add(upName); //Nom du dossier
		File repertoire = new File(GetPath(path));
		File[] listedFiles = repertoire.listFiles();
		for(File element : listedFiles) {
			File fileElement = new File(GetPath(path)+ "/" + element.getName());
			if(fileElement.isDirectory()) { //Gestion d'un nouveau dossier suivi d'un appel récursif
				path.add(element.getName());
				arborescence.add(Arborescence(element.getName(), path));
				path.remove(path.size() - 1);
			} else { //Gestion d'un fichier
				arborescence.add(new Pair<String, Long>(element.getName(), element.lastModified()/1000));
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
	private String GetPath(List<String> path) {
		String completePath = "";
		for(String element : path) {
			completePath += element;
			completePath += "/";
		}
		completePath = completePath.substring(0, completePath.length()-1);
		return completePath;
	}
    
}