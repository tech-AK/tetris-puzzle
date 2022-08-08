package tetris.tools;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * This class is responsible for storing the data for the highscore in a separated database.
 * The database is realized by using a comma-separated-value (csv) file which is stored in the resources folder of this project.
 */
public class DatabaseSaver {

    /*
    Internal implementation note:
    With regard to a three-layer architecture (UI - Business logic - Database separation),
    it would be better to separate the internal logic like "if newPoints > previousPoints, write a new entry" into another class
    so that this class is only responsible for writing and reading data, without implementing additional business logic.
    However, as this project is likely to not be continued and scalability and maintainability is not in the focus,
    we just did not separate it.
     */

    String highscorePath;

    /**
     * Initialises a new DatabaseSaver which can be used to save and receive entries in database.
     */
    public DatabaseSaver() {
        //Previously we used the project directory to store the highscore.csv file, but this causes problems with JAR files
        //Thus, use the temp directory now (file should survive long enough, although it is a temp directory)
        //In production-ready environment we would use a server and backend anyway.

        //String resourcePath = "/resources";

        String resourcePath = System.getProperty("java.io.tmpdir");

        String highscoreFileName = "highscores.csv";
        highscorePath = resourcePath + highscoreFileName;

        //highscorePath = resourceURL.toURI().getPath() + highscoreFileName;
        //highscorePath = resourceURL.toURI().getPath() + highscoreFileName;
        /*URL resourceURL = this.getClass().getResource(resourcePath);
        try {
            highscorePath = resourceURL.toURI().getPath() + highscoreFileName;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * Use this constructor to quickly update an entry in the highscore.
     * <br>This constructor can be used as fire-and-forget: <ul>
     * <li>If the given points are higher than the current entry in the highscore, the points will be saved.</li>
     * <li>If the given points are lower than the current entry in the highscore, the points will be discarded.</li>
     * </ul>
     * In both cases, no more interaction is needed.
     *
     * @param id     The ID of the game
     * @param name   The name of the player to be saved
     * @param points The points of the player to be saved
     */
    public DatabaseSaver(int id, String name, int points) {
        this();
        saveToHighscore(id, name, points);
    }

    /**
     * Use this constructor to quickly update an entry in the highscore.
     * <br>This constructor can be used as fire-and-forget: <ul>
     * <li>If the given points are higher than the current entry in the highscore, the user will prompted to enter his name and the points will be saved.</li>
     * <li>If the given points are lower than the current entry in the highscore, the points will be discarded and user will not be prompted.</li>
     * </ul>
     * In both cases, no more interaction is needed.
     * <br>NOTE: This constructor will block the thread until the user enters his name or cancels the dialog.
     *
     * @param id     The ID of the game
     * @param points The points of the player to be saved
     */
    public DatabaseSaver(int id, int points) {
        this();

        if (isNewHighscoreAchieved(id, points)) {
            //only display dialog if new highscore achieved
            String name = JOptionPane.showInputDialog(null, "You achieved a new highscore, please enter your name: ", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
            if (name == null) {
                name = "N/A";
            }

            saveToHighscore(id, name, points);
        }
    }

    /**
     * Saves a new entry to the highscore.
     * <br>This method can be used as fire-and-forget: <ul>
     * <li>If the given points are higher than the current entry in the highscore, the points will be saved.</li>
     * <li>If the given points are lower than the current entry in the highscore, the points will be discarded.</li>
     * </ul>
     *
     * @param gameId    The ID of the corresponding game
     * @param name      The name of the player to be saved
     * @param newPoints The points of the player to be saved
     * @return If the given points of the player are higher than the points saved and thus, the highscore entry is updated, this method returns {@code true}.
     * Otherwise it discards the points and returns {@code false}.
     */
    public boolean saveToHighscore(int gameId, String name, int newPoints) {
        if (name.contains(";")) {
            //name not allowed to contain ";" as it is used as separator. Just replace with ","
            name = name.replace(";", ",");
        }

        if (isNewHighscoreAchieved(gameId, newPoints)) {
            try {
                updateHighscoreEntry(gameId, name, newPoints);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Returns whether the user has achieved a new score that is bigger than the current highscore.
     *
     * @param gameId    The ID of the corresponding game
     * @param newPoints The points of the player to be saved
     * @return If the given points of the player are higher than the points saved this method returns {@code true}.
     * Otherwise it returns {@code false}.
     */
    public boolean isNewHighscoreAchieved(int gameId, int newPoints) {
        int previousPoints = getPoints(gameId);
        return previousPoints < newPoints && newPoints > 0;
    }

    /**
     * Updates the corresponding entry in the database.
     *
     * @param gameId    The ID of the corresponding game
     * @param name      The name of the player to be saved
     * @param newPoints The points of the player to be saved
     * @throws IOException Thrown if some problems occur while accessing the database
     */
    private void updateHighscoreEntry(int gameId, String name, int newPoints) throws IOException {
        String[] lines = readFile(highscorePath);
        resetDatabase();

        StringBuilder stringBuilder = new StringBuilder();

        if (lines != null) {
            for (String line : lines) {
                String[] splitData = splitLineInToData(line);
                if (!splitData[0].equals(gameId + "")) {
                    //skip this entry as we write the entry for the updated gameID below
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
            }
        }

        //write updated entry
        stringBuilder.append(gameId);
        stringBuilder.append(";");
        stringBuilder.append(name);
        stringBuilder.append(";");
        stringBuilder.append(newPoints);

        writeFile(highscorePath, stringBuilder.toString());
    }

    /**
     * Completely resets the database. Thus, every game will have no highscore anymore.
     * <br><b>NOTE:</b> This cannot be undone.
     *
     * @throws IOException Thrown if some problems occur while accessing the database
     */
    public void resetDatabase() throws IOException {
        File file = new File(highscorePath);
        file.delete();
        createNewFile(highscorePath);
    }

    /**
     * Gets the points for one specific game ID.
     *
     * @param gameId The ID of the corresponding game
     * @return Returns the current points as saved in the highscore.
     * <br><b>NOTE:</b>If no entry for the game was set yet, this method returns 0.
     */
    public int getPoints(int gameId) {
        String[] lines = readFile(highscorePath);
        if (lines != null) {
            for (String line : lines) {
                String[] splitData = splitLineInToData(line);
                if (splitData[0].equals(gameId + "")) {
                    return Integer.parseInt(splitData[2]);
                }
            }
        }

        return 0;
    }

    /**
     * Gets player's name for one specific game ID.
     *
     * @param gameId The ID of the corresponding game
     * @return Returns the current player's name as saved in the highscore.
     * <br><b>NOTE:</b>If no entry for the game was set yet, this method returns "keine Angabe".
     */
    public String getName(int gameId) {
        String[] lines = readFile(highscorePath);
        if (lines != null) {
            for (String line : lines) {
                String[] splitData = splitLineInToData(line);
                if (splitData[0].equals(gameId + "")) {
                    return splitData[1];
                }
            }
        }
        return "N/A";
    }

    /**
     * Returns a String array which contains every line in the database file. If database file is not found, a new file is created and null is returned.
     *
     * @param highscorePath The path to database file
     * @return An array of strings that start in a new line or null if no database file does not exist.
     */
    private String[] readFile(String highscorePath) {
        try {
            if (checkFileExists(highscorePath)) {
                return readLinesInFile(highscorePath);

            } else {
                createNewFile(highscorePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Works just like {@link #readLinesInFile(File)} except that a string is taken as argument.
     *
     * @param path The path to database file that should be read.
     * @see #readLinesInFile(File)
     */
    private String[] readLinesInFile(String path) throws IOException {
        return readLinesInFile(new File(path));
    }

    /**
     * Returns a String array which contains every line in the database file.
     * <br>As the file is quite small, it is faster to do only one reading operation on the drive and then process the stored data in RAM,
     * instead of do multiple reading operations until the right line was found.
     *
     * @param highscoreFile The database file that should be read.
     * @return An array of strings that start in a new line
     */
    private String[] readLinesInFile(File highscoreFile) throws IOException {
        /*Scanner sc = new Scanner(highscoreFile, StandardCharsets.UTF_8);
        sc.useDelimiter("\\Z");
        String input = sc.next();
        sc.close();*/

        String input = Files.readString(highscoreFile.toPath(), StandardCharsets.UTF_8);
        return splitInputByNewLine(input);
    }

    /**
     * Returns a string array that is split by line break.
     *
     * @param stringToSplit The string that should be split.
     * @return String array that is split by line break.
     */
    private String[] splitInputByNewLine(String stringToSplit) {
        return stringToSplit.split("\\r?\\n"); //need to use this regex in order to work on Windows and Unix.
    }

    /**
     * Returns a string array that contains the string split by the {@code ;} separator.
     *
     * @param string The string that should be split (e. g. a line in the csv-file).
     * @return String array that contains the string split by the {@code ;} separator.
     */
    private String[] splitLineInToData(String string) {
        return string.split(";");
    }

    /**
     * Initialises the database by creating a new file with the corresponding database scheme.
     *
     * @param path The path to database file
     * @throws IOException Thrown if some problems occur while accessing the database
     */
    private void createNewFile(String path) throws IOException {
        String header = "game_ID;name;points";
        writeFile(path, header);
    }

    /**
     * Checks if the file exists.
     *
     * @param path The path to file that should be checked.
     * @return True, if the file exists and false otherwise.
     */
    private boolean checkFileExists(String path) {
        return new File(path).exists();
    }

    /**
     * Works just like {@link #writeFile(File, String)} except that a string is taken as argument.
     *
     * @param path          The path to database file that should be read.
     * @param stringToWrite The string that should be appended in a new line
     * @see #writeFile(File, String)
     */
    private void writeFile(String path, String stringToWrite) throws IOException {
        writeFile(new File(path), stringToWrite);
    }

    /**
     * Writes the given string into the given file.
     *
     * @param file          The path to file that should be written to
     * @param stringToWrite The string that should be appended in a new line
     * @throws FileNotFoundException Thrown if some problems occur while accessing the file
     */
    private void writeFile(File file, String stringToWrite) throws IOException {
        file.createNewFile();
        PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
        writer.println(stringToWrite);
        writer.flush();
        writer.close();
    }

}
