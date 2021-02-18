package com.gtohelper.fxml;

import com.gtohelper.domain.HandData;
import com.gtohelper.utility.CardResolver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class Board extends TextFlow {

    @FXML
    Text card1, card2, card3, card4, card5;

    String board;

    public Board(HandData handData) {
        this(CardResolver.getBoardString(handData));
    }

    // Board string is in the format produced by cardResolver.getBoardString( hand data )
    public Board(String board) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Board.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        this.board = board;

        try
        {
            fxmlLoader.load();
        } catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }

    }

    @FXML
    void initialize() {
        setFromBoardString();
    }

    private void setFromBoardString() {
        String[] cards = board.split(" ");
        switch(cards.length) {
            case(5):
                setRiver(cards[4]);
            case(4):
                setTurn(cards[3]);
            case(3):
                setFlop(cards[0], cards[1], cards[2]);
                break;
        }
    }

    private void setFlop(String first, String second, String third) {
        setTextFromCard(card1, first);
        setTextFromCard(card2, second);
        setTextFromCard(card3, third);
    }

    private void setTurn(String fourth) {
        setTextFromCard(card4, fourth);
    }

    private void setRiver(String fifth) {
        setTextFromCard(card5, fifth);
    }

    // There's a much more elegant way of doing all of this... but whatever.
    private void setTextFromCard(Text text, String card) {
        if(text != card5)
            text.setText(card + " ");
        else
            text.setText(card);

        if(card.contains("c"))
            text.setFill(Color.DARKGREEN);
        else if(card.contains("d"))
            text.setFill(Color.DARKBLUE);
        else if(card.contains("h"))
            text.setFill(Color.DARKRED);
        else // contains s
            text.setFill(Color.BLACK);
    }
}
