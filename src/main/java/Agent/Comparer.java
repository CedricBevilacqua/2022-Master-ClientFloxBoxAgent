package Agent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

import Commander.DistCommander;
import Commander.LocalCommander;

/**
 * Cette classe est chargée de récupérer l'arborescence des fichiers et dossiers stockés en local et sur le serveur 
 * pour ensuite fournir différentes méthodes permettant de les analyser par comparaison.
 * 
 * @author Cédric Bevilacqua, Semae Altinkaynak
 */
public class Comparer {
	
	protected List<Object> localList;
	protected List<Object> distList;
	
	/**
	 * Constructeur qui va dès le départ enclancher le recensement de tous les éléments de l'arborescence locale et distante 
	 * afin de pouvoir ensuite effectuer les analyse dessus à l'aide des autres méthodes exposées.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param commandeDistance : Objet permettant d'effectuer des actions sur le serveur
	 * @param commandeLocale : Objet permettant d'effectuer des manipulations en local
	 */
	public Comparer(DistCommander commandeDistance, LocalCommander commandeLocale) throws JSONException, UnirestException, ParseException {
		UpdateTables(commandeLocale, commandeDistance);
    }
	
	/**
	 * Récupère l'arborescence distante et locale et la stocke sous forme d'attributs afin de pouvoir par la suite les analyser.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param commandeDistance : Objet permettant d'effectuer des actions sur le serveur
	 * @param commandeLocale : Objet permettant d'effectuer des manipulations en local
	 */
	public void UpdateTables(LocalCommander commandeLocale, DistCommander commandeDistance) throws UnirestException, JSONException, ParseException {
		try {
			localList = commandeLocale.GetTable();
			distList = commandeDistance.GetTable();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	/**
	 * Méthode publique permettant de déclencher la comparaison des modifications de la date des fichiers dans les 
	 * arborescences locales et distantes et en retourne le résultat.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Paire de deux listes contenant les fichiers modifiés localement puis ceux modifiés à distance
	 */
    public Pair<List<String>, List<String>> CompareChanges() {
    	List<String> localChanges = RecursiveCompareChanges(localList, distList, localList.get(0).toString()); //Changements locaux
    	List<String> distChanges = RecursiveCompareChanges(distList, localList, distList.get(0).toString()); //Changements distants
    	Pair<List<String>, List<String>> changes = new Pair<List<String>, List<String>>(localChanges, distChanges);
    	return changes;
    }
    
    /**
	 * Algorithme récursif effectuant une analyse en profondeur des arborescences afin de comparer la date de modification 
	 * chaque élément entre une arborescence vis à vis d'une autre.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param arborescence1 : Arborescence parcourue en profondeur
	 * @param arborescence2 : Arborescence avec laquel chaque fichier sera comparé
	 * @param location : Emplacement actuel en cours d'analyse, utile à la recursion
	 * @return List des fichiers modifiés
	 */
    private List<String> RecursiveCompareChanges(List<Object> arborescence1, List<Object> arborescence2, String location) {
    	Boolean firstPass = true;
    	List<String> changedFiles = new ArrayList<String>();
		for(Object element : arborescence1) {
			if(!firstPass) {
				if(element instanceof Pair) {
					if(GetChanged((Pair<String, String>) element, arborescence2)){
						changedFiles.add(location + "/" + ((Pair<String, String>) element).getValue0().toString());
					}
				} else if(element instanceof List<?>) {
					List<Object> newArborescence2 = GetFromArborescence(arborescence2, ((List<Object>) element).get(0).toString());
					if(newArborescence2 != null) {
						changedFiles.addAll(RecursiveCompareChanges((List<Object>) element, newArborescence2, location + "/" + ((List<Object>) element).get(0).toString()));
					} else {
						changedFiles.addAll(RecursiveCompareChanges((List<Object>) element, new ArrayList<Object>(), location + "/" + ((List<Object>) element).get(0).toString()));
					}
				}
			} else { //Passage du nom du dossier
				firstPass = false;
			}
		}
		return changedFiles;
    }
    
    /**
	 * Recherche un fichier à partir de son nom dans les fichiers accessibles immédiatement dans une arborescence puis 
	 * compare si le fichier donné est plus récent que le fichier retrouvé dans l'arborescence.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param file : Paire représentant le nom d'un fichier et le timestamp de sa dernière modification
	 * @param arborescence2 : Arborescence dans laquelle rechercher un fichier identique en profondeur 0
	 * @return Vrai si le fichier indiqué est plus récent que le fichier de l'arborescence, faux sinon ou si le fichier n'est pas trouvé
	 */
    private Boolean GetChanged(Pair<String, String> file, List<Object> arborescence2) {
    	for(Object element : arborescence2) {
    		if(element instanceof Pair) {
    			if(((Pair<String, String>) element).getValue0().toString().equals(file.getValue0())) {
    				int value1 = Integer.parseInt((((Pair) file).getValue1()).toString());
    				int value2 = Integer.parseInt((((Pair) element).getValue1()).toString());
    				if(value1 > value2) {
    					return true;
    				} else {
    					return false;
    				}
    			}
    		}
    	}
    	return false;
	}

    /**
	 * Méthode publique permettant de comparer les suppressions locales.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Paire de deux listes contenant les fichiers supprimés puis la liste des dossiers supprimés
	 */
	public Pair<List<String>, List<String>> CompareLocalDeletions() {
    	return RecursiveCompareLocalNew(distList, localList, new ArrayList<String>(), distList.get(0).toString());
    }
    
	/**
	 * Méthode publique permettant de comparer les nouveaux fichiers qui sont apparus dans l'arborescence locale
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @return Paire de deux listes contenant la liste des nouveaux fichiers puis la liste des nouveaux dossiers
	 */
    public Pair<List<String>, List<String>> CompareLocalNew() {
    	return RecursiveCompareLocalNew(localList, distList, new ArrayList<String>(), localList.get(0).toString());
    }
    
    /**
	 * Algorithme recursif permettant de comparer deux arborescences afin d'identifier les nouveaux éléments de la 
	 * première vis à vis de la deuxième
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param arborescence1 : Arborescence parcourue en profondeur de manière récursive
	 * @param arborescence2 : Arborescence avec laquelle on vérifie l'existence de chaque fichier de la première arborescence
	 * @param newFolders : Liste des nouveaux dossiers identifiés, utilisé pour la récursion
	 * @param location : Emplacement en cours d'anlayse dans l'arborescence, utilisé pour la récursion
	 * @return Paire de la liste des nouveaux fichiers puis de la liste des nouveaux dossiers
	 */
    private Pair<List<String>, List<String>> RecursiveCompareLocalNew(List<Object> arborescence1, List<Object> arborescence2, List<String> newFolders, String location) {
    	Boolean firstPass = true;
    	List<String> newFiles = new ArrayList<String>();
		for(Object element : arborescence1) {
			if(!firstPass) {
				if(element instanceof Pair) {
					if(!Searcher(((Pair<String, String>) element).getValue0().toString(), arborescence2, false)) {
						newFiles.add(location + "/" + ((Pair<String, String>) element).getValue0().toString());
					}
				} else if(element instanceof List<?>) {
					List<Object> newArborescence2 = GetFromArborescence(arborescence2, ((List<Object>) element).get(0).toString());
					if(newArborescence2 != null) {
						newFiles.addAll(RecursiveCompareLocalNew((List<Object>) element, newArborescence2, newFolders, location + "/" + ((List<Object>) element).get(0).toString()).getValue0());
					} else {
						newFolders.add(location + "/" + ((List<Object>) element).get(0).toString());
						newFiles.addAll(RecursiveCompareLocalNew((List<Object>) element, new ArrayList<Object>(), newFolders, location + "/" + ((List<Object>) element).get(0).toString()).getValue0());
					}
				}
			} else { //Passage du nom du dossier
				firstPass = false;
			}
		}
		return new Pair<List<String>, List<String>>(newFiles, newFolders);
    }
	
    /**
	 * Méthode permettant de récupérer un dossier dans l'arborescence au niveau 0.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param arborescence : Arborescence à analyser
	 * @param name : Nom du dossier à récupérer
	 * @return Liste des fichiers correspondant au dossier dans l'arborescence
	 */
	private List<Object> GetFromArborescence(List<Object> arborescence, String name) {
		for(Object element : arborescence) {
			if(element instanceof List<?> && ((List) element).get(0).toString().equals(name)) {
				return (List<Object>) element;
			}
		}
		return null;
	}
    
	/**
	 * Vérifie l'existence d'un fichier dans une arborescence.
	 *
	 * @author Cédric Bevilacqua, Semae Altinkaynak
	 * @param fileName : Nom du fichier à rechercher
	 * @param arborescence2 : Arborescence dans laquelle rechercher le fichier à la profondeur 0
	 * @return Vrai ou faux selon si le fichier existe ou non
	 */
    private Boolean Searcher(String fileName, List<Object> arborescence2, Boolean directory) {
    	for(Object element : arborescence2) {
    		if(element instanceof Pair) {
    			if(((Pair) element).getValue0().toString().equals(fileName)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
}