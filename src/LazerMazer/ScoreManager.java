package LazerMazer;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ScoreManager {
	
	public ArrayList<ScoreEntry> scores;
    private String score_file;
    private ObjectOutputStream outputStream = null;
    private ObjectInputStream inputStream = null;
    static final int maxEntriesDisplayed = 100;
    
    public ScoreManager(String file) {
        scores = new ArrayList<ScoreEntry>();
        score_file = file;
    }
	
	@SuppressWarnings("unchecked")
	public void loadScoreFile() {
        try {
            inputStream = new ObjectInputStream(new FileInputStream(score_file));
            scores = (ArrayList<ScoreEntry>) inputStream.readObject();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
	}
	
	public void updateScoreFile() {
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(score_file));
            outputStream.writeObject(scores);
        }catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
	}
	
	public class ScoreComparator implements Comparator<ScoreEntry> {
		public int compare(ScoreEntry score1, ScoreEntry score2) {
            double sc1 = score1.getScore();
            double sc2 = score2.getScore();
            if (sc1 < sc2) return -1;
            if (sc1 > sc2) return +1;
            return 0;
        }
	}
	
	public ArrayList<ScoreEntry> getScores() {
        loadScoreFile();
        Collections.sort(scores, new ScoreComparator());
        return scores;
    }
	
	public void addScore(String name, double score) {
        loadScoreFile();
        scores.add(new ScoreEntry(name, score));
        updateScoreFile();
	}
	
	public void clearScores() {
		loadScoreFile();
		scores.clear();
		updateScoreFile();
	}
	
	public String getFormattedString(String title) {
        String highscoreString = "";
        if (title != null) highscoreString += title;
        highscoreString += String.format("%-7s%-28s%-5s\n", "Rank", "Name", "Time");
        highscoreString += "-----------------------------------------\n";
        ArrayList<ScoreEntry> scores = getScores();
        int x = scores.size();
        if (x > maxEntriesDisplayed) x = maxEntriesDisplayed;
        for (int i = 0; i < x; i++) {
            highscoreString += String.format("%-7s%-28s%4.1f\n", i+1 + ".",
            								scores.get(i).getName().substring(0,
            								  Math.min(26, scores.get(i).getName().length())),
            								scores.get(i).getScore());
        }
        return highscoreString;
	}
}
