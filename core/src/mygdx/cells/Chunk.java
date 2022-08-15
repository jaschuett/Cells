package mygdx.cells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
//x+ is right, y+ is down
public class Chunk {
	public static ArrayList<Chunk> chunks = new ArrayList<Chunk>();
	final public static int chunkSize = 16;
	public int[][] cells;
	public int[][] cellBuffer;
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
						if (containsCoord(new int[] {chunkIter.coord[0]-1, chunkIter.coord[1]})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[0]-1, chunkIter.coord[1]});
							break;
						}
					}
				}
			}
			
			//right neighbor
			for (int x = chunkSize-buffer; x < chunkSize; x++) {
				for (int y = 0; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[0]+1, chunkIter.coord[1]})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[0]+1, chunkIter.coord[1]});
							break;
						}
					}
				}
			}
			
			//up neighbor
			for (int x = 0; x < chunkSize; x++) {
				for (int y = 0; y < buffer; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[0], chunkIter.coord[1]-1})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[0], chunkIter.coord[1]-1});
							break;
						}
					}
				}
			}
			
			//down neighbor
			for (int x = 0; x < chunkSize; x++) {
				for (int y = chunkSize-buffer; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						if (containsCoord(new int[] {chunkIter.coord[0], chunkIter.coord[1]+1})) {
							break;
						} else {
							loadChunk(new int[] {chunkIter.coord[0], chunkIter.coord[1]+1});
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
			boolean empty = true;
			
			//remove empty chunks
			for (int x = 0; x < chunkSize; x++) {
				for (int y = 0; y < chunkSize; y++) {
					if (chunkIter.cells[x][y] != 0) {
						empty = false;
						break;
					}
				}
				if (!empty) {
					break;
				}
			}
			
			if (empty) {
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
		return allCoords.stream().filter(o -> (o[0] == coord[0] && o[1] == coord[1])).findFirst().isPresent();
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
		return new int[] {coord[0]*chunkSize, coord[1]*chunkSize};
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
