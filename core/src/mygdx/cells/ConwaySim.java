package mygdx.cells;

/**
 * Conway's game of life:
 * Cells are alive on next tick if:
 * 1. it is alive and 2 of its neighbors are alive, or
 * 2. 3 of its neighbors are alive
 */
public class ConwaySim implements Automaton{
	
	/**
	 * 
	 * @param neighbors the number of neighbors the cell has
	 * @param cell the current dead/alive status of the cell
	 * @return 1 if the cell should be alive, 0 if it should be dead
	 */
	
	public int applyRule(int neighbors, int cell) {
		if ((neighbors == 2 && cell != 0) || neighbors == 3) {return 1;} 
		else {return 0;}
	}
}
