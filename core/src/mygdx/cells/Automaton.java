package mygdx.cells;

public class Automaton {
	byte[] birth;
	byte[] survive;
	
	public Automaton(byte[] birth, byte[] survive) {
		this.birth = birth;
		this.survive = survive;
	}
	
	public int applyRule(int neighbors, int cell) {
		for (int i = 0; i < birth.length; i++) {
			if (neighbors == i) {
				if (birth[i] == 1) return 1;
			}
		}
		
		if (cell == 1) {
			for (int i = 0; i < survive.length; i++) {
				if (neighbors == i) {
					if (survive[i] == 1) return 1;
				}
			}
		}
		
		return 0;
	}
}
