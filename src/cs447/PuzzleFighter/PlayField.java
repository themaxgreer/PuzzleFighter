package cs447.PuzzleFighter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import jig.engine.RenderingContext;
import jig.engine.Sprite;
import jig.engine.util.Vector2D;

public class PlayField extends Sprite {
	public static final Vector2D    UP = new Vector2D( 0, -1);
	public static final Vector2D  DOWN = new Vector2D( 0, +1);
	public static final Vector2D  LEFT = new Vector2D(-1,  0);
	public static final Vector2D RIGHT = new Vector2D(+1,  0);
	public static final Gem WALL = new WallGem();
	private final Vector2D START_TOP;
	private final Vector2D START_BOT;
	public GemPair previewgem;

	private final static Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
	private Random randSrc = new Random();

	protected int gridWidth;
	protected int gridHeight;
	private int turnScore;
	private int gemCount;
	protected boolean secondary;
	private Socket socket;
	private ObjectOutputStream oos;
	protected ColoredGem[][] grid;
	protected GemPair cursor;

	private long inputTimer = 0;
	private long renderTimer = 0;
	protected short updateCount = 0;

	public int garbage;

	protected RobotMaster fighter;
	
	private Color randomColor() {
		return colors[randSrc.nextInt(colors.length)];
	}

	private ColoredGem randomGem(Vector2D pos) {
		if(gemCount % 25 == 0 && gemCount != 0){
			gemCount++;
			return new Diamond(this, pos, Color.RED);
		}
		if (randSrc.nextFloat() > 0.25) {
			gemCount++;
			return new PowerGem(this, pos, randomColor());
		}
		else {
			gemCount++;
			return new CrashGem(this, pos, randomColor());
		}
	}

	public PlayField(int width, int height, Socket socket, boolean secondary) throws IOException {
		super(PuzzleFighter.PF_SHEET + "#playfield");
		this.position = new Vector2D(0, 0);
		this.gridWidth = width;
		this.gridHeight = height;
		this.grid = new ColoredGem[height][width];
		this.turnScore = 0;
		this.garbage = 0;
		this.socket = socket;
		this.secondary = secondary;
		START_TOP = new Vector2D(width/2, 0);
		START_BOT = START_TOP.translate(DOWN);

	
		this.cursor = new GemPair(randomGem(START_BOT), randomGem(START_TOP));
		if(secondary){
			previewgem = new GemPair(randomGem(new Vector2D(-2, 8)), randomGem(new Vector2D(-2, 7)));
		}else{
			previewgem = new GemPair(randomGem(new Vector2D(7, 8)), randomGem(new Vector2D(7, 7)));
		}
		if(socket != null) {
			OutputStream os = socket.getOutputStream();
			oos = new ObjectOutputStream(os);
			oos.flush();
		}

		this.fighter = !secondary ? new CutMan() : new MegaMan();
		this.secondary = secondary;
	}

	public int getWidth() {
		return gridWidth;
	}

	public int getHeight() {
		return gridHeight;
	}
	
	public void render(RenderingContext rc) {
		super.render(rc);
		for (int y = 0; y < gridHeight; y++) {
			for (int x = 0; x < gridWidth; x++) {
				Gem g = grid[y][x];
				if (g != null && (cursor == null || !cursor.contains(g))) {
					g.render(rc);
				}
			}
		}
		if(previewgem != null){
			previewgem.render(rc);
		}
		if (cursor != null) {
			cursor.render(rc);
		}
		fighter.render(rc, !secondary);
	}

	public Gem ref(Vector2D pos) {
		int x = (int) pos.getX();
		int y = (int) pos.getY();
		if (x < 0 || x >= gridWidth || y < 0 || y >= gridHeight) {
			return WALL;
		}

		return grid[y][x];
	}

	public void set(Vector2D pos, ColoredGem g) {
		int x = (int) pos.getX();
		int y = (int) pos.getY();

		grid[y][x] = g;
	}

	public void clear(Vector2D pos) {
		set(pos, null);
	}

	public boolean isFilled(Vector2D pos) {
		return (ref(pos) != null);
	}

	public boolean move(Vector2D dv) {
		return cursor.move(dv);
	}

	public void step() {
		if (!cursor.move(DOWN)) {
			cursor = null;
			stepTimers();
		}
	}

	public boolean fall() {
		boolean hadEffect = false;

		for (int y = gridHeight-1; y >= 0; y--) {
			for (int x = 0; x < gridWidth; x++) {
				if (grid[y][x] != null) {
					hadEffect |= grid[y][x].move(DOWN);
				}
			}
		}

		return hadEffect;
	}

	public int crashGems() {
		int crashScore = 0;

		for (int y = gridHeight-1; y >= 0; y--) {
			for (int x = 0; x < gridWidth; x++) {
				ColoredGem g = grid[y][x];
				if (g != null) {
					crashScore += g.endTurn();
				}
			}
		}

		return crashScore;
	}
	
	public int crashDiamonds(){
		int crashScore = 0;
		for(int y = gridHeight -1; y >= 0; y--){
			for(int x = 0; x < gridWidth; x++){
				if(grid[y][x] != null && grid[y][x] instanceof Diamond){
					crashScore += grid[y][x].endTurn();
				}
			}
		}
		return crashScore;
	}

	public void stepTimers() {
		for (int y = gridHeight-1; y >= 0; y--) {
			for (int x = 0; x < gridWidth; x++) {
				ColoredGem g = grid[y][x];
				if (g instanceof TimerGem) {
					TimerGem tg = (TimerGem) g;
					if (tg.stepTimer()) {
						grid[y][x] = new PowerGem(this, tg.pos, tg.color);
					}
				}
			}
		}
	}
	
	public void combine(){
		for(int x = 0; x < gridWidth; x++){
			for(int y = 0; y < gridHeight; y++){
				if(grid[y][x] instanceof PowerGem){
					((PowerGem)grid[y][x]).combine();
				}
			}
		}	
	}
	
	public boolean gravitate() {
		if (fall()) {
			return true;
		}
		int crashScore = crashDiamonds();
		crashScore += crashGems();
		if (crashScore != 0) {
			turnScore *= 2;
			turnScore += crashScore;
			return true;
		}

		return false;
	}
	
	public int update(long deltaMs, boolean down, boolean left, boolean right, boolean ccw, boolean cw) {
		updateCount++;
		fighter.update(deltaMs);
		renderTimer += deltaMs;
		inputTimer += deltaMs;

		if (inputTimer > 100) {
			inputTimer = 0;
			if (cursor != null) {

				if (ccw && !cw) {
					cursor.rotateCounterClockwise();
				}
					
				if (cw && !ccw) {
					cursor.rotateClockwise();
				}
				if (down) {
					move(PlayField.DOWN);
				}
				if (left && !right) {
					move(PlayField.LEFT);
				}
				if (right && !left) {
					move(PlayField.RIGHT);
				}
			}
		}

		if (cursor != null && renderTimer > 500) {
			renderTimer = 0;
			step();
		}
		if (cursor == null && renderTimer > 100) {
			renderTimer = 0;
			boolean moreToDo = gravitate();
			combine();
			if (!moreToDo) {
				if (garbage > 0) {
					garbage /= 2;
					for (int i = 0; i < garbage; i++) {
						grid[i / gridWidth][i % gridWidth] = new TimerGem(this, new Vector2D(i%gridWidth,i/gridWidth), Color.RED);
					}
					garbage = 0;
					fighter.attack();
					if(socket != null){
						try {
							oos.writeInt(1);
						} catch (IOException ex) {
							Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
						}
						netsend(0, true);
					}
					return 0;
				}
				else {
					int tmp = turnScore;
					turnScore = 0;
					if(ref(START_BOT) != null){
						tmp = -1;
					}else{
						//cursor = new GemPair(randomGem(START_BOT), randomGem(START_TOP));
						cursor = previewgem;
						previewgem.gem1.pos = START_BOT;
						previewgem.gem2.pos = START_TOP;
						if(secondary){
							previewgem = new GemPair(randomGem(new Vector2D(-2, 8)), randomGem(new Vector2D(-2, 7)));
						}else{
							previewgem = new GemPair(randomGem(new Vector2D(7, 8)), randomGem(new Vector2D(7, 7)));
						}
					}
					
					if(socket != null){
						try {
							oos.writeInt(1);
						} catch (IOException ex) {
							Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
						}
						netsend(tmp, false);
					}
					return tmp;
				}
			}
		}
		if(socket != null && updateCount > 20){
			try {
				oos.writeInt(1);
			} catch (IOException ex) {
				Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
			}
			netsend(0, false);
			updateCount = 0;
		}
		return 0;
	}
	
	public void netsend(int garb, boolean attacking){
		ArrayList<ColoredGem> list = new ArrayList();
		Packet thepacket = new Packet();
		thepacket.garbage = garb;
		thepacket.attacking = attacking;
		thepacket.grid = new SerializableGem[gridHeight][gridWidth];
		for(int i = 0; i < gridHeight; i++){
			for(int j = 0; j < gridWidth; j++){
				if(grid[i][j] != null && !list.contains(grid[i][j])){
					thepacket.grid[i][j] = new SerializableGem();
					thepacket.grid[i][j].x = (int)grid[i][j].pos.getX();
					thepacket.grid[i][j].y = (int)grid[i][j].pos.getY();
					thepacket.grid[i][j].color = grid[i][j].color;
					if(grid[i][j] instanceof Diamond){
						thepacket.grid[i][j].type = "Diamond";
					}else if(grid[i][j] instanceof PowerGem){
						thepacket.grid[i][j].type = "Power";
						thepacket.grid[i][j].height = ((PowerGem)grid[i][j]).gemHeight;
						thepacket.grid[i][j].width = ((PowerGem)grid[i][j]).gemWidth;
					}else if(grid[i][j] instanceof CrashGem){
						thepacket.grid[i][j].type = "Crash";
					}else{
						thepacket.grid[i][j].type = "Time";
						thepacket.grid[i][j].frame = ((TimerGem)grid[i][j]).frame;
					}
					list.add(grid[i][j]);
				}
			}
		}
		try {
			oos.writeObject(thepacket);
		} catch (IOException ex) {
			Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void close(){
		if(oos != null){
			try {
				oos.close();
			} catch (IOException ex) {
				Logger.getLogger(PlayField.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
