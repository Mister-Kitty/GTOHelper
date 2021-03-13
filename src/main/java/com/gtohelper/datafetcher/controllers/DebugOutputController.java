package com.gtohelper.datafetcher.controllers;

import com.gtohelper.utility.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class DebugOutputController {

    @FXML
    TextArea PioLogText;
    StringBuilder pioTextBuilder = new StringBuilder();

    @FXML
    TextArea HelperLogText;
    StringBuilder helperTextBuilder = new StringBuilder();

    @FXML
    TextArea HudLogText;
    StringBuilder hudTextBuilder = new StringBuilder();

    public DebugOutputController() {
        Logger.addChannelListener(Logger.Channel.SOLVER, this::logPioMessage);
        Logger.addChannelListener(Logger.Channel.HELPER, this::logHelperMessage);
        Logger.addChannelListener(Logger.Channel.HUD, this::logHudMessage);
    }

    public void logPioMessage(String message) {
        try {
            pioTextBuilder.append(message);
            pioTextBuilder.append("\n");
            PioLogText.setText(pioTextBuilder.toString());
        } catch (Exception e) {
            // Until I implement a circle buffer, catch and ignore any possible
        }
    }

    public void logHelperMessage(String message) {
        try {
        helperTextBuilder.append(message);
        helperTextBuilder.append("\n");
        HelperLogText.setText(helperTextBuilder.toString());
        } catch (Exception e) {
            // Until I implement a circle buffer, catch and ignore any possible
        }
    }

    public void logHudMessage(String message) {
            try {
        hudTextBuilder.append(message);
        hudTextBuilder.append("\n");
        HudLogText.setText(hudTextBuilder.toString());
            } catch (Exception e) {
                // Until I implement a circle buffer, catch and ignore any possible
            }
    }
}
