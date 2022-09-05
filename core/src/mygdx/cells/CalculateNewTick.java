package mygdx.cells;

import java.util.Iterator;

/**
 * Calculates the number of live cells in the 8 adjacent spots
 */
public class CalculateNewTick {
	/**
	 * -1
	 */
	final static int LEFT = -1,  UP = -1;
	
	/**
	 * 0
	 */
	final static int CHUNK_COORD = 0, COORD_X = 0, XCENTER = 0, YCENTER = 0;
	
	/**
	 * 1
	 */
	final static int RIGHT = 1, DOWN = 1, CELL_COORD = 1, COORD_Y = 1;
	
	
	/**
	 * Calculates the number of live neighbors of each cell and updates next tick
	 */
	public static void calculate(Automaton ruleset) {
		ThreadGroup threadGroup = new ThreadGroup("chunks");
		Iterator<Chunk> iter = Chunk.chunks.iterator();
		while (iter.hasNext()) {
			Chunk chunk = iter.next();
			
			Thread thread = new Thread(threadGroup, new Runnable() {
				public void run() {
					calculateChunk(ruleset, chunk);
					
				}
			}); 
			thread.run(); 
		}
		if (threadGroup.activeCount() == 0) {
			//update every chunk at once
			iter = Chunk.chunks.iterator();
			while (iter.hasNext()) {
				Chunk chunk = iter.next();
				chunk.cells = chunk.cellBuffer;
			}
			Chunk.disposeChunks();
			Chunk.generateChunks();
		}
	}

	private static void calculateChunk(Automaton ruleset, Chunk chunk) {
		int[][] newTick = new int[Chunk.chunkSize][Chunk.chunkSize];
		int newTickLiveCount = 0;
		//for every cell in the chunk
		for (int x = 0; x < Chunk.chunkSize; x++) {
			for (int y = 0; y < Chunk.chunkSize; y++) {
				int neighbors = 0;
				
				//check its neighbors, starting from top left, going clockwise
				for (int n = 0; n < 8; n++) {
					
					if (x == 0 || x == Chunk.chunkSize-1 || y == 0 || y == Chunk.chunkSize-1) { //edge cases: cells on border
						switch (n) {
						case 0: 
							neighbors += ruleHelper(chunk, x, y, LEFT, UP);
							break;
							
						case 1:
							neighbors += ruleHelper(chunk, x, y, XCENTER, UP);
							break;
							
						case 2: 
							neighbors += ruleHelper(chunk, x, y, RIGHT, UP);
							break;
						
						case 3: 
							neighbors += ruleHelper(chunk, x, y, RIGHT, YCENTER);
							break;
						
						case 4: 
							neighbors += ruleHelper(chunk, x, y, RIGHT, DOWN);
							break;
						
						case 5: 
							neighbors += ruleHelper(chunk, x, y, XCENTER, DOWN);
							break;
						
						case 6: 
							neighbors += ruleHelper(chunk, x, y, LEFT, DOWN);
							break;
						
						case 7: 
							neighbors += ruleHelper(chunk, x, y, LEFT, YCENTER);
							break;
							
						} //end switch
						
						
					} else { //within chunk
						switch (n) {
						case 0: if (chunk.cells[x+LEFT] [y+UP]   != 0) {neighbors++;} break;
						case 1: if (chunk.cells[x]		[y+UP]   != 0) {neighbors++;} break;
						case 2: if (chunk.cells[x+RIGHT][y+UP]   != 0) {neighbors++;} break;
						case 3: if (chunk.cells[x+RIGHT][y]      != 0) {neighbors++;} break;
						case 4: if (chunk.cells[x+RIGHT][y+DOWN] != 0) {neighbors++;} break;
						case 5: if (chunk.cells[x]		[y+DOWN] != 0) {neighbors++;} break;
						case 6: if (chunk.cells[x+LEFT] [y+DOWN] != 0) {neighbors++;} break;
						case 7: if (chunk.cells[x+LEFT] [y]      != 0) {neighbors++;} break;
						}
					 }
				}
				int liveOrNot = ruleset.applyRule(neighbors, chunk.cells[x][y]);
				newTick[x][y] = liveOrNot;
				if (liveOrNot == 1) {newTickLiveCount++;}
			}
		}
		
		//update the chunk's cell buffer and live count
		chunk.cellBuffer = newTick;
		chunk.liveCount = newTickLiveCount;
		
	}
	
	/** Checks the cell relative to the cell at [x, y] using given offset
	 * @param chunk 
	 * @param x
	 * @param y
	 * @param xOffset
	 * @param yOffset
	 * @return 1 if the checked cell was alive, 0 if it was not
	 */
	private static int ruleHelper(Chunk chunk, int x, int y, int xOffset, int yOffset) {
		int alive = 0;
		
		//get the new chunk and cell coords we need
		int[][] fixedCoords = fixCoords(new int[][] {chunk.coord, {x+xOffset, y+yOffset}}); 
		Chunk fixedChunk = Chunk.getChunkAt(fixedCoords[CHUNK_COORD]);
		
		//break if coords are in an unloaded chunk
		if (fixedChunk == null) return 0;
		
		//check the cell at the coords
		if (fixedChunk.cells[fixedCoords[CELL_COORD][COORD_X]][fixedCoords[CELL_COORD][COORD_Y]] != 0) {
			alive = 1;
		}
		
		return alive;
	}
	
	/**
	 * Fixes chunk/cell coords.
	 * If a cell coord is out of bounds of the chunk, 
	 * change the chunk coord so it is in bounds
	 * coords[CHUNK_COORD]: the chunk coord
	 * coords[CELL_COORD]: the cell coord within that chunk
	 * 
	 * Example: [[0, 0], [-1, 0]] returns [[-1, 0], [chunkSize, 0]]
	 */
	private static int[][] fixCoords(int[][] coords){
		int[][] fixedCoords = new int[2][2];
		int chunkX = coords[CHUNK_COORD][COORD_X];
		int chunkY = coords[CHUNK_COORD][COORD_Y];
		int cellX = coords[CELL_COORD][COORD_X];
		int cellY = coords[CELL_COORD][COORD_Y];
		
		if (cellX < 0) {
			cellX += Chunk.chunkSize;
			chunkX--;
		}
		
		if (cellY < 0) {
			cellY += Chunk.chunkSize;
			chunkY--;
		}
		
		if (cellX > Chunk.chunkSize-1) {
			chunkX++;
		}
		
		if (cellY > Chunk.chunkSize-1) {
			chunkY++;
		}
		
		fixedCoords[CHUNK_COORD] = new int[] {chunkX, chunkY};
		fixedCoords[CELL_COORD] = new int[] {cellX % Chunk.chunkSize, cellY % Chunk.chunkSize};
		
		return fixedCoords;
	}
}
