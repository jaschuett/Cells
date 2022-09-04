package mygdx.cells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

//x+ is right, y+ is down
public class Chunk {
	/**
	 * 0
	 */
	final static int COORD_X = 0;
	
	/**
	 * 1
	 */
	final static int COORD_Y = 1;
	
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
	 * @param coord the coodinate of the chunk in chunk coordinates
	 */
	public Chunk(int[] coord) {
		cells = new int[chunkSize][chunkSize];
		this.coord = coord;
		chunks.add(this);
		allCoords.add(coord);
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

			int buffer = 3;
			//add new chunks
			//left neighbor
			for (int x = 0; x < buffer; x++) {
				for (int y = 0; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[COORD_X]-1, chunkIter.coord[COORD_Y]})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[COORD_X]-1, chunkIter.coord[COORD_Y]});
							break;
						}
					}
				}
			}
			
			//right neighbor
			for (int x = chunkSize-buffer; x < chunkSize; x++) {
				for (int y = 0; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[COORD_X]+1, chunkIter.coord[COORD_Y]})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[COORD_X]+1, chunkIter.coord[COORD_Y]});
							break;
						}
					}
				}
			}
			
			//up neighbor
			for (int x = 0; x < chunkSize; x++) {
				for (int y = 0; y < buffer; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[COORD_X], chunkIter.coord[COORD_Y]-1})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[COORD_X], chunkIter.coord[COORD_Y]-1});
							break;
						}
					}
				}
			}
			
			//down neighbor
			for (int x = 0; x < chunkSize; x++) {
				for (int y = chunkSize-buffer; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[COORD_X], chunkIter.coord[COORD_Y]+1})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[COORD_X], chunkIter.coord[COORD_Y]+1});
							break;
						}
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
