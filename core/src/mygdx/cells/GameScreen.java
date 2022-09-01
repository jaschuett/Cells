package mygdx.cells;
import java.util.Iterator;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.MathUtils;

public class GameScreen implements Screen {
	float simFps = 15;
	float timeSinceSim = 0;
	final Cells game;
	OrthographicCamera camera;
	ExtendViewport viewport;
	int cellSize = 50;
	boolean run = false;
	boolean showGrid = true;
	boolean showChunks = false;
	Color bgColor = Color.WHITE;
	Color cellColor = Color.BLACK;
	Color gridColor = Color.BLACK;
	Color chunkColor = Color.RED;
	Stage stage=new Stage();
	Skin skin=new Skin(Gdx.files.internal("ui/uiskin.json"));
	Table table;
	Table rulesDialog;
	boolean dialogUp = false;
	
	/**
	 * Bit flags to represent the game rules. Index represents
	 * number of neighbors from 0-8, value indicates whether the
	 * cell should be born/survive with index many neighbors.
	 * Default is Conway rules
	 */
	byte[] birth = new byte[] {0, 0, 0, 1, 0, 0, 0, 0, 0};
	byte[] survive = new byte[] {0, 0, 1, 1, 0, 0, 0, 0, 0};
	Automaton ruleset = new Automaton(birth, survive);
	
	public GameScreen(final Cells game) {
		//boilerplate
		this.game = game;
		camera = new OrthographicCamera();
		viewport = new ExtendViewport(1280, 720, camera);
		camera.setToOrtho(false, 1280, 720);

		//create top settings bar
		createMenuBar(skin);
		
		//input processor for the grid
		InputAdapter cellsInput = new InputAdapter() {
			@Override
			//zoom
			public boolean scrolled(float amountX, float amountY) {
				camera.zoom += amountY*0.75;
				camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 1000);
				camera.update();
				return true;
			}
			
			long timeClicked;
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				timeClicked = Gdx.input.getCurrentEventTime();
				return true;
			}
			
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				
				//mouse clicked instead of held
				if ((Gdx.input.getCurrentEventTime() - timeClicked)/1_000_000_000.0 > 0.1) {
					return true;
				}
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
				
				return true;
			}
			
			@Override
			public boolean keyDown(int keycode) {
				//toggle sim
				if (keycode == Input.Keys.SPACE) {
					run = !run;
				}
				
				//reset sim
				if (keycode == Input.Keys.R) {
					for (Chunk chunk: Chunk.chunks) {
						chunk.cells = new int[cellSize][cellSize];
					}
				}
				
				//edit rules
				if (keycode == Input.Keys.E) {
					createRulesDialog(skin);
				}
				
				return true;
			}
		};
		
		InputMultiplexer inputMulti = new InputMultiplexer(stage, cellsInput);
		Gdx.input.setInputProcessor(inputMulti);
	}
	
	private void createRulesDialog(Skin skin) {
		if (dialogUp) {
			rulesDialog.remove();
			dialogUp = false;
			return;
		}
		
		dialogUp = true;
		
		//dialog window
		rulesDialog = new Table(skin);
		rulesDialog.setSize(250, 350);
		rulesDialog.align(Align.center);
		rulesDialog.setBackground(skin.getDrawable("dialog"));
		rulesDialog.setX(Gdx.graphics.getWidth()-rulesDialog.getWidth());
		rulesDialog.setY(Gdx.graphics.getHeight()/2-rulesDialog.getHeight()/2);
		
		//birth/survival buttons
		final int NUM_BUTTONS = 9;
		final int BUTTON_PADDING = 5;
		VerticalGroup birthButtons = new VerticalGroup();
		Label birthLabel = new Label("Birth", skin);
		birthButtons.addActor(birthLabel);
		
		VerticalGroup surviveButtons = new VerticalGroup();
		Label surviveLabel = new Label("Survive", skin);
		surviveButtons.addActor(surviveLabel);
		for (int i = 0; i < NUM_BUTTONS; i++) {
			int buttonIndex = i;
			
			//set up the birth rule buttons
			CheckBox birth = new CheckBox(""+i, skin);
			birth.setChecked(ruleset.birth[buttonIndex] == 1);
			birth.addListener((e) -> {
				if (birth.isChecked()) {
					ruleset.birth[buttonIndex] = 1;
				} else {
					ruleset.birth[buttonIndex] = 0;
				}
				return true;
				});
			birth.pad(BUTTON_PADDING);
			birthButtons.addActor(birth);
			
			//set up the survive rule buttons
			CheckBox survive = new CheckBox(""+i, skin);
			survive.setChecked(ruleset.survive[buttonIndex] == 1);
			survive.addListener((e) -> {
				
				if (survive.isChecked()) {
					ruleset.survive[buttonIndex] = 1;
					return true;
				} else {
					ruleset.survive[buttonIndex] = 0;
					return true;
				}
				});
			survive.pad(BUTTON_PADDING);
			surviveButtons.addActor(survive);
		}
		
		rulesDialog.add(birthButtons);
		rulesDialog.add(surviveButtons);
		stage.addActor(rulesDialog);
	}

	private void createMenuBar(Skin skin) {
		//main bar
		table = new Table();
		
		float padding = 10f;
		
		//color selection
		final SelectBox<String> colorSelect = new SelectBox<String>(skin);
		colorSelect.setItems("Black", "Green", "Red", "Blue", "Random");
		colorSelect.addListener((e) -> {
			if (e instanceof ChangeEvent) {
					switch (colorSelect.getSelected()) {
						case "Green": cellColor = Color.GREEN; break;
						case "Black": cellColor = Color.BLACK; break;
						case "Red": cellColor = Color.RED; break;
						case "Blue": cellColor = Color.BLUE; break;
						case "Random": 
							Random rand = new Random();
							int num = rand.nextInt(0xfffffff);
							cellColor = Color.valueOf(""+num); break;
					}
				}
			return true;
			});
		Label colorLabel = new Label("Color", skin);
		VerticalGroup colorGp = new VerticalGroup();
		colorGp.addActor(colorLabel);
		colorGp.addActor(colorSelect);
		colorGp.pad(padding);
		table.add(colorGp);
		
		//speed selection
		final SelectBox<String> speedSelect = new SelectBox<String>(skin);
		speedSelect.setItems("5", "10", "15", "30", "60", "120", "Unlimited");
		speedSelect.setSelectedIndex(3);
		speedSelect.addListener((e) -> {
			switch(speedSelect.getSelected()) {
				case "5": simFps = 5; break;
				case "10": simFps = 10; break;
				case "15": simFps = 15; break;
				case "30": simFps = 30; break;
				case "60": simFps = 60; break;
				case "120": simFps = 120; break;
				case "Unlimited": simFps = Integer.MAX_VALUE; break;
			}
			return true;
		});
		Label speedLabel = new Label("Ticks/sec", skin);
		VerticalGroup speedGp = new VerticalGroup();
		speedGp.addActor(speedLabel);
		speedGp.addActor(speedSelect);
		speedGp.pad(padding);
		table.add(speedGp);
		
		//draw grid option
		CheckBox showGridButton = new CheckBox("Show grid", skin);
		showGridButton.setChecked(true);
		showGridButton.addListener((e) -> {
				if (showGridButton.isChecked()) showGrid = true;
				else showGrid = false;
				return true;
			});
		showGridButton.pad(padding);
		table.add(showGridButton);
		
		//draw chunk borders option
		CheckBox showChunksButton = new CheckBox("Show chunks", skin);
		showChunksButton.addListener((e) -> {
			if (showChunksButton.isChecked()) showChunks = true;
			else showChunks = false;
			return true;
		});
		showChunksButton.pad(padding);
		table.add(showChunksButton);
		
		//reset
		Label restartLabel = new Label("Reset\n[R]", skin);
		VerticalGroup rsGp = new VerticalGroup();
		rsGp.addActor(restartLabel);
		rsGp.pad(padding);
		table.add(rsGp);
		
		//start/stop
		Label startLabel = new Label("Start/stop\n[Space]", skin);
		VerticalGroup ssGp = new VerticalGroup();
		ssGp.addActor(startLabel);
		ssGp.pad(padding);
		table.add(ssGp);
		
		//rules
		Label rulesDialogButtonLabel = new Label("Edit Rules\n[E]", skin);
		HorizontalGroup rulesDialogButtonGp = new HorizontalGroup();
		rulesDialogButtonGp.addActor(rulesDialogButtonLabel);
		rulesDialogButtonGp.pad(padding);
		table.add(rulesDialogButtonGp);
		
		//finish table
		table.setPosition(0,Gdx.graphics.getHeight()-colorGp.getPrefHeight());
		table.setBackground(skin.getDrawable("blue"));
		
		table.left();
		table.setSize(Gdx.graphics.getWidth(), colorGp.getPrefHeight());
		//table.debug();
		stage.addActor(table);
	}


	/**
	 * Render the screen
	 */
	@Override
	public void render(float delta) {
		
		//pan camera
		if (Gdx.input.isTouched()) {
			camera.translate(-Gdx.input.getDeltaX() * camera.zoom, Gdx.input.getDeltaY() * camera.zoom, 0);
			camera.update();
		}
		ScreenUtils.clear(bgColor);
		
		camera.update();
		game.batch.setProjectionMatrix(camera.combined);
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		
		game.batch.begin();
		game.batch.end();
		
		drawGrid();
		drawChunks();
		
		timeSinceSim += delta;
		if (timeSinceSim >= 1/simFps) {
			timeSinceSim = 0;
			if (run) {
					CalculateNewTick.calculate(ruleset);
				}
		}
		metabolize();
		stage.act();
		stage.draw();
		
	}
	
	private void drawChunks() {
		if (!showChunks) return;
		if (Chunk.chunks.isEmpty()) return;
		
		//setup
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		game.shapeRenderer.begin(ShapeType.Line);
		game.shapeRenderer.setColor(chunkColor);
		
		for (Chunk chunk: Chunk.chunks) {
			int[] cellCoords = chunk.chunkCoordsToCell();
			int[] coordinates = new int[] {cellCoords[0]*cellSize, cellCoords[1]*cellSize};
			
			//left edge
			game.shapeRenderer.line(coordinates[0], coordinates[1], coordinates[0], coordinates[1]+Chunk.chunkSize*cellSize);
			
			//right
			game.shapeRenderer.line(coordinates[0]+Chunk.chunkSize*cellSize, coordinates[1], coordinates[0]+Chunk.chunkSize*cellSize, coordinates[1]+Chunk.chunkSize*cellSize);
			
			//up
			game.shapeRenderer.line(coordinates[0], coordinates[1], coordinates[0]+Chunk.chunkSize*cellSize, coordinates[1]);
			
			//down
			game.shapeRenderer.line(coordinates[0], coordinates[1]+Chunk.chunkSize*cellSize, coordinates[0]+Chunk.chunkSize*cellSize, coordinates[1]+Chunk.chunkSize*cellSize);
		}
		game.shapeRenderer.end();
	}

	/**
	 * Process the given automaton: render this tick
	 * and calculate the next
	 */
	public void metabolize() {
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
		if (!showGrid) return;
		
		game.shapeRenderer.setProjectionMatrix(camera.combined);
		
		float top = camera.unproject(new Vector3(0, 0, 0)).y;
		float bottom = camera.unproject(new Vector3(0, camera.viewportHeight, 0)).y;
		float left = camera.unproject(new Vector3(0, 0, 0)).x;
		float right = camera.unproject(new Vector3(camera.viewportWidth, 0, 0)).x;

		//We need to offset the grid so it doesn't stick to the camera
		float offsetX = left % cellSize;
		float offsetY = bottom % cellSize;
		game.shapeRenderer.begin(ShapeType.Line);
		game.shapeRenderer.setColor(gridColor);
		
		//verticals
		for (float x = left-offsetX; x < right; x+=cellSize) {
			game.shapeRenderer.line(x, bottom, x, top);
		}
		
		//horizontals
		for (float y = bottom-offsetY; y < top; y+=cellSize) {
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

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}
}
