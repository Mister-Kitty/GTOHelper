package com.piohelper.datafetcher;

import com.piohelper.datafetcher.models.PioHelper;
import org.junit.jupiter.api.Test;

class DataFetcherTest {

    @Test
    void connect() {

        String url = "jdbc:postgresql://localhost:5432/PT4 DB";
        String user = "postgres";
        String password = "dbpass";

        PioHelper.connect(url, user, password);
    }
}