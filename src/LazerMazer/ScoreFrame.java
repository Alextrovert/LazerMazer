package LazerMazer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class ScoreFrame extends JFrame {
	private static final long serialVersionUID = -1812422580643309836L;
	
	public String name = "";	   
    private JLabel label;
    private JPanel showPanel;
    private JTextField namefield;
    
	public ScoreFrame(String scoreText) throws IOException {
    	
		showPanel = new JPanel();
		
        label = new JLabel("Enter your name:");
        label.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        
        showPanel.add(label);

        namefield = new JTextField("", 20);
        namefield.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	name = namefield.getText();
                }
            });

        showPanel.add(namefield);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setFont(new Font("Consolas", Font.PLAIN, 16));
        editorPane.setEditable(false);
        editorPane.setText(scoreText);

        //Put the editor pane in a scroll pane.
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setLocation(0, 200);
        editorScrollPane.setPreferredSize(new Dimension(400, 330));
        editorScrollPane.setMinimumSize(new Dimension(100, 100));
        
        showPanel.add(editorScrollPane);
        showPanel.getComponent(1).setBounds(300, 300, 20, 200);
        
        add(showPanel);
        
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(420, 400);
        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("High Scores");
        this.setLocation(3, 3);
        this.setBackground(Color.white);
        //this.createBufferStrategy(2);
    } //ScoreFrame constructor
	
	public boolean nameEntered() {
		return !name.equals("");
	} //nameEntered method
}