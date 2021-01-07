package com.gtohelper.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RakeDataTest {

    @Test
    public RakeData initRakeDataText() {
        String fileData = settings;
        RakeData rakeData = new RakeData();
        for (String line : fileData.split("\n")) {
            if (line.startsWith("#"))
                continue;

            String[] commaSplit = line.split(",");
            float bbLimit = Float.parseFloat(commaSplit[0]);
            float rakePercent = Float.parseFloat(commaSplit[1]);
            float twoPlayerCap = Float.parseFloat(commaSplit[2]);
            float fourPlayerCap = Float.parseFloat(commaSplit[3]);
            float fullPlayerCap = Float.parseFloat(commaSplit[4]);

            rakeData.addRakeRow(bbLimit, rakePercent, twoPlayerCap, fourPlayerCap, fullPlayerCap);
        }

        return rakeData;
    }

    @Test
    public void testRakePercent() {
        RakeData rakeData = initRakeDataText();

        float result = rakeData.getRakeForBB(1.99f);
        assert result == 5f;

        result = rakeData.getRakeForBB(.01f);
        assert result == 3.50f;

        result = rakeData.getRakeForBB(.02f);
        assert result == 3.50f;

        result = rakeData.getRakeForBB(0.05f);
        assert result == 4.15f;

        result = rakeData.getRakeForBB(0.49f);
        assert result == 4.5f;

        result = rakeData.getRakeForBB(0.5f);
        assert result == 5f;

        result = rakeData.getRakeForBB(199f);
        assert result == 4.5f;

        result = rakeData.getRakeForBB(220f);
        assert result == 4.5f;
    }


    @Test
    public void testCap() {
        RakeData rakeData = initRakeDataText();

        float result = rakeData.getCapForBB(1.99f, 2);
        assert result == 1.00f;

        result = rakeData.getCapForBB(.01f, 2);
        assert result == 0.30f;

        result = rakeData.getCapForBB(.02f, 3);
        assert result == 0.30f;

        result = rakeData.getCapForBB(0.05f, 4);
        assert result == 0.50f;

        result = rakeData.getCapForBB(0.49f, 5);
        assert result == 2.00f;

        result = rakeData.getCapForBB(0.5f, 6);
        assert result == 2.00f;

        result = rakeData.getCapForBB(199f, 9);
        assert result == 5.00f;

        result = rakeData.getCapForBB(220f, 10);
        assert result == 5.00f;
    }



    private static final String settings = "# BBs MUST be in increasing order\n" +
            "# Big Blind $USD, Rake Percent, 2 Player Cap, 3-4 Player Cap, 5+ Player Cap\n" +
            "0.02, 3.50, 0.30, 0.30, 0.30\n" +
            "0.05, 4.15, 0.50, 0.50, 1.00\n" +
            "0.10, 4.50, 0.50, 1.00, 1.50\n" +
            "0.25, 4.50, 0.50, 1.00, 2.00\n" +
            "0.50, 5.00, 0.75, 0.75, 2.00\n" +
            "1, 5.00, 1.00, 1.00, 2.50\n" +
            "2, 5.0, 1.25, 1.25, 2.75\n" +
            "4, 5.0, 1.50, 1.50, 3.00\n" +
            "5, 5.0, 1.50, 1.50, 3.00\n" +
            "6, 5.0, 1.50, 1.50, 3.50\n" +
            "10, 4.5, 1.50, 1.50, 3.00\n" +
            "20, 4.5, 1.75, 1.75, 3.00\n" +
            "50, 4.5, 2.25, 2.00, 3.00\n" +
            "100, 4.5, 2.50, 3.00, 5.00\n" +
            "200, 4.5, 3.00, 5.00, 5.00";
}