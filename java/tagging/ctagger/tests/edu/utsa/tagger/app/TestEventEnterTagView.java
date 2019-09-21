package edu.utsa.tagger.app;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TestSetup;
import edu.utsa.tagger.gui.AddValueView;
import edu.utsa.tagger.gui.EventEnterTagView;
import edu.utsa.tagger.gui.GuiTagModel;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TestEventEnterTagView {
    private TestSetup setup;

    @Before
    public void setUp() {
        setup = new TestSetup();
    }

    @Test
    public void testEventEnterTagView() {
        /**
         * When key down pressed, move to the next search result. If reached end of search result, stop at the last search. If there's no search result, should not do anything
         * When key up pressed, move to the previous search result. If reached beginning of search result, stop at the first search. If first pressed after search results are displayed, goes to first search result. If there's no search result, should not do anything
         * When key enter pressed, if a search result is being highlighted, act like a button press on a tag
         *      - When there's no event or group selected yet, a warning dialog appears
         *      - When there's selected event or group
         *          + If the tag is takeValue tag, show dialog for user to input value for the tag
         *          + Associate the final tag with the event or group
         *      If no search result is being highlighted, do nothing
         */
        JFrame frame = new JFrame("Test EventEnterTagView");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        EventEnterTagView evtEnterTagView = new EventEnterTagView(setup.getTestTagger(), setup.getTestTaggerView());
        panel.add(evtEnterTagView.getjTextAreaPanel());
        panel.add(evtEnterTagView.getSearchResultsScrollPane());
        JPanel searchResults = (JPanel)evtEnterTagView.getSearchResultsScrollPane().getComponent(0);
        JLabel label = new JLabel("Test");
        label.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(Color.gray);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(new Color(0,0,0));
            }
        });
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        try {
            wait(1000);
        }
        catch(Exception ex) {

        }

        // When there's no item in panel
    }
}
