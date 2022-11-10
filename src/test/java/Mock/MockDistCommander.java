package Mock;
import Commander.DistCommander;

import java.util.List;

public class MockDistCommander extends DistCommander {
	
	public MockDistCommander() {
		super("X", "X", "X");
    }
	
	public String GetPath(List<String> path) {
		return super.GetPath(path);
	}
	
	public void CalcTree(List<Object> arborescence) {
		super.CalcTree(arborescence, 0);
	}
	
	public String GetTree() {
		return super.tree.substring(4);
	}
    
}