import java.util.ArrayList;
import java.util.List;

class History {
	private List<String> history = new ArrayList<>();
	private int location;
	public History() {
		location = 0;
	}
	public String backwards() {
		//only do stuff if there is history to work with
		if (history.size() > 0) {
			//if we're already as far back as we can go we just have to stay there
			if (location == 0) {
				return history.get(0);
			}
			
			//go back one
			return history.get(--location);
		}
		
		//no history. empty string it is!
		return "";
	}
	
	public String forwards() {
		if (history.size() > 0) {
			if (location == history.size()) { //past the end. new territory
				return "";
			} else if (location < history.size()) { //if == last valid element ref
				++location;
				if (location == history.size()) {
					return "";
				}
				return history.get(location);
			}
		}
		
		//no history, only the empty string
		return "";
	}
	
	public void add(String element) {
		history.add(element);
		location = history.size();
	}
}