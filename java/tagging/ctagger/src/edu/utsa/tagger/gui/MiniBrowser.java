package edu.utsa.tagger.gui;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;


public class MiniBrowser extends JFrame {
    private final JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;

    private String url;

    public MiniBrowser(String title, String url) {
        super(title);
        this.url = url;
        init();
    }

    private void init() {
        createScene();
        JPanel panel = new JPanel();
        panel.add(jfxPanel);
        setPreferredSize(new Dimension(900, 810));
        getContentPane().add(panel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    private void createScene() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView view = new WebView();
                engine = view.getEngine();
                engine.load(url);
                jfxPanel.setScene(new Scene(view));
                jfxPanel.setPreferredSize(new Dimension(900, 780));
            }
        });
    }
}
