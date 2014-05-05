package cs447.PuzzleFighter;


import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jig.engine.FontResource;

import jig.engine.RenderingContext;
import jig.engine.ResourceFactory;
import jig.engine.hli.StaticScreenGame;

public class PuzzleFighter extends StaticScreenGame {
	final static double SCALE = 1;

	AffineTransform LEFT_TRANSFORM;
	AffineTransform RIGHT_TRANSFORM;

	public final static int height = (int) (416 * SCALE);
	public final static int width = (int) (700 * SCALE);
	
	private FontResource font = ResourceFactory.getFactory().getFontResource(new Font("Sans Serif", Font.BOLD, 30), java.awt.Color.red, null);
	private String host;
	private int key;
	private long timer;

	private PlayField pfLeft;
	private PlayField pfRight;
	
	public Socket socket = null;
	public ServerSocket serv = null;

	private boolean playing = false;
	private boolean editingText = false;

	final static String RSC_PATH = "cs447/PuzzleFighter/resources/";
	final static String GEM_SHEET = RSC_PATH + "gems.png";
	final static String CUT_SHEET = RSC_PATH + "cutman.png";
	final static String MEGA_SHEET = RSC_PATH + "megaman.png";
	final static String PF_SHEET = RSC_PATH + "playfield.png";

	public static void main(String[] args) throws IOException {
		PuzzleFighter game = new PuzzleFighter();
		game.run();
	}

	public PuzzleFighter() throws IOException {
		super(width, height, false);
		ResourceFactory.getFactory().loadResources(RSC_PATH, "resources.xml");

		LEFT_TRANSFORM = AffineTransform.getScaleInstance(SCALE, SCALE);
		RIGHT_TRANSFORM = (AffineTransform) LEFT_TRANSFORM.clone();
		RIGHT_TRANSFORM.translate(508, 0);
	}

	private void localMultiplayer() throws IOException {
		socket = null;
		pfLeft = new PlayField(6, 13, socket, false);
		pfRight = new PlayField(6, 13, socket, true);
	}

	public void remoteClient(String host) throws IOException {
		connectTo(host);

		pfLeft = new PlayField(6, 13, socket, false);
		pfRight = new RemotePlayfield(6, 13, socket);
	}

	public void remoteServer() throws IOException {
		host();
		
		pfLeft = new PlayField(6, 13, socket, false);
		pfRight = new RemotePlayfield(6, 13, socket);
	}

	public void render(RenderingContext rc) {
		super.render(rc);
		if (playing) {
			rc.setTransform(LEFT_TRANSFORM);
			pfLeft.render(rc);
			rc.setTransform(RIGHT_TRANSFORM);
			pfRight.render(rc);
			return;
		}
		else if (editingText) {
			font.render("Enter hostname:", rc, AffineTransform.getTranslateInstance(280, 100));
			font.render(host, rc, AffineTransform.getTranslateInstance(280, 150));
		}
		else {
			font.render("Puzzle Fighter", rc, AffineTransform.getTranslateInstance(280, 100));
			font.render("1 - local multiplayer", rc, AffineTransform.getTranslateInstance(280, 150));
			font.render("2 - remote host", rc, AffineTransform.getTranslateInstance(280, 200));
			font.render("3 - remote client", rc, AffineTransform.getTranslateInstance(280, 250));
		}
	}

	public void update(long deltaMs) {
		if (playing) {
			boolean down1 = keyboard.isPressed(KeyEvent.VK_S);
			boolean left1 = keyboard.isPressed(KeyEvent.VK_A);
			boolean right1 = keyboard.isPressed(KeyEvent.VK_D);
			boolean ccw1 = keyboard.isPressed(KeyEvent.VK_Q);
			boolean cw1 = keyboard.isPressed(KeyEvent.VK_E);
			int garbage = pfLeft.update(deltaMs, down1, left1, right1, ccw1, cw1);
			pfRight.garbage += garbage;

			boolean down2 = keyboard.isPressed(KeyEvent.VK_K);
			boolean left2 = keyboard.isPressed(KeyEvent.VK_J);
			boolean right2 = keyboard.isPressed(KeyEvent.VK_L);
			boolean ccw2 = keyboard.isPressed(KeyEvent.VK_U);
			boolean cw2 = keyboard.isPressed(KeyEvent.VK_O);
			int garbage2 = pfRight.update(deltaMs, down2, left2, right2, ccw2, cw2);
			pfLeft.garbage += garbage2;
			if(garbage2 == -1 || garbage == -1){
				pfLeft.close();
				pfRight.close();
				playing = false;
				if(socket != null){
					try {
						socket.close();
					} catch (IOException ex) {
						Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
			return;
		}
		
		if (editingText) {
			timer += deltaMs;
			if (timer > 100) {
				if (keyboard.isPressed(KeyEvent.VK_A)) key = KeyEvent.VK_A;
				if (keyboard.isPressed(KeyEvent.VK_B)) key = KeyEvent.VK_B;
				if (keyboard.isPressed(KeyEvent.VK_C)) key = KeyEvent.VK_C;
				if (keyboard.isPressed(KeyEvent.VK_D)) key = KeyEvent.VK_D;
				if (keyboard.isPressed(KeyEvent.VK_E)) key = KeyEvent.VK_E;
				if (keyboard.isPressed(KeyEvent.VK_F)) key = KeyEvent.VK_F;
				if (keyboard.isPressed(KeyEvent.VK_G)) key = KeyEvent.VK_G;
				if (keyboard.isPressed(KeyEvent.VK_H)) key = KeyEvent.VK_H;
				if (keyboard.isPressed(KeyEvent.VK_I)) key = KeyEvent.VK_I;
				if (keyboard.isPressed(KeyEvent.VK_J)) key = KeyEvent.VK_J;
				if (keyboard.isPressed(KeyEvent.VK_K)) key = KeyEvent.VK_K;
				if (keyboard.isPressed(KeyEvent.VK_L)) key = KeyEvent.VK_L;
				if (keyboard.isPressed(KeyEvent.VK_M)) key = KeyEvent.VK_M;
				if (keyboard.isPressed(KeyEvent.VK_N)) key = KeyEvent.VK_N;
				if (keyboard.isPressed(KeyEvent.VK_O)) key = KeyEvent.VK_O;
				if (keyboard.isPressed(KeyEvent.VK_P)) key = KeyEvent.VK_P;
				if (keyboard.isPressed(KeyEvent.VK_Q)) key = KeyEvent.VK_Q;
				if (keyboard.isPressed(KeyEvent.VK_R)) key = KeyEvent.VK_R;
				if (keyboard.isPressed(KeyEvent.VK_S)) key = KeyEvent.VK_S;
				if (keyboard.isPressed(KeyEvent.VK_T)) key = KeyEvent.VK_T;
				if (keyboard.isPressed(KeyEvent.VK_U)) key = KeyEvent.VK_U;
				if (keyboard.isPressed(KeyEvent.VK_V)) key = KeyEvent.VK_V;
				if (keyboard.isPressed(KeyEvent.VK_W)) key = KeyEvent.VK_W;
				if (keyboard.isPressed(KeyEvent.VK_X)) key = KeyEvent.VK_X;
				if (keyboard.isPressed(KeyEvent.VK_Y)) key = KeyEvent.VK_Y;
				if (keyboard.isPressed(KeyEvent.VK_Z)) key = KeyEvent.VK_Z;
	
				if (keyboard.isPressed(KeyEvent.VK_0))       key = KeyEvent.VK_0;
				if (keyboard.isPressed(KeyEvent.VK_1))       key = KeyEvent.VK_1;
				if (keyboard.isPressed(KeyEvent.VK_2))       key = KeyEvent.VK_2;
				if (keyboard.isPressed(KeyEvent.VK_3))       key = KeyEvent.VK_3;
				if (keyboard.isPressed(KeyEvent.VK_4))       key = KeyEvent.VK_4;
				if (keyboard.isPressed(KeyEvent.VK_5))       key = KeyEvent.VK_5;
				if (keyboard.isPressed(KeyEvent.VK_6))       key = KeyEvent.VK_6;
				if (keyboard.isPressed(KeyEvent.VK_7))       key = KeyEvent.VK_7;
				if (keyboard.isPressed(KeyEvent.VK_8))       key = KeyEvent.VK_8;
				if (keyboard.isPressed(KeyEvent.VK_9))       key = KeyEvent.VK_9;
				if (keyboard.isPressed(KeyEvent.VK_DECIMAL)) key = KeyEvent.VK_DECIMAL;
				if (keyboard.isPressed(KeyEvent.VK_MINUS))   key = KeyEvent.VK_MINUS;
				
				if (keyboard.isPressed(KeyEvent.VK_BACK_SPACE)) key = KeyEvent.VK_BACK_SPACE;
				if (keyboard.isPressed(KeyEvent.VK_ENTER)) key = KeyEvent.VK_ENTER;
			}
			
			if (timer > 500) {
				if (key == KeyEvent.VK_A) host = host + 'a';
				if (key == KeyEvent.VK_B) host = host + 'b';
				if (key == KeyEvent.VK_C) host = host + 'c';
				if (key == KeyEvent.VK_D) host = host + 'd';
				if (key == KeyEvent.VK_E) host = host + 'e';
				if (key == KeyEvent.VK_F) host = host + 'f';
				if (key == KeyEvent.VK_G) host = host + 'g';
				if (key == KeyEvent.VK_H) host = host + 'h';
				if (key == KeyEvent.VK_I) host = host + 'i';
				if (key == KeyEvent.VK_J) host = host + 'j';
				if (key == KeyEvent.VK_K) host = host + 'k';
				if (key == KeyEvent.VK_L) host = host + 'l';
				if (key == KeyEvent.VK_M) host = host + 'm';
				if (key == KeyEvent.VK_N) host = host + 'n';
				if (key == KeyEvent.VK_O) host = host + 'o';
				if (key == KeyEvent.VK_P) host = host + 'p';
				if (key == KeyEvent.VK_Q) host = host + 'q';
				if (key == KeyEvent.VK_R) host = host + 'r';
				if (key == KeyEvent.VK_S) host = host + 's';
				if (key == KeyEvent.VK_T) host = host + 't';
				if (key == KeyEvent.VK_U) host = host + 'u';
				if (key == KeyEvent.VK_V) host = host + 'v';
				if (key == KeyEvent.VK_W) host = host + 'w';
				if (key == KeyEvent.VK_X) host = host + 'x';
				if (key == KeyEvent.VK_Y) host = host + 'y';
				if (key == KeyEvent.VK_Z) host = host + 'z';

				if (key == KeyEvent.VK_0)       host = host + "0";
				if (key == KeyEvent.VK_1)       host = host + "1";
				if (key == KeyEvent.VK_2)       host = host + "2";
				if (key == KeyEvent.VK_3)       host = host + "3";
				if (key == KeyEvent.VK_4)       host = host + "4";
				if (key == KeyEvent.VK_5)       host = host + "5";
				if (key == KeyEvent.VK_6)       host = host + "6";
				if (key == KeyEvent.VK_7)       host = host + "7";
				if (key == KeyEvent.VK_8)       host = host + "8";
				if (key == KeyEvent.VK_9)       host = host + "9";
				if (key == KeyEvent.VK_DECIMAL) host = host + ".";
				if (key == KeyEvent.VK_MINUS)   host = host + "-";

				if (key == KeyEvent.VK_BACK_SPACE && host.length() > 0) host = host.substring(0, host.length() - 1);
				if (key == KeyEvent.VK_ENTER) {
					editingText = false;
					playing = true;
					try {
						remoteClient(host);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				key = KeyEvent.VK_ESCAPE;
				timer = 0;
			}

			return;
		}
		
		if (keyboard.isPressed(KeyEvent.VK_1)) {
			try {
				localMultiplayer();
				playing = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (keyboard.isPressed(KeyEvent.VK_2)) {
			try {
				remoteServer();
				playing = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (keyboard.isPressed(KeyEvent.VK_3)) {
			host = "";
			editingText = true;
		}
	}
	
	public void connectTo(String host){
		try {
			socket = new Socket(host, 50623);
		} catch (UnknownHostException ex) {
			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void host(){
		try {
			serv = new ServerSocket(50623);
			socket = serv.accept();
			serv.close();
		} catch (IOException ex) {
			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
