/*
 * The main game runs from here
 */
package LazerMazer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class Main extends JFrame {
    
    //JFrame properties
	public static final int WIDTH  = 960;
	public static final int HEIGHT = WIDTH/4 * 3;
	private static final int UPS = 60; //updates per second
	
	//global variables for inputs from the user
	private int clickX, clickY, X, Y; //the current location of the mouse and the last point that was clicked 
	private boolean mouseHeld; //true if mouse button is held
	private boolean[] pressed = new boolean[255]; //keys
	private static final int mouseOffsetX = -15; //funky offsets
	private static final int mouseOffsetY = -43;

	//music files
	private AudioInputStream audioIn;
	private Clip menutheme, gametheme;
    
	//buttons
	public static Button play, instructions, credits, quit, back, next, replay;
	
	private String mapName; //the path of the map currently open
	private int level; //the current level/map
	private static final int startLevel = 1; //level to start on (for debugging)
	private static final int maxlevels = 20; //returns to beginning when maxlevels is done
	
	//stores the amount of time elapsed since the start of each level in milliseconds
	//this is used for high scores, not UPS/FPS management
	public static long startTime, elapsedTime;

	//global variables for the current state of the program
	public enum state { MENU, INSTRUCTIONS, CREDITS, PLAY, WIN, LOSE, QUIT };
	public static state GAME_STATE;
	
	//global variables for the game objects
	public static Map m;
	public static Laser l;
	public static Player p;
	public static ArrayList<Enemy> e;
	
	private ScoreManager hs; //high score IO class
	private ScoreFrame sf; //JFrame to display high scores
	
	public static void main(String[] args) throws IOException, InterruptedException {
		new Main(); //instantiate this
	} //main method
	
	public Main() throws IOException, InterruptedException {
		
		this.getContentPane().add(new JLabel(new ImageIcon("images\\loading.png")));
		
		//Deset the keyboard presses when window is out of focus
		addWindowListener(new WindowListener() {
		    public void windowDeactivated(WindowEvent e) {
		        Arrays.fill(pressed, false);
		    }
			public void windowActivated(WindowEvent arg0) { }
			public void windowClosed(WindowEvent arg0) { }
			public void windowClosing(WindowEvent arg0) { }
			public void windowDeiconified(WindowEvent arg0) { }
			public void windowIconified(WindowEvent arg0) { }
			public void windowOpened(WindowEvent arg0) { }
		});
		
		//Use a MouseListener to update the last place clicked
        addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                clickX = e.getX() + mouseOffsetX;
                clickY = e.getY() + mouseOffsetY;
                //System.out.printf("clicked %d %d\n", e.getX(), e.getY());
            }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
            public void mousePressed(MouseEvent e) { mouseHeld = true; }
            public void mouseReleased(MouseEvent e) { mouseHeld = false; }
        });
        
        //Use a MouseMotionListener to update the current location of the mouse
        addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                X = e.getX() + mouseOffsetX;
                Y = e.getY() + mouseOffsetY;
                //System.out.printf("%d %d\n", e.getX(), e.getY());
            }
            public void mouseDragged(MouseEvent e) {
            	X = e.getX() + mouseOffsetX;
                Y = e.getY() + mouseOffsetY;
            }
        });
        
        //Listen for key presses and key releases
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if (key < 100) pressed[key] = true;
            }
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key < 100) pressed[key] = false;
            }
            public void keyTyped(KeyEvent e) {}
        });

        //the offsets for the title bar height
        //I don't know how to set it in a smart way
        //Just hard code values from each operating system in
        int titlebar_height = 28;
        if (System.getProperty("os.name").equals("Windows XP"))
        	titlebar_height = 32;
        else if (System.getProperty("os.name").equals("Windows 7"))
        	titlebar_height = 28;
        else if (System.getProperty("os.name").equals("Windows 8"))
        	titlebar_height = 29;
        
        //Boilerplate stuff to setup the main game window
        this.setTitle("LazerMazer");
        this.setSize(WIDTH + 6, HEIGHT + titlebar_height);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);
        this.createBufferStrategy(3); //triple buffer
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainLoop();
        
        //Close the window
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
        
	} //Main constructor
	
	private void mainLoop() throws IOException, InterruptedException {
		
		try {
            //load the main menu music
		    audioIn = AudioSystem.getAudioInputStream(new File("misc\\Gilded Darkness.mid")); 
		    menutheme = AudioSystem.getClip();
		    menutheme.open(audioIn);
		    
		    //load the game-play music
		    audioIn = AudioSystem.getAudioInputStream(new File("misc\\Broken Umbrella.mid")); 
		    gametheme = AudioSystem.getClip();
		    gametheme.open(audioIn);
		    
		    //adjust volumes
		    ((FloatControl)menutheme.getControl(FloatControl.Type.MASTER_GAIN)).setValue(-10.0f);
		    ((FloatControl)gametheme.getControl(FloatControl.Type.MASTER_GAIN)).setValue(6f);
		} catch (Exception e) {
		    System.err.println("Error loading music");
		    e.printStackTrace();
		}
		
		level = startLevel; //starting level
		GAME_STATE = state.MENU; //starting state
        
        //The main loop
		while (true) {
			
            //Check the current state of the game and decide how to evaluate it
			if (GAME_STATE == state.MENU) {
				
                //change the game music
				gametheme.stop();
				menutheme.loop(Clip.LOOP_CONTINUOUSLY);
				
				//custom button (Text, FontFileName, FontSize, X, Y, Width, Height)
				//width and height must be found manually and hard-coded
				play = new Button("Play", null, 40, 432, 500, 100, 35);
				instructions = new Button("Instructions", null, 40, 360, 555, 250, 35);
				instructions.shadowOffsetX = -7;
				instructions.shadowOffsetFontSize = -2;
				credits = new Button("Credits", null, 40, 405, 605, 150, 35);
				credits.shadowOffsetX = -4;
				quit = new Button("Quit", null, 40, 435, 660, 100, 35);
				
				clickX = clickY = -1; //set buttons to not clicked
				
				gameLoop();
				
			} //MENU
			
			else if (GAME_STATE == state.INSTRUCTIONS) {
				
				back = new Button("< Back", null, 40, 80, 80, 145, 35);
				gameLoop();
				
			} //INSTRUCTIONS
			
			else if (GAME_STATE == state.CREDITS) {
				
				back = new Button("< Back", null, 40, 80, 80, 145, 35);
				gameLoop();
				
			} //CREDITS
			
			else if (GAME_STATE == state.PLAY) {
				
				//custom button (Text, FontFileName, FontSize, X, Y, Width, Height)
				back = new Button("< Back to Menu", null, 26, 15, 337, 190, 35);
				back.drawShadow = false;
				back.Neutral = Color.blue;
				back.Hovered = Color.cyan;
				next = new Button("Next >", null, 26, 850, 337, 80, 35);
				next.drawShadow = false;
				next.Neutral = Color.blue;
				next.Hovered = Color.cyan;
				replay = new Button("Replay", null, 30, 425, 385, 105, 38);
				replay.drawShadow = false;
				replay.Neutral = Color.blue;
				replay.Hovered = Color.cyan;
				
                //change the game music
				menutheme.stop();
				gametheme.loop(Clip.LOOP_CONTINUOUSLY);
				
				mapName = "maps\\map" + level + ".txt"; //load the level and other classes
				m = new Map(mapName);
				p = new Player(m.startX, m.startY);
				e = new ArrayList<Enemy>();
				l = new Laser(m, e);
				
				//Load the enemies
				Scanner in = new Scanner(new FileInputStream(mapName));
				try {
					while (in.nextLine().indexOf("[Enemy Locations]") == -1);
					if (!in.hasNextInt()) throw new Exception();
					int numEnemies = in.nextInt();
					for (int i = 0; i < numEnemies; i++) {
						Y = in.nextInt() * 8; //scan and scale the row
						X = in.nextInt() * 8; //scan and scale the column
						e.add(new Enemy(X, Y)); //arbitrarily add an enemy to test
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				startTime = System.currentTimeMillis();
				
				gameLoop();
				
			} //PLAY
			
			else if (GAME_STATE == state.WIN) {
				
				syncButton(back);
				syncButton(replay);
				syncButton(next);
				
				Arrays.fill(pressed, false);
				
				//load high score manager class and high score window
				hs = new ScoreManager("hiscores\\level" + level + ".dat");
				sf = new ScoreFrame(hs.getFormattedString(String.format("%11s%s\n\n", "", "Level " + level + " High Scores")));
				
				
				gameLoop();
				sf.dispose(); //get rid of the score frame
				
				//move on to the next level
				level++;
				if (level > maxlevels) level = startLevel;
				
			} //WIN
			
			else if (GAME_STATE == state.LOSE) {
				
				syncButton(back);
				syncButton(replay);
				
				gameLoop();

			} //LOSE
			
			else if (GAME_STATE == state.QUIT) {
				break;
			} //QUIT
			
		} //game loop
		
	} //gameLoop method
	
	private void update() {
		//call each subroutine to update accordingly
        if (GAME_STATE == state.PLAY) {
    		updatePlay();
    	} else if (GAME_STATE == state.MENU) {
    		updateMenu();
    	} else if (GAME_STATE == state.INSTRUCTIONS) {
    		updateInstructions();
    	} else if (GAME_STATE == state.CREDITS) {
    		updateCredits();
    	} else if (GAME_STATE == state.WIN) {
    		updateWin();
    	} else if (GAME_STATE == state.LOSE) {
    		updateLose();
    	} 
	} //update method
	
	private void updatePlay() {
        //update the time elapsed
		elapsedTime = System.currentTimeMillis() - startTime;
		
		//slowly deplete the health bar
		p.depleteHealth(0.01);
		
		//update the energy bar
		if (mouseHeld) {
			p.dischargeEnergy();
		} else {
			p.rechargeEnergy();
		}
		
		//update the enemies
		for (int i = 0; i < e.size(); i++) {
			if (e.get(i).getHealth() == 0) {
				e.remove(i);
				continue;
			}
			if (e.get(i).collided(p)) {
				p.depleteHealth(1);
			} else {
				e.get(i).updateLocation(m, p);
			}
		}
		
		//update the player
		p.updateLocation(m, pressed);
		p.rotateToFace(X, Y);
		
		//update the laser
		if (mouseHeld && p.getEnergy() > 0) {
			l.update(p.getX() + 12, p.getY() + 12, X + 12, Y + 16); //hardcoded offsets
		} else
			l.clear();
		
        //check if the player is dead
		if (p.getHealth() == 0) {
			GAME_STATE = state.LOSE;
		} else if (m.at(p.getX(), p.getY()) == 'e') {
			GAME_STATE = state.WIN;
		}
	}
	
	private void updateMenu() {
		if (syncButton(play)) {
			GAME_STATE = state.PLAY;
			level = startLevel;
			gametheme.setFramePosition(0);
		} else if (syncButton(instructions)) {
			GAME_STATE = state.INSTRUCTIONS;
		} else if (syncButton(credits)) {
			GAME_STATE = state.CREDITS;
		} else if (syncButton(quit)) {
			GAME_STATE = state.QUIT;
		}
	} //updateMenu method
	
	private void updateWin() {
		if (syncButton(back)) {
			GAME_STATE = state.MENU;
			menutheme.setFramePosition(0);
		} else if (syncButton(next)) {
			GAME_STATE = state.PLAY;
		} else if (syncButton(replay)) {
			GAME_STATE = state.PLAY;
			level--;
		}
		
		if (sf != null && hs != null && sf.nameEntered()) {
			hs.addScore(sf.name, elapsedTime * 1E-3);
			hs = null;
			sf.dispose();
		}
	} //updateWin method
	
	private void updateLose() {
		if (syncButton(back)) {
			GAME_STATE = state.MENU;
			menutheme.setFramePosition(0);
		} else if (syncButton(replay)) {
			GAME_STATE = state.PLAY;
		}
	} //updateWin method
	
	private void updateInstructions() {
		if (syncButton(back)) {
			GAME_STATE = state.MENU;
		}
	} //updateInstructions method
	
	private void updateCredits() {
		if (syncButton(back)) {
			GAME_STATE = state.MENU;
		}
	} //updateCredits method
	
	private void render() throws Exception {
		
        if (GAME_STATE == state.MENU) {
        	//display the welcome menu
			display(new MainMenu());
        } //display menu
        
        else if (GAME_STATE == state.INSTRUCTIONS) {
        	if (back == null) return;
        	//display the instruction screen
        	display(new Instructions());
        } //display instructions
        
        else if (GAME_STATE == state.CREDITS) {
        	if (back == null) return;
        	//display the instruction screen
        	display(new Credits());
        } //display credits
        
        else if (GAME_STATE == state.PLAY) {
        	//display the game board with updated map, player and laser
			display(new Board(m, p, l, e));
        } //display game
        
        else if (GAME_STATE == state.WIN) {
        	//display the game board with the lose screen
        	display(new Board(m, p, l, e));
		} //display win
        
        else if (GAME_STATE == state.LOSE) {
        	//display the game board with the lose screen
        	display(new Board(m, p, l, e));
		} //display lose
        
	}
	
	private void gameLoop() {
		state ORIGINAL_STATE = GAME_STATE;
        
        double ns = 1000000000.0 / UPS;
        double delta = 0;
        int frames = 0, updates = 0;
        
        long UPSTimer = System.nanoTime();
        long FPSTimer = System.nanoTime();
		while (GAME_STATE == ORIGINAL_STATE) {
            long now = System.nanoTime();
            delta += (now - UPSTimer) / ns;
            UPSTimer = now;
            while (delta >= 1) {
                update();
                delta--;
                updates++;
            }
            //check if state changed to prevent render() nullpointers, etc.
            if (GAME_STATE != ORIGINAL_STATE) break;
            try {
            	render();
            } catch(Exception e) {
            	e.printStackTrace();
            }
            frames++;
            if (System.nanoTime() - FPSTimer > 1000000000) {
                this.setTitle("LazerMazer " + updates + " UPS  ||  " + frames + " FPS");
                FPSTimer += 1000000000;
                frames = 0;
                updates = 0;
            }
		}
	}
	
	private void display(Container arg) {
        this.getContentPane().removeAll();
        this.setContentPane(arg);
        this.validate();
	} //display method
	
	//sync button with the mouse location and click locations
	//returns whether the button is clicked
	private boolean syncButton(Button b) {
		b.updateState(X-mouseOffsetX, Y-mouseOffsetY, mouseHeld);
		if (clickX != -1 && b.inRange(clickX-mouseOffsetX, clickY-mouseOffsetY)) {
			clickX = clickY = -1;
			return true;
		}
		return false;
	} //syncButton method
}