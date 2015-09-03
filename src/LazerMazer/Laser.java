/*
 * A class that stores an individual of lasers
 */

package LazerMazer;

import java.util.ArrayList;

public class Laser {
	
	private static final int MAXBOUNCES = 30;   //a cap on the max # of times the laser bounces
	private static final int MAXDISTANCE = 500; //a cap on the distance the laser bounces
	
	private Map m;              //Used to detect collisions with walls
	private ArrayList<Enemy> E; //Used to detect collisions with enemies
	private double damage = 3;  //percent damage to enemy HP per update
	
    //custom line segment class
	public class Segment {
		int x1, y1, x2, y2; //the start and end points (x1,y1) (x2,y2) for the segment
		
		Segment(int X1, int Y1, int X2, int Y2) {
			x1 = X1; y1 = Y1;
			x2 = X2; y2 = Y2;
		} //segment constructor
		
		Segment(Segment s) {
			x1 = s.x1; y1 = s.y1;
			x2 = s.x2; y2 = s.y2;
		} //segment copy constructor
		
        //compares two Segments
		public boolean equals(Segment s) {
			return x1 == s.x1 && y1 == s.y1 && x2 == s.x2 && y2 == s.y2;
		} //equals method
	} //Segment class

	private ArrayList<Segment> segments; //The laser is stored as line segments
	
	public Laser(Map map, ArrayList<Enemy> enemies) {
		this.m = map;
		segments = new ArrayList<Segment>();
		E = enemies;
	} //Laser constructor
	
	public Laser(Laser laser) {
		this.m = laser.m;
		segments = new ArrayList<Segment>();
		for (int i = 0; i < laser.segments.size(); i++)
			segments.add(new Segment(laser.segments.get(i)));
		this.E = laser.E;
	} //copy constructor
	
	//ArrayList managing methods
	public void clear() { segments.clear();	}
	public Segment get(int i) { return segments.get(i); }
	public int segments() { return segments.size(); }
	
	public void update(int playerX, int playerY, int mouseX, int mouseY) {
		
		if (playerX == mouseX && playerY == mouseY) return;
		
		clear();
		
		double startX = playerX, startY = playerY; //the starting point of the laser segment
		double X = startX, Y = startY; //the current point of the laser segment
		double dx = 0, dy = 0; //the change in X and change in Y
		double precision_factor = 0.03; //balance the flicker rate and collision detection precision
		
		//calculate the initial change in x and change in y
		if (mouseX > startX) { //bounce to the right
			dx = 1;
			dy = ((double)(mouseY - startY) / (double)(mouseX - startX));
		} else if (mouseX < startX) { //bounce to the left
			dx = -1;
			dy = -((double)(mouseY - startY) / (double)(mouseX - startX));
		} else if (mouseX == startX) { //vertical bounce
			dx = 0;
			dy = (mouseY > startY) ? 1 : -1;
		}
		
		//make smaller increments while collision checking
		dx *= precision_factor;
		dy *= precision_factor;
		
		boolean horizontal = true;
		double dist = 0;
		
		for (int bounces = 0; bounces < MAXBOUNCES; bounces++) {
			//enemy collision variables
			boolean enemyHit = false; //did the laser hit an enemy?
			int enemyIndex = 0; //which enemy did it hit?
			
			//project the laser by the slope for collision checking
			while (X >= 0 && X < Map.COLS*8 && Y >= 0 && Y < Map.ROWS*8 && !enemyHit) {
				//collision with wall, stop the projection of the laser
				if (m.at(X, Y) == 'w') break;
				//check enemy collisions
				for (int i = 0; i < E.size(); i++)
					if (E.get(i).collided(X, Y)) { //collision with enemy
						enemyHit = true;
						enemyIndex = i; //keep track of ArrayList index of the enemy that got hit
						break;
					}
                //increment the beam in its path
				X += dx;
				Y += dy;
			} //while
			X -= dx;
			Y -= dy;
			
            //check for invalid segments where start/end points are the same to prevent division by zeros
			if ((int)startX == (int)Math.round(X) && (int)startY == (int)Math.round(Y)) break;
			
			segments.add(new Segment((int)startX, (int)startY, (int)Math.round(X), (int)Math.round(Y)));
			
			if (enemyHit) {
				E.get(enemyIndex).depleteHP(damage); //deplete the enemy HP
				break; //we're done
			}
			
			double length = Math.hypot(X - startX, Y - startY); //calculate the length of the current laser segment
			
			if (length < 8) { //too small, reflection failed, reevaluate direction
				//erase the segment
				segments.remove(segments.size()-1);
				//reset the start coordinates of the new laser
				if (segments.size() == 0) return;
				startX = segments.get(segments.size()-1).x2;
				startY = segments.get(segments.size()-1).y2;
				horizontal ^= true; //change direction
				dy = -dy; //update the change in y
				dx = -dx; //update the change in x
				bounces--;
				continue;
			}
			
            //once the laser has bounced a certain distance, stop bouncing it
			dist += length;
			if (dist > MAXDISTANCE) break;
			
			//bounce the ball based on whether the wall is horizontal or vertical
			if (horizontal)
				dy = -dy; //update the change in y
			else
				dx = -dx; //update the change in x

			startX = X; //set the new starting X of the next bounce
			startY = Y; //set the new starting Y of the next bounce
		} //for
	} //update method
	
} //Laser class
