package LazerMazer;

import java.awt.Image;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.ImageIcon;

/*
 * MainMenu class
 * This class is a JPanel that paints the welcome menu
 */

@SuppressWarnings("serial")
public class MainMenu extends JPanel {
	
    //The pictures to import
    private static final Image menulogo = (new ImageIcon("images\\menulogo.png")).getImage();
    private static final Image background = (new ImageIcon("images\\background.png")).getImage();
 
	public void paint(Graphics g) {
		
		//draw the background
		g.drawImage(background, 0, 0, null);
		//draw the title
		g.drawImage(menulogo, 250, 100, null);
		
        //draw the buttons
		Main.play.draw(g);
		Main.instructions.draw(g);
		Main.credits.draw(g);
		Main.quit.draw(g);
		
	} //paint method
	
} //MainMenu class
