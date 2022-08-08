package tetris.main;

import tetris.puzzles.MainPuzzlesPanel;

import javax.swing.*;
import java.awt.*;

public class StartFrame extends JFrame {

    public StartFrame() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Puzzles - Tetris Game");
        setMinimumSize(new Dimension(900, 700));


        add(new MainPuzzlesPanel());
        setVisible(true);


    }

    @Override
    public Insets getInsets() {
        return new Insets(5, 5, 5, 5);
    }

}
