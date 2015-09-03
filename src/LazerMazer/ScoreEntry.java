package LazerMazer;

import java.io.Serializable;

public class ScoreEntry implements Serializable {
	
	private static final long serialVersionUID = 4286262237800995318L;
	private double score;
    private String name;
    
    public double getScore() { return score; }
    public String getName() { return name; }
    public void setScore(double s) { score = s; }
    public void setName(String n) { name = n; }
    
    public ScoreEntry(String name, double score) {
        this.score = score;
        this.name = name;
    } //ScoreEntry constructor
    
} //ScoreEntry class