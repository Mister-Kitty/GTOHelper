package com.gtohelper.datafetcher.models.solversettings;

import com.gtohelper.domain.*;
import com.gtohelper.utility.SaveFileHelper;
import com.gtohelper.utility.Saveable;

import java.io.*;
import java.util.HashMap;

public class RangeFilesModel extends Saveable {
    public RangeFilesModel(SaveFileHelper saveHelper) {
        super(saveHelper, "RangeFiles");
    }

    @Override
    public HashMap<String, String> getDefaultValues() {
        HashMap<String, String> values = new HashMap<>();
        values.put("rangeFolderLocation", "");
        return values;
    }

    public Ranges loadRangeFromRangeFolder(String rangeFolderLocation) {
        Ranges ranges = new Ranges();

        fillLimpRanges(ranges, rangeFolderLocation);
        fillRFIRanges(ranges, rangeFolderLocation);
        fillIPRanges(ranges, rangeFolderLocation, Situation.VRFI, LastAction.CALL);
        fillIPRanges(ranges, rangeFolderLocation, Situation.VRFI, LastAction.RAISE);
        fillOPRanges(ranges, rangeFolderLocation, Situation.V3BET, LastAction.CALL);
        fillOPRanges(ranges, rangeFolderLocation, Situation.V3BET, LastAction.RAISE);
        fillIPRanges(ranges, rangeFolderLocation, Situation.V4BET, LastAction.CALL);
        fillIPRanges(ranges, rangeFolderLocation, Situation.V4BET, LastAction.RAISE);
        fillOPRanges(ranges, rangeFolderLocation, Situation.V5BET, LastAction.CALL);

        ranges.fillEmptyRanges();
        return ranges;
    }

    private void fillLimpRanges(Ranges ranges, String rangeFolderLocation) {
        PreflopState bbState = new PreflopState(Situation.LIMP, Seat.BB, Seat.SB, LastAction.CALL);
        loadFileIntoRanges(ranges, bbState, rangeFolderLocation + "\\Limp\\BB Call.txt");

        PreflopState sbState = new PreflopState(Situation.LIMP, Seat.SB, Seat.BB, LastAction.CALL);
        loadFileIntoRanges(ranges, sbState, rangeFolderLocation + "\\Limp\\SB Call.txt");

        for(int i = 2; i < Seat.preflopPositionsDESC.length; i++) {
            PreflopState state = new PreflopState(Situation.LIMP, Seat.preflopPositionsDESC[i], Seat.BB, LastAction.CALL);
            loadFileIntoRanges(ranges, state,
                    rangeFolderLocation + String.format("\\Limp\\%s Call.txt", state.heroSeat));
        }
    }

    private void fillRFIRanges(Ranges ranges, String rangeFolderLocation) {
        for(int i = 1; i < Seat.preflopPositionsDESC.length; i++) {
            PreflopState state = new PreflopState(Situation.RFI, Seat.preflopPositionsDESC[i], Seat.BB, LastAction.RAISE);
            loadFileIntoRanges(ranges, state,
                    rangeFolderLocation + String.format("\\RFI\\%s Raise.txt", state.heroSeat));
        }
    }

    private void fillIPRanges(Ranges ranges, String rangeFolderLocation, Situation situation, LastAction lastHeroAction) {
        for(int hSeat = 0; hSeat < Seat.preflopPositionsDESC.length - 1; hSeat++) {
            for (int vSeat = hSeat + 1; vSeat < Seat.preflopPositionsDESC.length; vSeat++) {
                PreflopState state = new PreflopState(situation, Seat.preflopPositionsDESC[hSeat],
                        Seat.preflopPositionsDESC[vSeat], lastHeroAction);

                loadFileIntoRanges(ranges, state,
                        rangeFolderLocation + String.format("\\%s\\vs %s\\%s %s.txt",
                                state.situation.name, state.villainSeat, state.heroSeat, state.lastHeroAction.name));
            }
        }
    }

    private void fillOPRanges(Ranges ranges, String rangeFolderLocation, Situation situation, LastAction lastHeroAction) {
        for(int hSeat = 1; hSeat < Seat.preflopPositionsDESC.length; hSeat++) {
            for (int vSeat = 0; vSeat < hSeat; vSeat++) {
                PreflopState state = new PreflopState(situation, Seat.preflopPositionsDESC[hSeat],
                        Seat.preflopPositionsDESC[vSeat], lastHeroAction);

                loadFileIntoRanges(ranges, state,
                        rangeFolderLocation + String.format("\\%s\\%s vs\\%s %s.txt",
                                state.situation.name, state.heroSeat, state.villainSeat, state.lastHeroAction.name));
            }
        }
    }

    private void loadFileIntoRanges(Ranges ranges, PreflopState state, String fileLocation) {
        try {
            File file = new File(fileLocation);
            RangeData data = new RangeData(loadFile(file));
            ranges.addRangeForAction(state, data);
        } catch (IOException e) {
            // In most but not all cases, missing a range file is fine.
        }
    }

    private String loadFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        return new String(data, "UTF-8");
    }
}
