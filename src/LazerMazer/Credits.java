package LazerMazer;

import java.awt.Image;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*
 * Welcome class
 * This class is a JPanel that paints the credits screen
 */

@SuppressWarnings("serial")
public class Credits extends JPanel {
	
	private static final Image credits = 
			(new ImageIcon("images\\credits.png")).getImage();

	public void paint(Graphics g) {
		//draw the credits
		g.drawImage(credits, 0, 0, null);
		Main.back.draw(g); //draw the back button
	} //paint method
	
} //Credits class
