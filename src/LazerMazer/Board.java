
/*
 * This is the main JPanel paint class for the game
 * Every time the gameLoop() updates with this.setContentPane(Board)
 * An new instance of this class is constructed with
 * the updated map, player and laser classes to be painted
 * 
 * Every instance of this class creates only ONE frame in the game
 */

package LazerMazer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Board extends JPanel {
	private static final int dist_pixels = 60;
	private static final int dist_pixels_player = 85;
	private static final int dist_pixels_sqr = dist_pixels * dist_pixels;
	private static final int hp_bar_width = 45;

	private Map m;
	private Player p;
	private Laser l;
	private ArrayList<Enemy> e;
	
	public Board(Map M, Player P, Laser L, ArrayList<Enemy> E) throws IOException {
		m = M; //shallow copy
		p = P; //shallow copy
		l = new Laser(L);
		e = new ArrayList<Enemy>(E);
	} //Board constructor
		
	//find the SQUARED distance between (X3,Y3) to line segment (X1,Y1)-(X2,Y2)
	//avoiding Math.sqrt() makes the program a bit faster
	private int sqrDist(int X3, int Y3, int X1, int Y1, int X2, int Y2) {
	    int px = X2 - X1, py = Y2 - Y1;
	    if ((px*px + py*py) == 0) return 0;
	    double u = (double)((X3 - X1)*px + (Y3 - Y1)*py) / (px*px + py*py);
	    if (u > 1) u = 1;
	    else if (u < 0) u = 0;
	    double dx = (X1 + u * px) - X3;
	    double dy = (Y1 + u * py) - Y3;
	    return (int)(dx*dx + dy*dy);
	} //distance method
	
	public void paint(Graphics g) {		
		//paint the map
        for (int r = 0; r < Map.ROWS; r++)
            for (int c = 0; c < Map.COLS; c++) {
            	
            	if (Main.GAME_STATE == Main.state.WIN) {
            		g.drawImage(m.getImage(r, c), c*8, r*8, null); //draw the actual map
            		continue;
            	}
         
            	boolean close_enough = false;
            	int minSqrDist = (int)1E15, temp;
            	
            	//this is for the spotlight around the player
            	close_enough |= Math.hypot(p.getX() - c*8 + 10, p.getY() - r*8 + 8) < dist_pixels_player;
            	for (int i = 0; i < l.segments(); i++) {
            		temp = sqrDist(c*8, r*8, l.get(i).x1, l.get(i).y1, l.get(i).x2, l.get(i).y2);
            		if (temp < dist_pixels_sqr) {
            			if (temp < minSqrDist) minSqrDist = temp;
            			close_enough = true;
            		}
            	}
            	if (!close_enough)
            		g.drawImage(m.getBlackImage(), c*8, r*8, null); //draw a black unit
            	else {
            		g.drawImage(m.getImage(r, c), c*8, r*8, null); //draw the actual map
            	}
            }
        
        //paint the enemies
        for (int i = 0; i < e.size(); i++)
        	g.drawImage(e.get(i).getImage(), e.get(i).x, e.get(i).y, null);

        //paint the enemy HP bars
        for (Enemy E : e) {
            g.setColor(Color.black);
            g.drawRect(E.x + 10 - hp_bar_width/2 - 1, E.y - 15 - 1, (int)(hp_bar_width) + 1, 5 + 1);
            g.setColor(new Color(220, 0, 0));
            g.fillRect(E.x + 10 - hp_bar_width/2, E.y - 15, (int)(hp_bar_width*((double)E.getHealth() / 100)), 5);
        }
        
        if (Main.GAME_STATE != Main.state.WIN) {
	        //paint the shadow
	        for (int r = 0; r < Map.ROWS; r++)
	            for (int c = 0; c < Map.COLS; c++) {
	            	
	            	boolean close_enough = false;
	            	int minSqrDist = (int)1E15, temp;
	            	
	            	//this is for the spotlight around the player
	            	close_enough |= Math.hypot(p.getX() - c*8 + 10, p.getY() - r*8 + 8) < dist_pixels_player;
	            	//check for collision with the lasers
	            	for (int i = 0; i < l.segments(); i++) {
	            		temp = sqrDist(c*8, r*8, l.get(i).x1, l.get(i).y1, l.get(i).x2, l.get(i).y2);
	            		if (temp < dist_pixels_sqr) {
	            			if (temp < minSqrDist) minSqrDist = temp;
	            			close_enough = true;
	            		}
	            	}
	            	if (minSqrDist == (int)1e15)
	            		minSqrDist = 2*((p.getX() - c*8 + 10)*(p.getX() - c*8 + 10) + (p.getY() - r*8 + 8)*(p.getY() - r*8 + 8));
	            	if (minSqrDist > dist_pixels_sqr) minSqrDist = dist_pixels_sqr;
	            	if (!close_enough)
	            		g.drawImage(m.getBlackImage(), c*8, r*8, null); //draw a black unit
	            	else {
	            		g.drawImage(m.getShadow((double)minSqrDist / dist_pixels_sqr), c*8, r*8, null); //draw the shadow
	            	}
	            }
        }
        
        Graphics2D g2 = (Graphics2D)g;

        //paint the character's health bar
        g.setColor(Color.black);
        g.drawRect(p.getX() + 10 - hp_bar_width/2 - 1, p.getY() - 15 - 1, (int)(hp_bar_width) + 1, 5 + 1);
        g.setColor(new Color(220, 0, 0));
        g.fillRect(p.getX() + 10 - hp_bar_width/2, p.getY() - 15, (int)(hp_bar_width*((double)p.getHealth() / 100)), 5);
        //paint the energy bar
        g.setColor(Color.blue);
        g.fillArc(p.getX() - 5, p.getY() - 5, 34, 34, 0, (int)(p.getEnergy() * 0.01 * 360));
        //paint the small space between the character and energy bar
        g.setColor(Color.gray);
        g.fillOval(p.getX() - 1, p.getY() - 1, 26, 26);
        
        //paint the laser
        drawLaser(g2);
        
        //paint the character
        g.drawImage(p.getImage(), p.getX(), p.getY(), null);
        
        if (Main.GAME_STATE != Main.state.PLAY) {
	        //paint a win message if the character won or lost
	        g.setColor(Color.red);
	        g.fillRect(0, 300, Main.WIDTH, 60);
	        g.fillRoundRect(400, 355, 150, 50, 10, 10);
        	g.setColor(Color.white);
        	g.setFont(new Font("Verdana", Font.PLAIN, 25));
        	
            //Either draw Cleared or Failed on each map if the game ended
	        if (Main.GAME_STATE == Main.state.WIN) {
	        	g.drawString("Level Cleared", 392, 335);
	        	g.setFont(new Font("Verdana", Font.PLAIN, 10));
	        	g.drawString(String.format("Time: %.1f", Main.elapsedTime/1E3), 450, 350);
	        	Main.next.draw(g); //paint the next level button
	        } else if (Main.GAME_STATE == Main.state.LOSE) {
	        	g.drawString("Level Failed", 405, 340);
	        }
	        Main.replay.draw(g); //paint the replay button
        } else {
            //paint the time
            g.setFont(new Font("Verdana", Font.BOLD, 10));  
            g.setColor(Color.red);
            g.drawString(String.format("Time: %.1f", Main.elapsedTime/1E3), 10, 700);
        }
        Main.back.draw(g); //paint the back button
	} //paint method
	
	public void drawLaser(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //output each laser in the ArrayList
        for (int i = 0; i < l.segments(); i++) {
        	//paint a larger laser underneath for decoration
            g2.setColor(Color.blue);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        	g2.drawLine(l.get(i).x1, l.get(i).y1, l.get(i).x2, l.get(i).y2);
        	
            //paint the laser
        	g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
        	g2.drawLine(l.get(i).x1, l.get(i).y1, l.get(i).x2, l.get(i).y2);
        }
	} //drawLaser method
	
} //Board class
