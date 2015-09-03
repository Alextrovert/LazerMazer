/*
 * This is a custom class for text buttons; it doesn't listen for actions
 * It can draw the button and syncs the state of the button with mouse input
 */

package LazerMazer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;

public class Button {
	public static String defaultFontFileName = "misc\\MAGNETOB.TTF";
	
	private int x, y, width, height; //the info on the button's sensitive location
	
	//the following are public to make life easier, just be careful
	public int shadowOffsetX, shadowOffsetY; //custom shadow offsets
	public float shadowOffsetFontSize; //custom shadow font offset
	public String text; //text of the button
	public Font font; //font of the text
	public Color Shadow, Neutral, Hovered, Pressed; //states of the button
	public boolean drawShadow; //draw the shadow?
	
    //Enumeration of button states
	public enum ButtonState { neutral, hovered, pressed } ButtonState state;
	
	public Button(String text, String fontFile, float size, int x, int y, int width, int height) {
        //basic initialization of the button
		this.text = text;
		//set the font, default is Magneto Bold
        try {
			if (font == null)
				this.font = Font.createFont(Font.TRUETYPE_FONT, new File(defaultFontFileName));
			else
				this.font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
		} catch (Exception e) {
			System.err.println("Error loading font file" + font);
		}
        //set physical properties
		this.font = this.font.deriveFont(size);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		shadowOffsetX = shadowOffsetY = 0;
		shadowOffsetFontSize = 0.0f;
		
		//default colors
		Shadow =  new Color(0, 0, 255);
		Neutral = new Color(255, 0, 0);
		Hovered = new Color(255, 153, 153);
		Pressed = new Color(153, 0, 0);
		drawShadow = true;
	} //Button constructor
	
	public void draw(Graphics g, Color shadowCol, Color buttonCol, int x, int y) {
		//draw the shadow
		if (drawShadow) {
			g.setFont(font.deriveFont(font.getSize() + shadowOffsetFontSize));
			g.setColor(shadowCol);
			g.drawString(text, x + shadowOffsetX, y + shadowOffsetY);
		}
		
		//draw the text
		g.setFont(font.deriveFont(font.getSize() - 5.0f));
		g.setColor(buttonCol);
		g.drawString(text, x + 5, y);
	} //drawNeutral method
	
	public void draw(Graphics g) {
		if (state == ButtonState.neutral) {            //draw normal colored
			draw(g, Shadow, Neutral, x, y); 
		} else if (state == ButtonState.hovered) {     //draw with mouse hovered color
			draw(g, Shadow, Hovered, x, y);
		} else if (state == ButtonState.pressed) {     //draw with button pressed color
			draw(g, Shadow, Pressed, x, y);
		}
	} //draw method
	
	public void draw(Graphics g, int X, int Y) {
		if (state == ButtonState.neutral) {            //draw normal colored
			draw(g, Shadow, Neutral, X, Y); 
		} else if (state == ButtonState.hovered) {     //draw with mouse hovered color
			draw(g, Shadow, Hovered, X, Y);
		} else if (state == ButtonState.pressed) {     //draw with button pressed color
			draw(g, Shadow, Pressed, X, Y);
		}
	} //draw method
	
    //check if mouse coordinates are over the button
	public boolean inRange(int X, int Y) {
		return (X >= x && X <= x+width && Y >= y && Y <= y+height);
	} //inRange method
	
    //updated the button state with the mouse
	public void updateState(int X, int Y, boolean pressed) {
		if (!inRange(X, Y)) { //mouse not in range, return
			state = ButtonState.neutral;
			return;
		}
		if (!pressed) { //mouse hovered, not pressed, return
			state = ButtonState.hovered;
			return;
		}
		state = ButtonState.pressed;
	} //updateState method
	
} //Button class
