package LazerMazer;

import java.util.Scanner;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.FileInputStream;
import javax.swing.ImageIcon;

/*
 * The Map class. It takes care of map input and sprites.
 * 
 * map files in the map folder must be of the format:
 * 
 * - A ROWS by COLS grid (a.k.a 120 rows and 90 columns)
 * -----> 'w' in the grid means a wall
 * -----> '.' in the grid means a space
 * -----> 'e' in the grid means an end location
 * 
 * - A line containing "[Start Location]" without quotes
 * - two integers, the row and column of the starting location
 * 
 * - A line containing "[Enemy Locations]" without quotes
 * - A number indicating the number of enemies in the map
 * - For every line after this: the row and column of that enemy would spawn
 * 
 * The sprites called "space.png" and "wall.png" must be:
 * 8x8 in resolution and placed in the images folder
 */

public class Map {

	public static final int ROWS = Main.HEIGHT / 8;
	public static final int COLS = Main.WIDTH / 8;
	
	private static final int SHADOW_LEVELS = 20;
	private static final Image space = (new ImageIcon("images\\space.png")).getImage();
	private static final Image wall = (new ImageIcon("images\\wall.png")).getImage();
	private static final Image darkness = (new ImageIcon("images\\darkness.png")).getImage();
	private static final Image end = (new ImageIcon("images\\end.png")).getImage();
	private BufferedImage[] shadows = new BufferedImage[SHADOW_LEVELS + 1]; 
	
	public char[][] m; //stores the map
	public int startX, startY, endX, endY; //start, end locations
	
	public Map(String fileName) {
		m = new char[ROWS][COLS];
		
		//prerender the shading masks with alpha channels
		for (int i = 0; i <= SHADOW_LEVELS; i++) {
			shadows[i] = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    		BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
    		bi.getGraphics().drawImage(this.getBlackImage(), 0, 0, null);

    		float[] scales = { 1f, 1f, 1f, 1f * i / SHADOW_LEVELS };
    		float[] offsets = new float[4];
    		RescaleOp rop = new RescaleOp(scales, offsets, null);

    		// Draw the image, applying the filter
    		Graphics2D g2d = (Graphics2D)shadows[i].getGraphics();
    		g2d.drawImage(bi, rop, 0, 0);
		}
		
		try {
			Scanner in = new Scanner(new FileInputStream(fileName));

			//input the grid line by line
            for (int r = 0; r < ROWS; r++) {
                m[r] = in.nextLine().trim().toCharArray();
                if (m[r].length != COLS)
                	throw new Exception();
            }
            
            //input the start location
			while (in.nextLine().indexOf("[Start Location]") == -1) { }
			startX = in.nextInt() * 8;
			startY = in.nextInt() * 8;
		} catch (Exception e) {
			System.err.println("Error loading map file: " + fileName);
			e.printStackTrace();
		}		
	} //map method
	
	public Map(Map M) {
        this.m = new char[ROWS][];
        for (int i = 0; i < ROWS; i++)
            this.m[i] = M.m[i].clone();
        this.startX = M.startX;
        this.startY = M.startY;
        this.endX = M.endX;
        this.endY = M.endY;
	} //deep Copy constructor
	
    public char tileAt(int R, int C) {
    	if (R < 0 || R >= ROWS || C < 0 || C >= COLS) return 'w';
    	return m[R][C];
    } //tileAt method

    public char at(int x, int y) {
        return tileAt(Math.round(y / 8), Math.round(x / 8));
    } //at method
    
    public char at(double x, double y) {
    	return at((int)x, (int)y);
    } //at method overload for doubles
   
    public Image getImage(int r, int c) {
    	switch (m[r][c]) {
    	case '.': return space;
    	case 'w': return wall;
    	case 'e': return end;
    	default : return space;
    	}
    } //getImage method
    
    public Image getBlackImage() {
    	return darkness;
    } //getBlackImage method
    
    public BufferedImage getShadow(double a) {
    	return shadows[(int)Math.floor(a * SHADOW_LEVELS)];
    } //getShadow method
} //Map class
