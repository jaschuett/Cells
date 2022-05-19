package mygdx.cells;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class GameScreen implements Screen {
	float simFps = 15;
	float timeSinceSim = 0;
	final Cells game;
	OrthographicCamera camera;
	ExtendViewport viewport;
	int cellSize = 50;
	ConwaySim conway = new ConwaySim();
	boolean run = false;
	Color bgColor = Color.WHITE;
	Color cellColor = Color.GREEN;
	Color gridColor = Color.BLACK;
	Color chunkColor = Color.BLACK;
	
	
	/*
	Stage stage=new Stage();
	Dialog dialog;
	*/
	
	public GameScreen(final Cells game) {
		this.game = game;
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(1280, 720, camera);
		
		camera.setToOrtho(false, 1280, 720);
		
		
		
		/*
		Skin skin=new Skin(Gdx.files.internal("ui/uiskin.json"));

		dialog=new Dialog("Setting",skin);
		dialog.setSize(Gdx.graphics.getWidth()/10,200);
		dialog.setPosition(Gdx.graphics.getWidth()/2-100,Gdx.graphics.getHeight()/2-100);

		final SelectBox<String> selectBox=new SelectBox<String>(skin);
		selectBox.setItems("XYZ","ABC","PQR","LMN");

		dialog.getContentTable().defaults().pad(10);
		dialog.getContentTable().add(selectBox);

		dialog.scaleBy(-0.5f);
		
		stage.addActor(dialog);
		*/
		
		

		Gdx.input.setInputProcessor(new InputAdapter() {
			@Override
			public boolean scrolled(float amountX, float amountY) {
				camera.zoom += amountY*0.2;
				camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 100);
				camera.update();
				//System.out.println(camera.zoom);
				return true;
			}
			
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				//find the coords of the chunk that was clicked on
				Vector3 world = camera.unproject(new Vector3(screenX, screenY, 0));
				int chunkScale = cellSize*Chunk.chunkSize;
				int[] worldChunk = new int[]{(int) Math.floor(world.x/chunkScale), (int)Math.floor(world.y/chunkScale)};
				
				//find the coords of the cell clicked on (x+ is right, y+ is down)
				//will range from 0 to chunkSize
				int[] cellCoords = new int[]{((int)(world.x - worldChunk[0]*chunkScale)/cellSize+Chunk.chunkSize) % Chunk.chunkSize, 
											((int)(world.y - worldChunk[1]*chunkScale)/cellSize+Chunk.chunkSize) % Chunk.chunkSize};
				
				//load the chunk and update the cell
				Chunk.loadChunk(worldChunk);
				Chunk thisChunk = Chunk.getChunkAt(worldChunk);
				thisChunk.cells[cellCoords[0]][cellCoords[1]] = 1;
				
				//System.out.println(thisChunk.coord[0] + " " + thisChunk.coord[1] + " "+ cellCoords[0] + " " + cellCoords[1]);
				System.out.println(Chunk.chunks.size());
				return true;
			}
			
			@Override
			public boolean keyDown(int keycode) {
				if (keycode == Input.Keys.SPACE) run = !run;
				return true;
			}
		});
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * meat
	 */
	@Override
	public void render(float delta) {
		
		if (Gdx.input.isTouched()) {
			camera.translate(-Gdx.input.getDeltaX() * camera.zoom, Gdx.input.getDeltaY() * camera.zoom, 0);
			camera.update();
		}
		
		ScreenUtils.clear(bgColor);
		
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
		game.font.draw(game.batch, ""+1/delta, 300, 200);
		game.batch.end();
		
		
		drawGrid();
		
		timeSinceSim += delta;
		if (timeSinceSim >= 1/simFps) {
			timeSinceSim = 0;
			if (run) {
				conway.applyRule();
				}
		}
		System.out.println(timeSinceSim);
		metabolize(conway);
		//Gdx.input.setInputProcessor(stage);
		//stage.act();
		//stage.draw();
		
	}
	
	/*
	 * Process the given automaton: render this tick
	 * and calculate the next
	 */
	public void metabolize(Automaton auto) {
		
		
		//render the cells
		game.shapeRenderer.begin(ShapeType.Filled);
		game.shapeRenderer.setColor(cellColor);
		Iterator<Chunk> iter = Chunk.chunks.iterator();
		int chunkScale = cellSize*Chunk.chunkSize;
		//for every chunk:
		while (iter.hasNext()) {
			Chunk chunk = iter.next();
			
			//for every cell in chunk
			for (int x = 0; x < Chunk.chunkSize; x++) {
				for (int y = 0; y < Chunk.chunkSize; y++) {
					
					//if cell not empty, render it
					if (chunk.cells[x][y] != 0) {
						game.shapeRenderer.rect(chunkScale*chunk.coord[0] + x*cellSize, 
												chunkScale*chunk.coord[1] + y*cellSize, cellSize, cellSize);
					}
					
				}
			}
		}
		game.shapeRenderer.end();
	}
	
	
	public void drawGrid() {
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		
		float top = camera.unproject(new Vector3(0, 0, 0)).y;
		float bottom = camera.unproject(new Vector3(0, camera.viewportHeight, 0)).y;
		float left = camera.unproject(new Vector3(0, 0, 0)).x;
		float right = camera.unproject(new Vector3(camera.viewportWidth, 0, 0)).x;

		//We need to offset the grid so it doesn't stick to the camera
		float offsetX = left % cellSize;
		float offsetY = bottom % cellSize;
		game.shapeRenderer.begin(ShapeType.Line);
		
		//verticals
		for (float x = left-offsetX; x < right; x+=cellSize) {
			if (x/2 % (Chunk.chunkSize) == 0) { //render chunks
				game.shapeRenderer.setColor(chunkColor);
			}
			else game.shapeRenderer.setColor(gridColor);
			game.shapeRenderer.line(x, bottom, x, top);
			//System.out.println(left-offsetX);
		}
		
		//horizontals
		for (float y = bottom-offsetY; y < top; y+=cellSize) {
			if (y/2 % (Chunk.chunkSize) == 0) { //render chunks
				game.shapeRenderer.setColor(chunkColor);
			}
			else game.shapeRenderer.setColor(gridColor);
			game.shapeRenderer.line(left, y, right, y);
		}
		game.shapeRenderer.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		game.batch.dispose();
		game.shapeRenderer.dispose();
	}

}
