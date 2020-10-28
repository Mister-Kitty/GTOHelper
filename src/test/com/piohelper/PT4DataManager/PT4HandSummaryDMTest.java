package com.piohelper.PT4DataManager;

import com.piohelper.datafetcher.DataFetcher;
import com.piohelper.domain.HandSummary;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.fail;

public class PT4HandSummaryDMTest {
    String url = "jdbc:postgresql://localhost:5432/PT4 DB";
    String user = "postgres";
    String password = "dbpass";

    @Test
    void getRowCount() {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PT4HandSummaryDM handSummaryDM = new PT4HandSummaryDM(con);

            int rowCount = handSummaryDM.getRowCount();
            System.out.println("rowcount = " + rowCount);
        } catch (SQLException ex) {
            fail();
        }
    }

    @Test
    void getNHandSummaries() {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            PT4HandSummaryDM handSummaryDM = new PT4HandSummaryDM(con);

            ArrayList<HandSummary> hands = handSummaryDM.getNHandSummaries(5);
            if(hands.size() != 5)
                fail();

            System.out.println("rowcount = " + hands.size());
        } catch (SQLException ex) {
            fail();
        }
    }
}