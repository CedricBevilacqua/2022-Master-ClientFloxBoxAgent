package Tests;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONException;
import org.junit.Test;

import com.mashape.unirest.http.exceptions.UnirestException;

import Mock.MockComparer;

public class ComparerTest {
	@Test
	public void main() throws JSONException, UnirestException, ParseException {
		//Liste locale
		List<Object> local = new ArrayList<Object>();
		local.add("/Users/cedricbevilacqua/Downloads/locationtest/Perso");
		local.add(new Pair<String, Long>("README copie 2.md",(long) 1649516499));
		local.add(new Pair<String, Long>("README copie 3.md",(long) 1649516401));
		local.add(new Pair<String, Long>("README copie 4.md",(long) 1649516398));
		local.add(new Pair<String, Long>("README copie.md",(long) 1649516394));
		local.add(new Pair<String, Long>("Restored2.md",(long) 1649591998));
		local.add(new Pair<String, Long>("Restored3.md",(long) 1649591998));
		List<Object> localtata = new ArrayList<Object>();
		localtata.add("tata");
		local.add(localtata);
		local.add(new Pair<String, Long>("teeest.md",(long) 1649516345));
		local.add(new Pair<String, Long>("test2",(long) 1649516409));
		List<Object> localtiti = new ArrayList<Object>();
		localtiti.add("titi");
		List<Object> localtitiyt = new ArrayList<Object>();
		localtitiyt.add("yt");
		localtitiyt.add(new Pair<String, Long>("TT.md",(long) 1649516402));
		localtitiyt.add(new Pair<String, Long>("T2.md",(long) 1649516402));
		localtiti.add(localtitiyt);
		local.add(localtiti);
		List<Object> localtoto = new ArrayList<Object>();
		localtoto.add("toto");
		localtoto.add(new Pair<String, Long>("README 2.md",(long) 1649516397));
		localtoto.add(new Pair<String, Long>("README copie.md",(long) 1649516397));
		local.add(localtoto);
		List<Object> localtutu = new ArrayList<Object>();
		localtutu.add("tutu");
		localtutu.add(new Pair<String, Long>("element1",(long) 1649516397));
		localtutu.add(new Pair<String, Long>("element2",(long) 1649516397));
		local.add(localtutu);
		
		//Liste distante
		List<Object> dist = new ArrayList<Object>();
		dist.add("");
		dist.add(new Pair<String, Long>("README copie 2.md",(long) 1649516399));
		dist.add(new Pair<String, Long>("README copie 3.md",(long) 1649516400));
		dist.add(new Pair<String, Long>("README copie 4.md",(long) 1649516396));
		dist.add(new Pair<String, Long>("README copie.md",(long) 1649516398));
		dist.add(new Pair<String, Long>("Restored.md",(long) 1649591997));
		dist.add(new Pair<String, Long>("Restored2.md",(long) 1649591998));
		List<Object> distTROTRO = new ArrayList<Object>();
		distTROTRO.add("TROTRO");
		dist.add(distTROTRO);
		List<Object> disttata = new ArrayList<Object>();
		disttata.add("tata");
		dist.add(localtata);
		dist.add(new Pair<String, Long>("teeest.md",(long) 1649516345));
		dist.add(new Pair<String, Long>("test",(long) 1649516395));
		dist.add(new Pair<String, Long>("test2",(long) 1649516401));
		List<Object> disttiti = new ArrayList<Object>();
		disttiti.add("titi");
		List<Object> disttitiyt = new ArrayList<Object>();
		disttitiyt.add("yt");
		disttitiyt.add(new Pair<String, Long>("TT.md",(long) 1649517402));
		disttiti.add(localtitiyt);
		dist.add(disttiti);
		List<Object> disttoto = new ArrayList<Object>();
		disttoto.add("toto");
		disttoto.add(new Pair<String, Long>("README 2.md",(long) 1649516397));
		disttoto.add(new Pair<String, Long>("README copie.md",(long) 1649516397));
		dist.add(disttoto);
		
		System.out.println("Local: " + local);
		System.out.println("Dist: " + dist);

		//Test de l'élément
		MockComparer mock = new MockComparer();
		mock.SetLists(local, dist);
		String correct;
		/*Si un nouveau fichié est créé localement dans l'arborescence d'un alias, alors il doit être ajouté au 
		  serveur FTP associé (si l'usager à les droits d'écriture distant).*/
		System.out.println("LOCALNEW: " + mock.CompareLocalNew());
		correct = "[[/Users/cedricbevilacqua/Downloads/locationtest/Perso/Restored3.md, /Users/cedricbevilacqua/Downloads/locationtest/Perso/tutu/element1, /Users/cedricbevilacqua/Downloads/locationtest/Perso/tutu/element2], [/Users/cedricbevilacqua/Downloads/locationtest/Perso/tutu]]";
		assertEquals(correct, mock.CompareLocalNew().toString());
		/*Si un fichier local est supprimé, alors il sera déplacé dans un répertoire .deleted/ à la racine du serveur*/
		System.out.println("LOCALDELETION: " + mock.CompareLocalDeletions());
		correct = "[[/Restored.md, /test], [/TROTRO]]";
		assertEquals(correct, mock.CompareLocalDeletions().toString());
		/*Si un nouveau fichié est créé localement dans l'arborescence d'un alias, alors il doit être ajouté au serveur 
		  FTP associé */
		System.out.println("CHANGES: " + mock.CompareChanges());
		correct = "[[/Users/cedricbevilacqua/Downloads/locationtest/Perso/README copie 2.md, /Users/cedricbevilacqua/Downloads/locationtest/Perso/README copie 3.md, /Users/cedricbevilacqua/Downloads/locationtest/Perso/README copie 4.md, /Users/cedricbevilacqua/Downloads/locationtest/Perso/test2], [/README copie.md]]";
		assertEquals(correct, mock.CompareChanges().toString());
	}

}
