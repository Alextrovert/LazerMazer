/*
 * A little extra GUI program to design maps
 * It's not formally part of project right now
 * Not optimized for ease of usage
 * Quick and (very) ugly code to speed up mapmaking
 */

package LazerMazer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
 
import javax.swing.*;
 
import java.io.*;
import java.util.*;
 
@SuppressWarnings("serial")
public class MapMaker extends JFrame {
    static String fileName = "";
    static int X, Y, clickX, clickY;
    static Map m;
    static JFrame f;
    static boolean opened = false;
    static boolean save = false;
    static boolean pressed = false;
   
    static int lineMode = 0;
    static boolean clickOnce = false;
    static int trow = 0, tcol = 0;
   
    static Stack<Map> s = new Stack<Map>();
   
    JLabel label;
    JPanel showPanel, textFieldPanel;
    String sentence;
    JTextField textfield;
   
    public MapMaker() throws IOException {
 
        showPanel = new JPanel();
        textFieldPanel = new JPanel();
 
        label = new JLabel("Enter the file name in the maps folder (e.g. map1.txt)");
        label.setFont(new Font("Sans-Serif",Font.BOLD,25));
        showPanel.add(label);
        showPanel.setLocation(500,600);
        add(showPanel, BorderLayout.CENTER);
 
        textfield = new JTextField("", 50);
        textfield.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileName = textfield.getText();
                }
            });
 
        textFieldPanel.add(textfield);
        add(textFieldPanel, BorderLayout.SOUTH);    
       
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(Map.COLS * 8 + 6, Map.ROWS * 8 + 100);
        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("Map Maker");
        this.setLocationRelativeTo(null);
        this.setBackground(Color.white);
        this.createBufferStrategy(2);
    }
    
    public static int savestate;
    
    private static void drawBoard(int saveStatus, int lineModeStatus, int undoStatus) {
        BufferStrategy bf = f.getBufferStrategy();
        Graphics g = null;
        try {
            g = bf.getDrawGraphics();
            g.setColor(Color.yellow);
            g.fillRect(1,626,806,Map.COLS);
           
            g.setFont(new Font("Verdana", Font.BOLD, 40));
            g.setColor(Color.gray);
            g.drawString("SAVE DIS", 28, 693);
            g.drawString("UNDO", 318, 793);
            g.drawString("LINE MODE", 513, 693);
           
            if (lineModeStatus == 1) g.setColor(Color.blue);
            else g.setColor(Color.black);
            g.setFont(new Font("Verdana", Font.BOLD, 40));
            g.drawString("LINE MODE", 515, 795);
            g.setColor(Color.gray);
 
            g.setFont(new Font("Verdana", Font.BOLD, 40));
            if (undoStatus == 1) g.setColor(new Color(0, Map.COLS, 255));
            else if (undoStatus == 2) g.setColor(new Color(0, 0, 255));
            else g.setColor(Color.black);
            g.drawString("UNDO", 320, 795);
           
            g.setFont(new Font("Verdana", Font.BOLD, 40));
            if (saveStatus == 1) g.setColor(new Color(0, Map.COLS, 255));
            else if (saveStatus == 2) g.setColor(new Color(0, 0, 255));
            else if (saveStatus == 0) g.setColor(Color.black);
            g.drawString("SAVE DIS", 30, 795);
            
            
            g.setFont(new Font("Verdana", Font.BOLD, 20));
            if (saveStatus == 1) g.setColor(new Color(0, Map.COLS, 255));
            else if (saveStatus == 2) g.setColor(new Color(0, 0, 255));
            else g.setColor(Color.black);
            if (savestate > 0) {
            	g.clearRect(780, 600, 1000, 1000);
	            g.drawString("SAVING " + savestate + "%", 800, 795);
            } else {
            	g.clearRect(780, 600, 1000, 1000);
            	g.drawString("OK!", 800, 795);
            }
            for (int r = 0; r < Map.ROWS; r++)
                for (int c = 0; c < Map.COLS; c++)
                    g.drawImage(m.getImage(r, c), c*8 + 3, r*8 + 26, null);
           
            g.setFont(new Font("Arial", Font.BOLD, 10));
            g.setColor(Color.red);
            
            for (int c = 0; c < Map.COLS; c++)
                if (c % 5 == 0) g.drawString("" + c, c*8 + 5, 40);
            for (int r = 0; r < Map.ROWS; r++)
                if (r % 5 == 0) g.drawString("" + r, 5, r*8 + 33);
            
            if (clickOnce) g.drawRect(tcol*8+3, trow * 8 + 26, 8, 8);
           
        } finally { g.dispose(); }
        bf.show();
        Toolkit.getDefaultToolkit().sync();
    }
 
    public static void main(String[] args) throws IOException {
    	//m = new Map();
        f = new MapMaker();
       
        MouseListener mouseListen = new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                clickX = e.getX();
                clickY = e.getY();
                //System.out.printf("clicked %d %d\n", e.getX(), e.getY());
            }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) {    }
            public void mousePressed(MouseEvent e) { pressed = true; }
            public void mouseReleased(MouseEvent e) { pressed = false; }
        };
        f.addMouseListener(mouseListen);
 
        MouseMotionListener mouseMotionListen = new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                X = e.getX();
                Y = e.getY();
                //System.out.printf("%d %d\n", e.getX(), e.getY());
            }
 
            public void mouseDragged(MouseEvent e) { }
        };
        f.addMouseMotionListener(mouseMotionListen);
       
        while (fileName.equals("")) { }
       
        f.removeAll();
        //fileName = "map1.txt";
        
        m = new Map("maps\\" + fileName);
        f.setTitle("Editing " + fileName);
       
        savestate = 0;
        
        while (true) {
            if (clickX >= 30 && clickX <= 243 && clickY >= 764 && clickY <= 795) {
                saveMap();
                clickX = clickY = 0;
            } else if (clickX >= 453 && clickX <= 764 && clickY >= 763 && clickY <= 801){
                if (lineMode == 0) lineMode = 1;
                else {
                    lineMode = 0;
                    clickOnce = false;
                }
                clickX = clickY = 0;
            } else if (clickX >= 321 && clickX <= 451 && clickY >= 765 && clickY <= 794) {
                if (!s.empty()) {
                    m = s.pop();
                }
                clickX = clickY = 0;
            } else if (clickX != 0 && clickY != 0) {
                clickX -= 3; clickY -= 26;
                int row = clickY/8, col = clickX/8;
                if (lineMode == 0){  
                    if (row >= 0 && row < Map.ROWS && col >= 0 && col < Map.COLS) {
                        s.push(new Map(m));
                        if (m.m[row][col] == 'w') m.m[row][col] = '.';
                        else m.m[row][col] = 'w';
                    }
                } else {
                    if (!clickOnce && row >= 0 && row < Map.ROWS && col >= 0 && col < Map.COLS ){ // find first point
                        trow = row;
                        tcol = col;
                        clickOnce = true;
                    } else if (clickOnce && row >= 0 && row < Map.ROWS && col >= 0 && col < Map.COLS ){
                        clickOnce = false;
                        if (trow > row) {
                            row = (trow ^= (row ^= trow)) ^ row; // swap(row, trow)
                        }
                        if (tcol > col) {
                            col = (tcol ^= (col ^= tcol)) ^ col; // swap(col, tcol)
                        }
                        s.push(new Map(m));
                        if (trow == row){
                            for (int position = tcol; position <=col; position++){
                                if (m.m[row][position] == 'w') m.m[row][position] = '.';
                                else m.m[row][position] = 'w';
                            }
                       
                        } else if (tcol == col){
                            for (int position = trow; position <=row; position++){
                                if (m.m[position][col] == 'w') m.m[position][col] = '.';
                                else m.m[position][col] = 'w';
                            }
                        } else {
                            for (int position = tcol; position <=col; position++){
                                if (m.m[row][position] == 'w') m.m[row][position] = '.';
                                else m.m[row][position] = 'w';
                                if (m.m[trow][position] == 'w') m.m[trow][position] = '.';
                                else m.m[trow][position] = 'w';
                            }
                            for (int position = trow+1; position <row; position++){
                                if (m.m[position][col] == 'w') m.m[position][col] = '.';
                                else m.m[position][col] = 'w';
                                if (m.m[position][tcol] == 'w') m.m[position][tcol] = '.';
                                else m.m[position][tcol] = 'w';
 
                            }
                        }
                    }
                }
                clickX = clickY = 0;
            }
            if (pressed && X >= 30 && X <= 243 && Y >= 764 && Y <= 795) {
                drawBoard(2,lineMode,0);
            } else if (X >= 30 && X <= 243 && Y >= 764 && Y <= 795) {
                drawBoard(1,lineMode,0);
            } else {
                if (pressed && X >= 321 && X <= 451 && Y >= 765 && Y <= 794) {
                    drawBoard(0,lineMode,2);
                } else if (X >= 321 && X <= 451 && Y >= 765 && Y <= 794) {
                    drawBoard(0,lineMode,1);
                } else
                    drawBoard(0,lineMode,0);
            }
            
        }
    }
   
    public static void saveMap() {
    	PrintStream out;
    	
    	try {
    		out = new PrintStream(new FileOutputStream("maps\\"+fileName));
    	} catch (Exception e) { return; }
    	
        for (int r = 0; r < Map.ROWS; r++) {
        	
        	drawBoard((100 * (r+1) / Map.ROWS),lineMode,0);
        	savestate = (100 * (r+1) / Map.ROWS);
            for (int c = 0; c < Map.COLS; c++)
                out.print(m.m[r][c]);
            out.println();
            //out.flush();
        }
        savestate = 0;
        out.println("[Start Location]");
        out.println(1 + " " + 1);
        //out.println(m.startX + " " + m.startY);
        out.println("[End Location]");
        out.println(88 + " " + 118);
        //out.println(m.endX + " " + m.endY);
        System.out.println("DONE!");
    }
}

