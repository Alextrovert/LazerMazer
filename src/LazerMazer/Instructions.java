package LazerMazer;

import java.awt.Image;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/*
 * Welcome class
 * This class is a JPanel that paints the instructions screen
 */

@SuppressWarnings("serial")
public class Instructions extends JPanel {
	
	private static final Image instructions = 
			(new ImageIcon("images\\instructions.png")).getImage();

	public void paint(Graphics g) {
		//draw the instructions
		g.drawImage(instructions, 0, 0, null);
		Main.back.draw(g); //draw the back button
	} //paint method
	
} //Instructions class
