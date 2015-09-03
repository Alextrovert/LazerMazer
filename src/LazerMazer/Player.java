/* 
 * This is the Player class
 * It stores the player's location, angle, health, image and more
 */

package LazerMazer;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player {
	
    //Variables to store the player properties
	private int x, y;
	private double angle;
	private BufferedImage player;
	private AffineTransformOp op;
	private double percent_energy = 100; //% energy from 0 to 100
	private double percent_health = 100; //% health from 0 to 100
	private double energy_recharge_rate  = 0.5; //in percents per update
	private double energy_discharge_rate = 1.0; //in percents per update
	
    public Player(int startX, int startY) {
    	try {
    		player = (BufferedImage)ImageIO.read(new File("images\\player.png"));
	        if (x < 0 || x >= Map.COLS || y < 0 || y >= Map.COLS)
	        	throw new Exception();
	        x = startX;
	        y = startY;
    	} catch (Exception e) {
    		System.out.println("Error loading player at " + startX + ", " + startY);
    	}
    	setAngle(0);
    } //Player constructor
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    public void move(int dx, int dy) {
    	try {
    		if (x + dx < 0 || x + dx >= Map.COLS*8 ||
    			y + dy < 0 || y + dy >= Map.ROWS*8)
    			throw new Exception();
    		x += dx;
    		y += dy;
    	} catch (Exception e) {
    		System.err.println("Error: player moved out of range.");
    		e.printStackTrace();
    	}
    } //move method
    
    public void updateLocation(Map m, boolean[] pressed) {
        int dx = 0, dy = 0;
        
        if (pressed[KeyEvent.VK_W] || pressed[KeyEvent.VK_UP])   dy--;
        if (pressed[KeyEvent.VK_S] || pressed[KeyEvent.VK_DOWN]) dy++;
        if (pressed[KeyEvent.VK_A] || pressed[KeyEvent.VK_LEFT]) dx--;
        if (pressed[KeyEvent.VK_D] || pressed[KeyEvent.VK_RIGHT])dx++;
        
        //collision detection. Trust me, it works
        if (dx != 0 && dy != 0) { 
            dx *= 2; dy *= 2;
        } else {
            dx *= 2; dy *= 2;
        }
        if (dy != 0) {
            move(0, dy);
            if (y < 0 || y > Map.ROWS*8) {
                move(0, -dy);
            } else {
                boolean wall = false;
                for (int xx = 0; xx < 4 && !wall; xx++)
                for (int yy = 0; yy < 4 && !wall; yy++)
                    wall |= m.at(x + 8 * xx, y + 8 * yy) == 'w';
                if (wall) move(0, -dy);
            } 
        }
        if (dx != 0) {
            move(dx, 0);
            if (x >= 0 && x <= Map.COLS*8) {
                boolean wall = false;
                for (int xx = 0; xx < 4 && !wall; xx++)
                for (int yy = 0; yy < 4 && !wall; yy++)
                    wall |= m.at(x + 8 * xx, y + 8 * yy) == 'w';
                if (wall) move(-dx, 0);
            } else move(-dx, 0);
        }
    } //updateLocation method
    
    public void setEnergy(double energy) {
    	percent_energy = energy;
    	if (percent_energy < 0)   percent_energy = 0;
    	if (percent_energy > 100) percent_energy = 100;
    } //setEnergy method
    
    public double getEnergy() {
    	return percent_energy;
    } //getEnergy method
    
    public void rechargeEnergy() {
    	setEnergy(getEnergy() + energy_recharge_rate);
    } //rechargeEnergy method
    
    public void dischargeEnergy() {
    	setEnergy(getEnergy() - energy_discharge_rate);
    } //dischargeEnergy method
    
    public void setHealth(double energy) {
    	percent_health = energy;
    	if (percent_health < 0)   percent_health = 0;
    	if (percent_health > 100) percent_health = 100;
    } //setHealth method
    
    public double getHealth() {
    	return percent_health;
    } //getEnergy method
    
    public void depleteHealth(double damage) {
    	percent_health -= damage;
    	if (percent_health < 0) percent_health = 0;
    } //depleteHealth method
    
    public void setAngle(double theta) {
        angle = theta;
        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, player.getWidth()/2, player.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
    } //setAngle method
    
    public void rotateToFace(int X, int Y) {
    	if (X == x && Y == y) return;
        double theta = Math.atan((double)(Y - y)/(double)(X - x)) + (Math.PI/2);
        if (X < x) theta += Math.PI;
        setAngle(theta);
    } //rotateToFace method
    
    public Image getImage() {
        return op.filter(player, null);
    } //getImage method
    
} //Player class
