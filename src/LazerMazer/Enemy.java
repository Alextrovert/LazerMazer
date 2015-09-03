/*
 * This is the Enemy class, it is very similar to the player class
 */
 
package LazerMazer;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import javax.imageio.ImageIO;

public class Enemy {
	//how close the enemy has to be to the player to chase it
	//the bigger the value, the more aggressive the enemy
	private static final int chaseDist = 100;
	
    //Variables to store the enemy properties
	public int x, y;
	private int dx, dy;
	private double angle;
	private BufferedImage enemy; //the enemy image
	private AffineTransformOp op;
	private double percent_energy; //% energy from 0..100
	private double percent_health; //% health from 0..100

	//random generator for changing directions
	public Random rand;

    public Enemy(int startX, int startY) {
    	rand = new Random(System.nanoTime());
    	//initialize health
    	percent_energy = 100;
    	percent_health = 100;
    	try {
    		enemy = (BufferedImage)ImageIO.read(new File("images\\enemy.png"));
	        if (x < 0 || x >= Map.COLS || y < 0 || y >= Map.COLS)
	        	throw new Exception();
	        //initialize start coordinates
	        x = startX;
	        y = startY;
    	} catch (Exception e) {
    		System.out.println("Error loading enemy at " + startX + ", " + startY);
    	}
    	setAngle(0);
    } //Enemy constructor
    
    public void move(int dx, int dy) {
    	try {
    		if (x + dx < 0 || x + dx >= Map.COLS*8 ||
    			y + dy < 0 || y + dy >= Map.ROWS*8)
    			throw new Exception();
    		x += dx;
    		y += dy;
    	} catch (Exception e) {
    		System.err.println("Error: enemy moved out of range.");
    		e.printStackTrace();
    	}
    } //move method
    
    public void updateLocation(Map m, Player p) {
    	if (System.nanoTime() % 20 == 0) { //change the direction of the enemy's path
    		//if enemy is chaseDist pixels from the player, move it in the player's direction
    		//there is no special path-finding algorithm implemented to chase the player
    		//therefore the enemy will bump into a wall if one exists in between.
    		if (Math.hypot(x - p.getX(), y - p.getY()) < chaseDist) {
    			dx = (p.getX() < x ? -1 : 1);
    			dy = (p.getY() < y ? -1 : 1);
    		} else { //otherwise, randomly change directions
    			dx = rand.nextInt(3) - 1;
        		dy = rand.nextInt(3) - 1;
    		}
    	}
    	
        //collision detection. Trust me, it works
        if (dx != 0 && dy != 0) { 
            dx *= 1; dy *= 1;
        } else {
            dx *= 1; dy *= 1;
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
    } //getEnergy percent
    
    public void setHealth(double energy) {
    	percent_health = energy;
    	if (percent_health < 0)   percent_health = 0;
    	if (percent_health > 100) percent_health = 100;
    } //setHealth method
    
    public double getHealth() {
    	return percent_health;
    } //getEnergy method
    
    public void depleteHP(double damage) {
    	percent_health -= damage;
    	if (percent_health < 0) percent_health = 0;
    } //depleteHP method
    
    public void setAngle(double theta) {
        angle = theta;
        AffineTransform transform = new AffineTransform();
        transform.rotate(angle, enemy.getWidth()/2, enemy.getHeight()/2);
        op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
    } //setAngle method
    
    public void rotateToFace(int X, int Y) {
    	if (X == x && Y == y) return;
        double theta = Math.atan((double)(Y - y)/(double)(X - x)) + (Math.PI/2);
        if (X < x) theta += Math.PI;
        setAngle(theta);
    } //rotateToFace method
    
    public Image getImage() {
        return op.filter(enemy, null);
    } //getImage method
    
    public boolean collided(double X, double Y) {
    	return (X-(x+12))*(X-(x+12)) + (Y-(y+12))*(Y-(y+12)) < 144;
    } //collided method
    
    public boolean collided(Enemy e) {
    	return collided(e.x+12, e.y+12);
    } //collided method
    
    public boolean collided(Player p) { //inputs are the top
    	return collided(p.getX()+12, p.getY()+12);
    } //collided Person method
} //Player class
