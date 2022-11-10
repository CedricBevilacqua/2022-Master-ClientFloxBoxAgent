package Mock;
import Agent.Comparer;

import java.text.ParseException;
import java.util.List;

import org.javatuples.Pair;
import org.json.JSONException;

import com.mashape.unirest.http.exceptions.UnirestException;

public class MockComparer extends Comparer {
	
	public MockComparer() throws JSONException, UnirestException, ParseException {
		super(null, null);
    }
    
    public Pair<List<String>, List<String>> CompareChanges() {
    	return super.CompareChanges();
    }

	public Pair<List<String>, List<String>> CompareLocalDeletions() {
    	return super.CompareLocalDeletions();
    }
    
    public Pair<List<String>, List<String>> CompareLocalNew() {
    	return super.CompareLocalNew();
    }
    
    public void SetLists(List<Object> localList, List<Object> distList) {
    	super.localList = localList;
    	super.distList = distList;
    }
    
}