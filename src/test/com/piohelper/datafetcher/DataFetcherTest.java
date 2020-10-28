package com.piohelper.datafetcher;

import org.junit.jupiter.api.Test;

class DataFetcherTest {

    @Test
    void connect() {

        String url = "jdbc:postgresql://localhost:5432/PT4 DB";
        String user = "postgres";
        String password = "dbpass";

        DataFetcher.connect(url, user, password);
    }
}