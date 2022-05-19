package mygdx.cells;

public interface Automaton {
	/*
	 * Go through every chunk in Chunk,
	 * apply the rule for every cell in those chunks,
	 * and then call Chunk.update
	 */
	public void applyRule();
}
