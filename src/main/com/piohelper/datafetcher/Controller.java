package com.piohelper.datafetcher;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller {

    @FXML
    private TextArea inputText;

    @FXML
    private TextField DBName;

    @FXML
    private TextField DBUser;

    @FXML
    private TextField DBPassword;

    DataFetcher dataFetcher;

    @FXML
    private URL location;

    @FXML
    private ResourceBundle resources;

    @FXML
    private void initialize()
    {
    }

    public void setDataFetcher(DataFetcher df) {
        dataFetcher = df;
    }

    @FXML
    private void testConnection() {
        dataFetcher.initializeDM();
    }
}
