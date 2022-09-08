package mygdx.cells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

//x+ is right, y+ is down
public class Chunk {
	/**
	 * -1
	 */
	final static int LEFT = -1,  UP = -1;
	
	/**
	 * 0
	 */
	final static int COORD_X = 0, XCENTER = 0, YCENTER = 0;
	
	/**
	 * 1
	 */
	final static int COORD_Y = 1, RIGHT = 1, DOWN = 1, CHUNK_CENTER = 1;
	
	public Chunk[][] neighbors = new Chunk[3][3];
	public static ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	final public static int chunkSize = 50;
	public int[][] cells;
	public int[][] cellBuffer;
	public int liveCount = 0;
	/**
	 * Coordinate of the chunk.
	 * Chunk coords: right is +x, down is +y, origin is {0, 0} in center of screen
	 */
	public int[] coord;
	public static ArrayList<int[]> allCoords = new ArrayList<int[]>();;
	
	/**
	 * create default chunk
	 */
	public Chunk() {
		cells = new int[chunkSize][chunkSize];
		this.coord = new int[]{0, 0};
		allCoords.add(coord);
	}
	
	/**
	 * create new chunk and add it to the list
	 * @param coord the coordinate of the chunk in chunk coordinates
	 */
	public Chunk(int[] coord) {
		cells = new int[chunkSize][chunkSize];
		this.coord = coord;
		chunks.add(this);
		allCoords.add(coord);
		neighbors[CHUNK_CENTER][CHUNK_CENTER] = this;
		addAllNeighbors();
		updateAllNeighbors();
	}
	
	private void addAllNeighbors() {
		//the chunk's 8 neighbors, clockwise
		addNeighbor(LEFT, UP);
		addNeighbor(XCENTER, UP);
		addNeighbor(RIGHT, UP);
		addNeighbor(RIGHT, YCENTER);
		addNeighbor(RIGHT, DOWN);
		addNeighbor(XCENTER, DOWN);
		addNeighbor(LEFT, DOWN);
		addNeighbor(LEFT, YCENTER);
	}
	
	private void updateAllNeighbors() {
		//the chunk's 8 neighbors, clockwise
		updateNeighbor(LEFT, UP);
		updateNeighbor(XCENTER, UP);
		updateNeighbor(RIGHT, UP);
		updateNeighbor(RIGHT, YCENTER);
		updateNeighbor(RIGHT, DOWN);
		updateNeighbor(XCENTER, DOWN);
		updateNeighbor(LEFT, DOWN);
		updateNeighbor(LEFT, YCENTER);
	}
	
	/**
	 * Evaluate the existence of the neighbor chunk, add it or null to neighbors array
	 * @param coord coord of the chunk whose neighbor to check
	 * @param xDir x direction of neighbor
	 * @param yDir y direction of neighbor
	 */
	private void addNeighbor(int xDir, int yDir) {
		neighbors[CHUNK_CENTER+xDir][CHUNK_CENTER+yDir] = getChunkAt(new int[] {coord[COORD_X]+xDir, coord[COORD_Y]+yDir});
	}
	
	/**
	 * Updates the neighbor field of the chunk at coord[coord[x]+xDir][coord[y]+yDir]
	 * @param xDir x offset of neighbor
	 * @param yDir y offset of neighbor
	 */
	private void updateNeighbor(int xDir, int yDir) {
		Chunk neighborChunk = neighbors[CHUNK_CENTER+xDir][CHUNK_CENTER+yDir];
		if (neighborChunk == null) return;
		neighborChunk.neighbors[CHUNK_CENTER-xDir][CHUNK_CENTER-yDir] = this;
	}
	
	/**
	 * Gets the neighbor of a chunk. Can only be up/down/left/right, no corners
	 * @param chunk
	 * @param xOffset LEFT, RIGHT or XCENTER
	 * @param yOffset UP, DOWN or YCENTER
	 * @return the neighbor. Can be null
	 */
	public Chunk getChunkRel(int xOffset, int yOffset) {
		return neighbors[CHUNK_CENTER+xOffset][CHUNK_CENTER+yOffset];
	}
	
	/**
	 * Generate new chunks
	 */
	public static void generateChunks() {
		@SuppressWarnings("unchecked")
		ArrayList<Chunk> newChunks = (ArrayList<Chunk>) chunks.clone();
		Iterator<Chunk> iter = newChunks.iterator();
		//add new chunks
		while (iter.hasNext()) {
			Chunk chunkIter = iter.next();
			generateChunkNeighbor(chunkIter, LEFT, YCENTER);
			generateChunkNeighbor(chunkIter, XCENTER, UP);
			generateChunkNeighbor(chunkIter, RIGHT, YCENTER);
			generateChunkNeighbor(chunkIter, XCENTER, DOWN);
		}
		
	}

	private static void generateChunkNeighbor(Chunk chunkIter, int xDir, int yDir) {
		int buffer = 3;
		
		//border regions of the chunk
		final int[] LEFT_NEIGHBOR_X = new int[] {0, buffer};
		final int[] LEFT_NEIGHBOR_Y = new int[] {0, chunkSize};
		final int[] UP_NEIGHBOR_X = new int[] {0, chunkSize};
		final int[] UP_NEIGHBOR_Y = new int[] {0, buffer};
		final int[] RIGHT_NEIGHBOR_X = new int[] {chunkSize-buffer, chunkSize};
		final int[] RIGHT_NEIGHBOR_Y = new int[] {0, chunkSize};
		final int[] DOWN_NEIGHBOR_X = new int[] {0, chunkSize};
		final int[] DOWN_NEIGHBOR_Y = new int[] {chunkSize-buffer, chunkSize};
		
		switch(xDir) {
			case LEFT: generateChunkNeighborHelper(chunkIter, xDir, yDir, LEFT_NEIGHBOR_X, LEFT_NEIGHBOR_Y); break;
			case RIGHT: generateChunkNeighborHelper(chunkIter, xDir, yDir, RIGHT_NEIGHBOR_X, RIGHT_NEIGHBOR_Y); break;
			default: break;
		}
		
		switch(yDir) {
			case UP: generateChunkNeighborHelper(chunkIter, xDir, yDir, UP_NEIGHBOR_X, UP_NEIGHBOR_Y); break;
			case DOWN: generateChunkNeighborHelper(chunkIter, xDir, yDir, DOWN_NEIGHBOR_X, DOWN_NEIGHBOR_Y); break;
			default: break;
		}
	}

	private static void generateChunkNeighborHelper(Chunk chunkIter, int xDir, int yDir, int[] REGION_X,
			int[] REGION_Y) {
		final int START = 0, STOP = 1;
		
		for (int x = REGION_X[START]; x < REGION_X[STOP]; x++) {
			for (int y = REGION_Y[START]; y < REGION_Y[STOP]; y++) {
				if (chunkIter.cells[x][y] != 0) {
					if (containsCoord(new int[] {chunkIter.coord[COORD_X]+xDir, chunkIter.coord[COORD_Y]+yDir})) {
						break;
					} else {
						loadChunk(new int[] {chunkIter.coord[COORD_X]+xDir, chunkIter.coord[COORD_Y]+yDir});
						break;
					}
				}
			}
		}
	}
	
	
	/**
	 * Remove empty chunks
	 */
	public static void disposeChunks() {
		Iterator<Chunk> chunkIterator = Chunk.chunks.iterator();
		while (chunkIterator.hasNext()) {
			
			Chunk chunkIter = chunkIterator.next();
			
			//if chunk is empty
			if (chunkIter.liveCount == 0) {
				chunkIterator.remove();
			}
			
			//update coords list
			allCoords = new ArrayList<int[]>();
			
			Iterator<Chunk> iter = chunks.iterator();
			
			while (iter.hasNext()) {
				allCoords.add(iter.next().coord);
			}
		} 
	}
	
	/**
	 * returns true if the coord is in allCoords
	 * @param coord the coordinate in chunk coordinates
	 */
	public static boolean containsCoord(int[] coord) {
		return allCoords.stream().filter(o -> (o[COORD_X] == coord[COORD_X] && o[COORD_Y] == coord[COORD_Y])).findFirst().isPresent();
	}
	
	/**
	 * Adds the chunk at the coord if it isn't already in the list
	 * @param coord the coordinate in chunk coordinates
	 */
	public static void loadChunk(int[] coord) {
		if (!Chunk.containsCoord(coord)) {
			new Chunk(coord);
		}
	}
	
	/**
	 * Converts chunk coords to world coords (cell coords)
	 * @param coords the chunk coords
	 * @return absolute cell coords of cell in upper left of chunk
	 */
	public int[] chunkCoordsToCell() {
		return new int[] {coord[COORD_X]*chunkSize, coord[COORD_Y]*chunkSize};
	}
	
	
	/**
	 * Gets the chunk at the specified coords (null if none)
	 * @param coords the coords in chunk coords
	 * @return returns the chunk or null
	 */
	public static Chunk getChunkAt(int[] coords) {
		try {
			return Chunk.chunks.stream().filter(o -> (Arrays.equals(o.coord, coords))).findFirst().get();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
}
