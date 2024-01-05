package com.gtohelper.datafetcher.controllers;

import javafx.event.ActionEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AnalyzeResultsController {

    public void onHyperlinkClick(ActionEvent mouseEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("www.patreon.com/gtohelper"));
    }
}
