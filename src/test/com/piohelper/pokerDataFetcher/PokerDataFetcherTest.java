package com.piohelper.pokerDataFetcher;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PokerDataFetcherTest {

    @Test
    void connect() {

        String url = "jdbc:postgresql://localhost:5432/PT4 DB";
        String user = "postgres";
        String password = "dbpass";

        PokerDataFetcher.connect(url, user, password);
    }
}