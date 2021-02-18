package com.gtohelper.fxml;

import com.gtohelper.domain.HandData;
import com.gtohelper.utility.CardResolver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

public class Hand extends TextFlow {

    @FXML
    Text card1, card2;

    String hand;

    public Hand(HandData.PlayerHandData handData) {
        this(CardResolver.getHandString(handData));
    }

    // Board string is in the format produced by cardResolver.getBoardString( hand data )
    public Hand(String hand) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Hand.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        this.hand = hand;

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
        String[] cards = hand.split(" ");
        if(cards.length != 2) {
            return;
        }

        setTextFromCard(card1, cards[0]);
        setTextFromCard(card2, cards[1]);
    }

    // There's a much more elegant way of doing all of this... but whatever.
    private void setTextFromCard(Text text, String card) {
        if(text != card2)
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
