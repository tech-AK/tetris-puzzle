package tetris.puzzles.control;

import tetris.puzzles.datamodels.FallingVelocity;
import tetris.puzzles.datamodels.UserPreferences;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static tetris.puzzles.game.GameGrid.TIME_IN_MS_BETWEEN_NEW_TETROMINOES_RELEASED;
import static tetris.puzzles.game.GameGrid.TIME_IN_MS_BETWEEN_VELOCITY_INCREASE;

/**
 * This class creates the PuzzlesInstructions showing instructions for playing the game.
 */
public class PuzzlesInstructions extends JFrame {

    UserPreferences userPreferences;

    /**
     * Constructs a new JFrame for displaying the PuzzlesInstructions.
     * @param userPreferences The userPreferences currently used.
     */
    PuzzlesInstructions(UserPreferences userPreferences) {
        setSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Gaming Instructions / Spielanleitung für Puzzles");
        setLayout(new BorderLayout());

        this.userPreferences = userPreferences;

        setVisible(true); //first setting the new Frame visible as laying out the HTML text is quite slow.

        showInstructions();
    }

    /**
     * Creates a new view component holding the instructions that are added to the frame.
     */
    private void showInstructions() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(getHTMLText());
        editorPane.setCaretPosition(0); //cause the scroll to start at the top
        editorPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane);

        JButton closeButton = new JButton("Alles klar !");
        closeButton.setBackground(new Color(134, 254, 134)); //mint green
        this.add(closeButton, BorderLayout.PAGE_END);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }


    /**
     * Returns the variable string for the instructions in HTML format.
     * @return Variable string for the instructions in HTML format.
     */
    private String getHTMLText() {
        StringBuilder string = new StringBuilder();
        string.append("<HTML>");
        string.append("<center>");
        string.append("<h1><u>Gaming Instructions (only in German) / Spielregeln für Puzzle</u></h1>");
        string.append("<h3><i>Basierend auf den aktuellen Einstellungen</i></h3>");
        string.append("<p><span style=\"font-size:14px;\"><b><u>Ziel des Spiels:</b></u></span><br>");
        string.append("Ziel des Spiel ist es, die im Spielfeld sichtbaren <b>Umrisse durch genau zwei passende Steine aufzufüllen</b> und währenddessen zu <b>verhindern, dass die herabfallenden Steine</b> mit ihrer Kante das <b>untere Spielfeld berühren</b>.</p>");
        string.append("<p><span style=\"font-size:14px;\"><b><u>Spielablauf:</b></u></span><br>");
        string.append("<b>Jede " + (TIME_IN_MS_BETWEEN_NEW_TETROMINOES_RELEASED / 1000) + " Sekunden</b> " + getPlurarOrSingularStringErscheinen(userPreferences.getNumberOfNewAppearingStones()) + " genau <b>" + userPreferences.getNumberOfNewAppearingStones() + " " + getPlurarOrSingularStringSteine(userPreferences.getNumberOfNewAppearingStones()) + "</b>.");
        string.append(" Diese Steine dürfen nicht die untere Kante des Spielfelds berühren, da dann das Spiel verloren ist \uD83D\uDC80. Die Steine können hierbei in die <b> unteren " + userPreferences.getNumberOfShapes() + " Umrisse eingeführt werden</b> oder <b>auf den " + userPreferences.getNumberOfParkingSpots() + " Parkplätzen zwischengeparkt werden.</b></p>");
        string.append("<p><span style=\"font-size:14px;\"><b><u>Einschränkungen:</b></u></span>");
        string.append("<p>Ein Stein kann nur dann auf einem <b>Parkplatz zwischengeparkt</b> werden, <b>wenn der Parkplatz frei ist</b>, das heißt, noch kein anderer Stein den Platz einnimmt. Ein eingeparkter Stein kann nicht mehr zurück in das Gitter verschoben werden, sondern nur noch auf andere freie Parkplätze oder in passende Umrisse. <br>");
        string.append("Ebenso kann ein Stein <b>nur dann in einen Umriss platziert</b> werden, wenn der <b>Stein bereits richtig gedreht und gespiegelt ist</b>, andernfalls lässt sich der Stein nicht einfügen. In Umrisse eingefügte Steine können nicht mehr gedreht, gespeigelt oder entfernt werden. Sobald zwei passende Steine in einen Umriss eingefügt werden, verschwinden beide Steine, der Umriss wird wieder benutzbar und Du erhälst Punkte \uD83C\uDF89 \uD83C\uDF89 \uD83C\uDF89 !<br>");
        string.append("Wird eine <b>ungültige Aktion</b> durchgeführt (z. B. ein Parkplatz wird angewählt, der schon besetzt ist), erscheint im rechten Balken ein <b>Warnzeichen </b> ⚠️.</p> <br>");
        string.append("<p><span style=\"font-size:14px;\"><b><u>Interaktionsmöglichkeiten:</b></u></span><br></center>");
        string.append("<ul>");
        string.append("<li><b><u>Auswählen eines Steins:</u></b> Steine im Gitter oder auf den Parkplätzen können mit der Maustaste ausgewählt werden, und durch erneuten Klick auf denselben Stein oder einen anderen Stein wieder abgewählt werden. Ausgewählte Steine bekommen eine gelbe Farbe.</li><br>");
        string.append("<li><b><u>Verschieben eines ausgewählten Steins:</u></b> Ein ausgewählter Stein kann mit den Pfeiltasten oder den Tasten <b><i><span style=\"color: #3366ff;\">W</b></i></span>, <b><i><span style=\"color: #3366ff;\">A</b></i></span>, <b><i><span style=\"color: #3366ff;\">S</b></i></span>, <b><i><span style=\"color: #3366ff;\">D</b></i></span> entsprechend nach oben, links, unten, rechts bewegt werden. Nur Steine in Parkplätzen können nach oben bewegt werden.</li><br>");
        string.append("<li><b><u>Spiegeln eines ausgewählten Steins:</u></b> Ein ausgewählter Stein kann mit <span style=\"color: #3366ff;\"><b><i>Q</b></i></span> horizontal und mit <span style=\"color: #3366ff;\"><b><i>E</b></i></span> vertikal gespiegelt werden.</li><br>");
        string.append("<li><b><u>Drehen eines ausgewählten Steins:</u></b> Ein ausgewählter Stein kann mit <span style=\"color: #3366ff;\"><b><i>C</b></i></span> im Uhrzeigersinn und mit <span style=\"color: #3366ff;\"><b><i>Y</b></i></span> gegen den Uhrzeigersinn gedreht werden.</li><br>");
        string.append("<li><b><u>Einfügen eines ausgewählten Steins in einen Umriss:</u></b> Drücke solange die Taste <span style=\"color: #3366ff;\"><b><i>V</b></i></span> bis das Feld der Umrisse ausgewählt ist und nutze die Pfeiltasten oder wähle die Nummer des Umrisses, um den entsprechenden Umriss auszuwählen. Durch Drücken von <span style=\"color: #3366ff;\"><b><i>RETURN</b></i></span> wird der Stein abgelegt. </li><br>");
        string.append("<li><b><u>Einfügen eines ausgewählten Steins in einen Parkplatz:</u></b> Drücke solange die Taste <span style=\"color: #3366ff;\"><b><i>V</b></i></span> bis das Feld der Parkplätze ausgewählt ist und nutze die Pfeiltasten oder wähle die Nummer des Parkplatzes, um den entsprechenden Parkplatz auszuwählen. Durch Drücken von <span style=\"color: #3366ff;\"><b><i>RETURN</b></i></span> wird der Stein abgelegt. </li><br>");
        string.append("</ul>");
        string.append("<center>");
        string.append("<p><span style=\"font-size:14px;\"><b><u>Game-Modi:</b></u></span><br> ");
        string.append("Das Spiel stellt des Weiteren <b>verscheidene Game-Modi bereit, die in den Einstellungen eingestellt werden können</b>. <b>Aktuell</b> verhält sich das Spiel wie im Folgenden beschrieben:");
        string.append("</center><ul>");
        string.append("<li><b><u>Spielgeschwindigkeit:</u></b> Steine fallen zurzeit mit einer <b> " + getGermanTranslationForVelocity(userPreferences.getVelocity()) + "</b> Geschwindigkeit. Bei dieser Einstellung benötigt ein Stein, der gerade am oberen Rand erschienen ist, " +
                "<b>in etwa " + getTimeToCrash(userPreferences.getVelocity()) + " Sekunden, bis dieser am unteren Gitterrand ankommt</b> und das Spiel somit vorbei ist. " +
                "Die Steine fallen hierbei <b>pixelunabhängig</b> immer mit der gleichen Geschwindigkeit: Wenn Du also denkst, dass du das Fenster vergrößern kann, damit Du mehr Zeit und Fläche hast, bis die Steine den unteren Rand berühren, " +
                "dann lass Dir gesagt sein, dass dies nichts bringt, da dann die Steine mit einem größeren Fenster auch schneller die Strecke zurücklegen :) </li><br>");
        string.append("<li><b><u>Steigerung der Spielgeschwindigkeit:</u></b> " + getVelocityIncreaseDescription(userPreferences.isVelocityIncreasing()) + "</li><br>");
        string.append("<li><b><u>Größe der Steine:</u></b> Die Steine, die während des Spiels erscheinen haben immer eine <b>Größe von " + userPreferences.getNumberOfKachelnInStone() + " Kacheln. </li><br>");
        string.append("<li><b><u>Anzahl der Parkplätze:</u></b> Die Anzahl der Parkplätze wird von der Größe der Steine beeinflusst. Bei den aktuellen Einstellungen gibt es <b> " + userPreferences.getNumberOfParkingSpots() + " Parkplätze. </li><br>");
        string.append("<li><b><u>Anzahl der Umrisse:</u></b> In dem Spielfeld sind aktuell <b> " + userPreferences.getNumberOfShapes() + " Umrisse verfügbar. </li><br>");
        string.append("<li><b><u>Anzahl der Farben:</u></b> In diesem Spiel kann selbst die Anzahl der Farben variiert werden! Zurzeit werden die Steine in <b> " + userPreferences.getAmountOfColors() + " verschiedenen Farben</b> zufällig eingefärbt. </li><br>");
        string.append("</ul>");
        string.append("<center><h2>Viel Spaß beim Spielen!</h2></center>");

        string.append("</HTML>");

        return string.toString();
    }

    /**
     * Method that is used to get the plural or singular form depending on the count used.
     * @param count The count of tetrominoes
     * @return A string with the right form: "neue Steine" or "neuer Stein" depending on the amount.
     */
    private String getPlurarOrSingularStringSteine(int count) {
        return (count > 1) ? "neue Steine" : "neuer Stein";
    }

    /**
     * Method that is used to get the plural or singular form depending on the count used.
     * @param count The count of tetrominoes
     * @return A string with the right form: "erscheinen" or "erscheint" depending on the amount.
     */
    private String getPlurarOrSingularStringErscheinen(int count) {
        return (count > 1) ? "erscheinen" : "erscheint";
    }

    /**
     * Returns the description of the velocity increase depending on if it is increasing or not.
     * @param increasing If true, a string is returned that describes that the velocity is increasing.
     * @return Description of velocity increase
     */
    private String getVelocityIncreaseDescription(boolean increasing) {
        if (increasing) {
            return "Die <b>anfängliche " + getGermanTranslationForVelocity(userPreferences.getVelocity()) + " Geschwindigkeit</b> wird im Laufe der Zeit <b>immer weiter gesteigert</b>, um die Spannung hoch zu halten. Die Geschwindigkeit steigt jede " + (TIME_IN_MS_BETWEEN_VELOCITY_INCREASE / 1000) + " Sekunden.";
        } else {
            return "Zurzeit <b>verändert sich die Spielgeschwindigkeit</b> im Verlaufe des Spiels <b>nicht</b>. Du kannst dies in den Einstellungen aber noch ändern, um mehr Spielspaß zu haben!";
        }
    }

    /**
     * Returns the time one tetromino needs in the current settings to crash at the bottom.
     * @param velocity The {@link FallingVelocity} that is used in the game.
     * @return Time one tetromino needs in the current settings to crash at the bottom.
     */
    private int getTimeToCrash(FallingVelocity velocity) {
        switch (velocity) {
            case SLOW:
                return 30;

            case MEDIUM:
                return 15;

            case FAST:
                return 8;
        }
        return -1;
    }

    /**
     * Returns a german translation for the used {@link FallingVelocity}.
     * @param velocity The velocity of the game as {@link FallingVelocity} object
     * @return A german translation for the used {@link FallingVelocity}.
     */
    private String getGermanTranslationForVelocity(FallingVelocity velocity) {
        switch (velocity) {
            case SLOW:
                return "langsamen";

            case MEDIUM:
                return "mittleren";

            case FAST:
                return "schnellen";
        }
        return "NOT SET";
    }
}
