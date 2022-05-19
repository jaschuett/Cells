package mygdx.cells;

import java.util.Arrays;
import java.util.Iterator;

/*
 * Conway's game of life:
 * Cells are alive on next tick if:
 * 1. it is alive and 2 of its neighbors are alive, or
 * 2. 3 of its neighbors are alive
 */
public class ConwaySim implements Automaton {

	//x+ is right, y+ is down
	//bug: chunk borders
	@Override
	public void applyRule() {
		Iterator<Chunk> iter = Chunk.chunks.iterator();
		while (iter.hasNext()) {
			Chunk chunk = iter.next();
			Chunk fixedChunk; //used bc array borders
			int[][] newTick = new int[Chunk.chunkSize][Chunk.chunkSize];
			
			//for every cell in the chunk
			for (int x = 0; x < Chunk.chunkSize; x++) {
				for (int y = 0; y < Chunk.chunkSize; y++) {
					int neighbors = 0;
					int[][] fixedCoords = fixCoords(new int[][] {chunk.coord, {x, y}});
					//check its neighbors, starting from top left, going clockwise
					for (int n = 0; n < 8; n++) {
						
						
						
						
						if (x == 0 || x == Chunk.chunkSize-1 || y == 0 || y == Chunk.chunkSize-1) { //edge cases: cells on border
							switch (n) {
							case 0: 
								//get the new chunk and cell coords we need
								fixedCoords = fixCoords(new int[][] {chunk.coord, {x-1, y-1}}); 
								
								//update fixedChunk 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								
								//break if coords are in an unloaded chunk
								if (fixedChunk == null) break;
								
								//check chunk and cell coords
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
								
							case 1: 
								fixedCoords = fixCoords(new int[][] {chunk.coord, {x, y-1}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
								
							case 2: fixedCoords = fixCoords(new int[][] {chunk.coord, {x+1, y-1}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
							
							case 3: fixedCoords = fixCoords(new int[][] {chunk.coord, {x+1, y}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
							
							case 4: fixedCoords = fixCoords(new int[][] {chunk.coord, {x+1, y+1}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
							
							case 5: fixedCoords = fixCoords(new int[][] {chunk.coord, {x, y+1}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
							
							case 6: fixedCoords = fixCoords(new int[][] {chunk.coord, {x-1, y+1}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
							
							case 7: fixedCoords = fixCoords(new int[][] {chunk.coord, {x-1, y}}); 
								fixedChunk = Chunk.getChunkAt(fixedCoords[0]);
								if (fixedChunk == null) break;
								if (fixedChunk.cells[fixedCoords[1][0]][fixedCoords[1][1]] != 0) {
									neighbors++;
								}
								break;
								
							} //end switch
							
							
						} else { //within chunk
							switch (n) {
							case 0: if (chunk.cells[x-1][y-1] != 0) neighbors++; break;
							case 1: if (chunk.cells[x][y-1] != 0) neighbors++; break;
							case 2: if (chunk.cells[x+1][y-1] != 0) neighbors++; break;
							case 3: if (chunk.cells[x+1][y] != 0) {neighbors++;} break;
							case 4: if (chunk.cells[x+1][y+1] != 0) {neighbors++;} break;
							case 5: if (chunk.cells[x][y+1] != 0) {neighbors++;} break;
							case 6: if (chunk.cells[x-1][y+1] != 0) {neighbors++;} break;
							case 7: if (chunk.cells[x-1][y] != 0) {neighbors++;} break;
							}
						 }
					}
					
					if ((neighbors == 2 && chunk.cells[x][y] != 0) || neighbors == 3) {
						newTick[x][y] = 1;
					}
				}
			}
			
			//update the chunk's cell buffer
			chunk.cellBuffer = newTick;
		}//end iterator while loop
		
		//update every chunk at once
		iter = Chunk.chunks.iterator();
		while (iter.hasNext()) {
			Chunk chunk = iter.next();
			chunk.cells = chunk.cellBuffer.clone();
		}
		Chunk.disposeChunks();
		Chunk.generateChunks();
	}
	
	/*
	 * Fixes chunk/cell coords
	 * If a cell coord is out of bounds of the chunk, 
	 * change the chunk coord so it is in bounds
	 * coords[0]: the chunk coord
	 * coords[1]: the cell coord within that chunk
	 * 
	 * Example: [[0, 0], [-1, 0]] returns [[-1, 0], [chunkSize, 0]]
	 */
	private int[][] fixCoords(int[][] coords){
		int[][] fixedCoords = new int[2][2];
		int chunkX = coords[0][0];
		int chunkY = coords[0][1];
		int cellX = coords[1][0];
		int cellY = coords[1][1];
		
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
		
		//fixedCoords[0] = new int[] {chunkX + cellX/Chunk.chunkSize, chunkY + cellY/Chunk.chunkSize}; //chunk coords
		fixedCoords[0] = new int[] {chunkX, chunkY};
		fixedCoords[1] = new int[] {cellX % Chunk.chunkSize, cellY % Chunk.chunkSize}; //cell coords
		
		return fixedCoords;
	}
	
}
