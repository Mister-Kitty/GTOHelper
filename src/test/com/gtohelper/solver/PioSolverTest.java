package com.gtohelper.solver;

import com.gtohelper.domain.BettingOptions;
import com.gtohelper.domain.HandSolveSettings;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

class PioSolverTest {

    static ISolver solver = new PioSolver();
    final static String pioLocation = "C:\\PioSolver Edge\\PioSOLVER-edge.exe";

    public static void setup() throws IOException {
        solver.connectAndInitToSolver(pioLocation);
    }

    public static void tearDown() {
        solver.disconnect();
    }

    /*
        Start with some utility functions used throughout
     */

    void setRange() throws IOException {
        solver.setRange("IP", IPRange1);
        solver.setRange("OOP", OOPRange1);
    }

    void setFlopData(HandSolveSettings handSolveSettings) throws IOException {
        solver.setBoard(BasicTestBoard);
        solver.setPotAndAccuracy(0, 0, handSolveSettings.initialPot, 1.628F);
        solver.setEffectiveStack(handSolveSettings.initialEffectiveStack);
    }

    /*
        And now, test 1 - basic
     */

    void fillFlopData(HandSolveSettings handSolveSettings) {
        handSolveSettings.initialPot = 185;
        handSolveSettings.initialEffectiveStack = 910;
    }

    void fillBettingOptionsBasicTest(BettingOptions bettingOptions) {
        bettingOptions.options.allInThresholdPercent = 100;
        bettingOptions.options.addAllinOnlyIfPercentage = 500;
        bettingOptions.options.forceFlopOOPBet = false;
        bettingOptions.options.forceFlopOOPCheckIPBet = false;
    }

    void fillBetsizesBasicTest(BettingOptions bettingOptions) {
        bettingOptions.IPFlop.setBets("52");
        bettingOptions.IPFlop.setRaises("2.5x");
        bettingOptions.OOPFlop.setDonks("52");
        bettingOptions.OOPFlop.setBets("52");
        bettingOptions.OOPFlop.setRaises("2.5x");

        bettingOptions.IPTurn.setBets("52");
        bettingOptions.IPTurn.setRaises("3x");
        bettingOptions.OOPTurn.setDonks("");
        bettingOptions.OOPTurn.setBets("52");
        bettingOptions.OOPTurn.setRaises("3x");

        bettingOptions.IPRiver.setBets("52");
        bettingOptions.IPRiver.setRaises("3x");
        bettingOptions.OOPRiver.setDonks("");
        bettingOptions.OOPRiver.setBets("52");
        bettingOptions.OOPRiver.setRaises("3x");
    }

    @Test
    void getAddLinesBasicTest() throws IOException {
        HandSolveSettings handSolveSettings = new HandSolveSettings();
        BettingOptions bettingOptions = new BettingOptions("Test");

        fillBettingOptionsBasicTest(bettingOptions);
        fillBetsizesBasicTest(bettingOptions);
        fillFlopData(handSolveSettings);

        setFlopData(handSolveSettings);
        setRange();

        solver.setIsomorphism(true, false);
        solver.clearLines();

        GameTree tree = new GameTree();
        tree.buildGameTree(bettingOptions, handSolveSettings);

        // Tree is build. Test results.
        ArrayList<String> basicTestSolverResults = tree.getAllInLeaves(bettingOptions);
        for(String s : basicTestSolverResults) {
            if(!basicTestResults.contains(s))
                assert false;
        }

        for(String s : basicTestResults) {
            if(!basicTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        for(String leaf : basicTestSolverResults) {
            solver.addLine(leaf);
        }
        solver.buildTree();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 457 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }

    void runFullBasicTest() throws IOException {
        getAddLinesBasicTest();
        solver.go();
        String calcResults = solver.waitForSolve();
        assert(!calcResults.isEmpty());
    }

    /*
        Raise Test
     */

    void fillBetsizesRaisesTest(BettingOptions bettingOptions) {
        bettingOptions.IPFlop.setBets("52");
        bettingOptions.IPFlop.setRaises("2.5x");
        bettingOptions.OOPFlop.setDonks("52");
        bettingOptions.OOPFlop.setBets("52");
        bettingOptions.OOPFlop.setRaises("2.5x");

        bettingOptions.IPTurn.setBets("52");
        bettingOptions.IPTurn.setRaises("80,3x");
        bettingOptions.OOPTurn.setDonks("");
        bettingOptions.OOPTurn.setBets("52");
        bettingOptions.OOPTurn.setRaises("50,3x");

        bettingOptions.IPRiver.setBets("52");
        bettingOptions.IPRiver.setRaises("60,3x");
        bettingOptions.OOPRiver.setDonks("");
        bettingOptions.OOPRiver.setBets("52");
        bettingOptions.OOPRiver.setRaises("50,3x");
    }

    @Test
    void getAddLinesRaisesTest() throws IOException {
        HandSolveSettings handSolveSettings = new HandSolveSettings();
        BettingOptions bettingOptions = new BettingOptions("Test");

        fillBettingOptionsBasicTest(bettingOptions);
        fillBetsizesRaisesTest(bettingOptions);
        fillFlopData(handSolveSettings);

        setFlopData(handSolveSettings);
        setRange();

        solver.setIsomorphism(true, false);
        solver.clearLines();

        GameTree tree = new GameTree();
        tree.buildGameTree(bettingOptions, handSolveSettings);

        // Tree is build. Test results.
        ArrayList<String> raiseTestSolverResults = tree.getAllInLeaves(bettingOptions);
        for(String s : raiseTestSolverResults) {
            if(!raiseTestResults.contains(s))
                assert false;
        }

        for(String s : raiseTestResults) {
            if(!raiseTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        for(String leaf : raiseTestSolverResults) {
            solver.addLine(leaf);
        }
        solver.buildTree();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 652 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }

    void runFullRaisesTest() throws IOException {
        getAddLinesBasicTest();
        solver.go();
        String calcResults = solver.waitForSolve();
        assert(!calcResults.isEmpty());
    }

    /*
        'allin' and 'add all-in' test.
    */

    void fillBetsizesAllinTest(BettingOptions bettingOptions) {
        bettingOptions.IPFlop.setBets("52");
        bettingOptions.IPFlop.setRaises("2.5x");
        bettingOptions.IPFlop.setAddAllIn(true);
        bettingOptions.OOPFlop.setDonks("52");
        bettingOptions.OOPFlop.setBets("52");
        bettingOptions.OOPFlop.setRaises("2.5x");
        bettingOptions.OOPFlop.setAddAllIn(true);

        bettingOptions.IPTurn.setBets("52,allin");
        bettingOptions.IPTurn.setRaises("3x");
        bettingOptions.OOPTurn.setDonks("");
        bettingOptions.OOPTurn.setBets("52");
        bettingOptions.OOPTurn.setRaises("3x");

        bettingOptions.IPRiver.setBets("52");
        bettingOptions.IPRiver.setRaises("3x");
        bettingOptions.IPRiver.setAddAllIn(true);
        bettingOptions.OOPRiver.setDonks("");
        bettingOptions.OOPRiver.setBets("52");
        bettingOptions.OOPRiver.setRaises("3x");
        bettingOptions.OOPRiver.setAddAllIn(true);
    }

    @Test
    void getAddLinesAllinTest() throws IOException {
        HandSolveSettings handSolveSettings = new HandSolveSettings();
        BettingOptions bettingOptions = new BettingOptions("Test");

        fillBettingOptionsBasicTest(bettingOptions);
        fillBetsizesAllinTest(bettingOptions);
        fillFlopData(handSolveSettings);

        setFlopData(handSolveSettings);
        setRange();

        solver.setIsomorphism(true, false);
        solver.clearLines();

        GameTree tree = new GameTree();
        tree.buildGameTree(bettingOptions, handSolveSettings);

        // Tree is build. Test results.
        ArrayList<String> allinTestSolverResults = tree.getAllInLeaves(bettingOptions);
        for(String s : allinTestSolverResults) {
            if(!allinTestResults.contains(s))
                assert false;
        }

        for(String s : allinTestResults) {
            if(!allinTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        for(String leaf : allinTestSolverResults) {
            solver.addLine(leaf);
        }
        solver.buildTree();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 580 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }

    void runFullAllinTest() throws IOException {
        getAddLinesAllinTest();
        solver.go();
        String calcResults = solver.waitForSolve();
        assert(!calcResults.isEmpty());
    }

    /*
        Don't 3-bet+ test
    */

    void fillBetsizesDont3BetTest(BettingOptions bettingOptions) {
        bettingOptions.IPFlop.setBets("52");
        bettingOptions.IPFlop.setRaises("2.5x");
        bettingOptions.IPFlop.setDont3BetPlus(true);
        bettingOptions.OOPFlop.setDonks("52");
        bettingOptions.OOPFlop.setBets("52");
        bettingOptions.OOPFlop.setRaises("2.5x");

        bettingOptions.IPTurn.setBets("52");
        bettingOptions.IPTurn.setRaises("3x");
        bettingOptions.OOPTurn.setDonks("");
        bettingOptions.OOPTurn.setBets("52");
        bettingOptions.OOPTurn.setRaises("3x");

        bettingOptions.IPRiver.setBets("52");
        bettingOptions.IPRiver.setRaises("3x");
        bettingOptions.IPRiver.setDont3BetPlus(true);
        bettingOptions.OOPRiver.setDonks("");
        bettingOptions.OOPRiver.setBets("52");
        bettingOptions.OOPRiver.setRaises("3x");
    }

    @Test
    void getAddLinesDont3BetTest() throws IOException {
        HandSolveSettings handSolveSettings = new HandSolveSettings();
        BettingOptions bettingOptions = new BettingOptions("Test");

        fillBettingOptionsBasicTest(bettingOptions);
        fillBetsizesDont3BetTest(bettingOptions);
        fillFlopData(handSolveSettings);

        setFlopData(handSolveSettings);
        setRange();

        solver.setIsomorphism(true, false);
        solver.clearLines();

        GameTree tree = new GameTree();
        tree.buildGameTree(bettingOptions, handSolveSettings);

        // Tree is build. Test results.
        ArrayList<String> dont3BetTestSolverResults = tree.getAllInLeaves(bettingOptions);
        for(String s : dont3BetTestSolverResults) {
            if(!dont3BetTestResults.contains(s))
                assert false;
        }

        for(String s : dont3BetTestResults) {
            if(!dont3BetTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        for(String leaf : dont3BetTestResults) {
            solver.addLine(leaf);
        }
        solver.buildTree();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 402 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }


    void runFullDont3BetTest() throws IOException {
        getAddLinesDont3BetTest();
        solver.go();
        String calcResults = solver.waitForSolve();
        assert(!calcResults.isEmpty());
    }

    /*
        Allin threshold and Allin pot percent aka Allin options
    */

    void fillBettingOptionsAllinOptionsTest(BettingOptions bettingOptions) {
        bettingOptions.options.allInThresholdPercent = 67;
        bettingOptions.options.addAllinOnlyIfPercentage = 250;
        bettingOptions.options.forceFlopOOPBet = false;
        bettingOptions.options.forceFlopOOPCheckIPBet = false;
    }


    @Test
    void getAddLinesAllinOptionsTest() throws IOException {
        HandSolveSettings handSolveSettings = new HandSolveSettings();
        BettingOptions bettingOptions = new BettingOptions("Test");

        fillBettingOptionsAllinOptionsTest(bettingOptions);
        fillBetsizesAllinTest(bettingOptions);
        fillFlopData(handSolveSettings);

        setFlopData(handSolveSettings);
        setRange();

        solver.setIsomorphism(true, false);
        solver.clearLines();

        GameTree tree = new GameTree();
        tree.buildGameTree(bettingOptions, handSolveSettings);

        // Tree is build. Test results.
        ArrayList<String> allinTestSolverResults = tree.getAllInLeaves(bettingOptions);
        for(String s : allinTestSolverResults) {
            if(!allinOptionsTestResults.contains(s))
                assert false;
        }

        for(String s : allinOptionsTestResults) {
            if(!allinTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        for(String leaf : dont3BetTestResults) {
            solver.addLine(leaf);
        }
        solver.buildTree();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 402 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }

    /*
        Force flop bets and checks test.


    void initializeOptionsForceOOPBetTest() throws IOException {
        int allInThresholdPercent = 67;
        int allInOnlyIfLessThanNPercent = 500;
        final boolean forceOOPBet = true;
        final boolean forceOOPCheckIPBet = false;
        solver.setGameTreeOptions(allInThresholdPercent, allInOnlyIfLessThanNPercent, forceOOPBet, forceOOPCheckIPBet);

        final boolean flopIso = true;
        final boolean turnIso = false;
        solver.setIsomorphism(flopIso, turnIso);
    }

    @Test
    void getAddLinesForceOOPBetTest() throws IOException {
        setRangeBasicTest();
        setFlopDataBasicTest();
        initializeOptionsForceOOPBetTest();
        setBetsizesBasicTest();

        solver.clearLines();
        solver.buildTree();

        // Tree is build. Test results.
        ArrayList<String> ForceOOPBetTestSolverResults = solver.getAllInLeaves();
        for(String s : ForceOOPBetTestSolverResults) {
            if(!ForceOOPBetTestResults.contains(s))
                assert false;
        }

        for(String s : ForceOOPBetTestResults) {
            if(!ForceOOPBetTestSolverResults.contains(s))
                assert false;
        }

        // Results have been validated. Send the tree to Pio and validate the tree size estimate
        solver.setBuiltTreeAsActive();

        String treeSize = solver.getEstimateSchematicTree();
        assert(treeSize.equals("estimated tree size: 392 MB"));

        String showMemory = solver.getShowMemory();
        assert(!showMemory.isEmpty());

        String calc = solver.getCalcResults();
        assert(!calc.isEmpty());
    }

    void initializeOptionsForceIPBetTest() throws IOException {
        int allInThresholdPercent = 67;
        int allInOnlyIfLessThanNPercent = 500;
        final boolean forceOOPBet = false;
        final boolean forceOOPCheckIPBet = false;
        solver.setGameTreeOptions(allInThresholdPercent, allInOnlyIfLessThanNPercent, forceOOPBet, forceOOPCheckIPBet);

        final boolean flopIso = true;
        final boolean turnIso = false;
        solver.setIsomorphism(flopIso, turnIso);
    }

     */


    final String BasicTestBoard = "Qs Jh 2h";

    final ArrayList<String> basicTestResults = new ArrayList<>(
            Arrays.asList("0 0 0 0 0 96 288 672 910",
        "0 0 0 0 96 288 672 910",
        "0 0 0 96 288 288 288 684 910",
        "0 0 0 96 288 288 684 910",
        "0 0 0 96 288 672 672 672 910",
        "0 0 0 96 288 672 910",
        "0 0 0 96 96 96 292 684 910",
        "0 0 96 288 288 288 684 910",
        "0 0 96 288 672 672 672 910",
        "0 0 96 288 672 672 910",
        "0 0 96 288 672 910",
        "0 0 96 96 292 684 910",
        "0 0 96 96 96 292 684 910",
        "0 96 240 240 240 240 240 586 910",
        "0 96 240 240 240 240 586 910",
        "0 96 240 240 240 586 586 586 910",
        "0 96 240 240 240 586 910",
        "0 96 240 240 586 586 586 910",
        "0 96 240 240 586 586 910",
        "0 96 240 240 586 910",
        "0 96 240 456 456 456 456 456 910",
        "0 96 240 456 456 456 456 910",
        "0 96 240 456 456 456 910",
        "0 96 240 456 780 780 780 780 780 910",
        "0 96 240 456 780 780 780 780 910",
        "0 96 240 456 780 780 780 910",
        "0 96 240 456 780 780 910",
        "0 96 240 456 780 910",
        "0 96 96 96 292 292 292 692 910",
        "0 96 96 96 292 684 684 684 910",
        "0 96 96 96 292 684 684 910",
        "0 96 96 96 292 684 910",
        "0 96 96 96 96 292 684 910",
        "0 96 96 96 96 96 292 684 910",
        "96 240 240 240 240 240 586 910",
        "96 240 240 240 240 586 910",
        "96 240 240 240 586 586 586 910",
        "96 240 240 240 586 910",
        "96 240 456 456 456 456 456 910",
        "96 240 456 456 456 456 910",
        "96 240 456 456 456 910",
        "96 240 456 456 910",
        "96 240 456 780 780 780 780 780 910",
        "96 240 456 780 780 780 780 910",
        "96 240 456 780 780 780 910",
        "96 240 456 780 910",
        "96 96 292 292 292 692 910",
        "96 96 292 292 692 910",
        "96 96 292 684 684 684 910",
        "96 96 292 684 910",
        "96 96 96 292 292 292 692 910",
        "96 96 96 292 684 684 684 910",
        "96 96 96 292 684 684 910",
        "96 96 96 292 684 910",
        "96 96 96 96 292 684 910",
        "96 96 96 96 96 292 684 910"));


    final ArrayList<String> raiseTestResults = new ArrayList<>(
            Arrays.asList("0 0 0 0 0 96 284 660 910",
                    "0 0 0 0 0 96 284 736 910",
                    "0 0 0 0 0 96 288 672 910",
                    "0 0 0 0 0 96 288 745 910",
                    "0 0 0 0 96 288 668 910",
                    "0 0 0 0 96 288 672 910",
                    "0 0 0 0 96 322 736 910",
                    "0 0 0 0 96 322 774 910",
                    "0 0 0 96 284 284 284 676 910",
                    "0 0 0 96 284 284 676 910",
                    "0 0 0 96 284 660 660 660 910",
                    "0 0 0 96 284 660 910",
                    "0 0 0 96 284 886 886 886 910",
                    "0 0 0 96 284 886 910",
                    "0 0 0 96 288 288 288 684 910",
                    "0 0 0 96 288 288 684 910",
                    "0 0 0 96 288 672 672 672 910",
                    "0 0 0 96 288 672 910",
                    "0 0 0 96 288 897 897 897 910",
                    "0 0 0 96 288 897 910",
                    "0 0 0 96 96 96 292 676 910",
                    "0 0 0 96 96 96 292 684 910",
                    "0 0 96 288 288 288 684 910",
                    "0 0 96 288 668 668 668 910",
                    "0 0 96 288 668 668 910",
                    "0 0 96 288 668 910",
                    "0 0 96 288 672 672 672 910",
                    "0 0 96 288 672 672 910",
                    "0 0 96 288 672 910",
                    "0 0 96 398 398 398 908 910",
                    "0 0 96 398 888 888 888 910",
                    "0 0 96 398 888 888 910",
                    "0 0 96 398 888 910",
                    "0 0 96 398 910",
                    "0 0 96 96 292 684 910",
                    "0 0 96 96 292 753 910",
                    "0 0 96 96 96 292 676 910",
                    "0 0 96 96 96 292 684 910",
                    "0 96 240 240 240 240 240 586 910",
                    "0 96 240 240 240 240 586 910",
                    "0 96 240 240 240 586 586 586 910",
                    "0 96 240 240 240 586 910",
                    "0 96 240 240 586 586 586 910",
                    "0 96 240 240 586 586 910",
                    "0 96 240 240 586 910",
                    "0 96 240 456 456 456 456 456 910",
                    "0 96 240 456 456 456 456 910",
                    "0 96 240 456 456 456 910",
                    "0 96 240 456 780 780 780 780 780 910",
                    "0 96 240 456 780 780 780 780 910",
                    "0 96 240 456 780 780 780 910",
                    "0 96 240 456 780 780 910",
                    "0 96 240 456 780 910",
                    "0 96 96 96 292 292 292 692 910",
                    "0 96 96 96 292 676 676 676 910",
                    "0 96 96 96 292 676 676 910",
                    "0 96 96 96 292 676 910",
                    "0 96 96 96 292 684 684 684 910",
                    "0 96 96 96 292 684 684 910",
                    "0 96 96 96 292 684 910",
                    "0 96 96 96 96 292 684 910",
                    "0 96 96 96 96 292 753 910",
                    "0 96 96 96 96 96 292 676 910",
                    "0 96 96 96 96 96 292 684 910",
                    "96 240 240 240 240 240 586 910",
                    "96 240 240 240 240 586 910",
                    "96 240 240 240 586 586 586 910",
                    "96 240 240 240 586 910",
                    "96 240 456 456 456 456 456 910",
                    "96 240 456 456 456 456 910",
                    "96 240 456 456 456 910",
                    "96 240 456 456 910",
                    "96 240 456 780 780 780 780 780 910",
                    "96 240 456 780 780 780 780 910",
                    "96 240 456 780 780 780 910",
                    "96 240 456 780 910",
                    "96 96 292 292 292 692 910",
                    "96 96 292 292 692 910",
                    "96 96 292 684 684 684 910",
                    "96 96 292 684 910",
                    "96 96 292 907 907 907 910",
                    "96 96 292 907 910",
                    "96 96 96 292 292 292 692 910",
                    "96 96 96 292 676 676 676 910",
                    "96 96 96 292 676 676 910",
                    "96 96 96 292 676 910",
                    "96 96 96 292 684 684 684 910",
                    "96 96 96 292 684 684 910",
                    "96 96 96 292 684 910",
                    "96 96 96 96 292 684 910",
                    "96 96 96 96 292 753 910",
                    "96 96 96 96 96 292 676 910",
                    "96 96 96 96 96 292 684 910"));

    final ArrayList<String> allinTestResults = new ArrayList<>(
            Arrays.asList("0 0 0 0 0 910",
                    "0 0 0 0 0 96 288 672 910",
                    "0 0 0 0 0 96 288 910",
                    "0 0 0 0 0 96 910",
                    "0 0 0 0 910",
                    "0 0 0 0 96 288 672 910",
                    "0 0 0 0 96 288 910",
                    "0 0 0 0 96 910",
                    "0 0 0 910",
                    "0 0 0 96 288 288 288 684 910",
                    "0 0 0 96 288 288 288 910",
                    "0 0 0 96 288 288 684 910",
                    "0 0 0 96 288 288 910",
                    "0 0 0 96 288 672 672 672 910",
                    "0 0 0 96 288 672 910",
                    "0 0 0 96 96 96 292 684 910",
                    "0 0 0 96 96 96 292 910",
                    "0 0 0 96 96 96 910",
                    "0 0 96 288 288 288 684 910",
                    "0 0 96 288 288 288 910",
                    "0 0 96 288 672 672 672 910",
                    "0 0 96 288 672 672 910",
                    "0 0 96 288 672 910",
                    "0 0 96 96 292 684 910",
                    "0 0 96 96 292 910",
                    "0 0 96 96 910",
                    "0 0 96 96 96 292 684 910",
                    "0 0 96 96 96 292 910",
                    "0 0 96 96 96 910",
                    "0 910",
                    "0 96 240 240 240 240 240 586 910",
                    "0 96 240 240 240 240 240 910",
                    "0 96 240 240 240 240 586 910",
                    "0 96 240 240 240 240 910",
                    "0 96 240 240 240 586 586 586 910",
                    "0 96 240 240 240 586 910",
                    "0 96 240 240 240 910",
                    "0 96 240 240 586 586 586 910",
                    "0 96 240 240 586 586 910",
                    "0 96 240 240 586 910",
                    "0 96 240 456 456 456 456 456 910",
                    "0 96 240 456 456 456 456 910",
                    "0 96 240 456 456 456 910",
                    "0 96 240 456 780 780 780 780 780 910",
                    "0 96 240 456 780 780 780 780 910",
                    "0 96 240 456 780 780 780 910",
                    "0 96 240 456 780 780 910",
                    "0 96 240 456 780 910",
                    "0 96 240 456 910",
                    "0 96 240 910",
                    "0 96 910",
                    "0 96 96 96 292 292 292 692 910",
                    "0 96 96 96 292 292 292 910",
                    "0 96 96 96 292 684 684 684 910",
                    "0 96 96 96 292 684 684 910",
                    "0 96 96 96 292 684 910",
                    "0 96 96 96 910",
                    "0 96 96 96 96 292 684 910",
                    "0 96 96 96 96 292 910",
                    "0 96 96 96 96 910",
                    "0 96 96 96 96 96 292 684 910",
                    "0 96 96 96 96 96 292 910",
                    "0 96 96 96 96 96 910",
                    "910",
                    "96 240 240 240 240 240 586 910",
                    "96 240 240 240 240 240 910",
                    "96 240 240 240 240 586 910",
                    "96 240 240 240 240 910",
                    "96 240 240 240 586 586 586 910",
                    "96 240 240 240 586 910",
                    "96 240 240 240 910",
                    "96 240 456 456 456 456 456 910",
                    "96 240 456 456 456 456 910",
                    "96 240 456 456 456 910",
                    "96 240 456 456 910",
                    "96 240 456 780 780 780 780 780 910",
                    "96 240 456 780 780 780 780 910",
                    "96 240 456 780 780 780 910",
                    "96 240 456 780 910",
                    "96 240 456 910",
                    "96 240 910",
                    "96 910",
                    "96 96 292 292 292 692 910",
                    "96 96 292 292 292 910",
                    "96 96 292 292 692 910",
                    "96 96 292 292 910",
                    "96 96 292 684 684 684 910",
                    "96 96 292 684 910",
                    "96 96 96 292 292 292 692 910",
                    "96 96 96 292 292 292 910",
                    "96 96 96 292 684 684 684 910",
                    "96 96 96 292 684 684 910",
                    "96 96 96 292 684 910",
                    "96 96 96 910",
                    "96 96 96 96 292 684 910",
                    "96 96 96 96 292 910",
                    "96 96 96 96 910",
                    "96 96 96 96 96 292 684 910",
                    "96 96 96 96 96 292 910",
                    "96 96 96 96 96 910"));

    final ArrayList<String> dont3BetTestResults = new ArrayList<>(
            Arrays.asList("0 0 0 0 0 96 288",
                    "0 0 0 0 96 288 672", //"0 0 0 0 96 288 672 910",   4bet is replaced with 2bet result.
                    "0 0 0 96 288 288 288 684 910",
                    "0 0 0 96 288 288 684 910",
                    "0 0 0 96 288 672 672 672 910",
                    "0 0 0 96 288 672 910",
                    "0 0 0 96 96 96 292 684",
                    "0 0 96 288 288 288 684 910",
                    "0 0 96 288 672 672 672 910",
                    "0 0 96 288 672 672 910",
                    "0 0 96 288 672 910",
                    "0 0 96 96 292 684 910",
                    "0 0 96 96 96 292 684",
                    "0 96 240 240 240 240 240 586 910",
                    "0 96 240 240 240 240 586 910",
                    "0 96 240 240 240 586 586 586 910",
                    "0 96 240 240 240 586 910",
                    "0 96 240 240 586 586 586 910",
                    "0 96 240 240 586 586 910",
                    "0 96 240 240 586 910",
                    "0 96 96 96 292 292 292 692 910",
                    "0 96 96 96 292 684 684 684 910",
                    "0 96 96 96 292 684 684 910",
                    "0 96 96 96 292 684 910",
                    "0 96 96 96 96 292 684 910",
                    "0 96 96 96 96 96 292 684",
                    "96 240 240 240 240 240 586 910",
                    "96 240 240 240 240 586 910",
                    "96 240 240 240 586 586 586 910",
                    "96 240 240 240 586 910",
                    "96 240 456 456 456 456 456 910",
                    "96 240 456 456 456 456 910",
                    "96 240 456 456 456 910",
                    "96 240 456 456 910",
                    // "96 240 456 780 780 780 780 780 910", 4bets are removed as per comment above
                    // "96 240 456 780 780 780 780 910",
                    // "96 240 456 780 780 780 910",
                    // "96 240 456 780 910",
                    "96 96 292 292 292 692 910",
                    "96 96 292 292 692 910",
                    "96 96 292 684 684 684 910",
                    "96 96 292 684 910",
                    "96 96 96 292 292 292 692 910",
                    "96 96 96 292 684 684 684 910",
                    "96 96 96 292 684 684 910",
                    "96 96 96 292 684 910",
                    "96 96 96 96 292 684 910",
                    "96 96 96 96 96 292 684"));


    final ArrayList<String> allinOptionsTestResults = new ArrayList<>(
            Arrays.asList("0 0 0 0 0 96 288 910",
                    "0 0 0 0 0 96 910",
                    "0 0 0 0 96 288 910",
                    "0 0 0 0 96 910",
                    "0 0 0 910",
                    "0 0 0 96 288 288 288 910",
                    "0 0 0 96 288 288 910",
                    "0 0 0 96 288 910",
                    "0 0 0 96 96 96 292 910",
                    "0 0 0 96 96 96 910",
                    "0 0 96 288 288 288 910",
                    "0 0 96 288 910",
                    "0 0 96 96 292 910",
                    "0 0 96 96 910",
                    "0 0 96 96 96 292 910",
                    "0 0 96 96 96 910",
                    "0 96 240 240 240 240 240 586 910",
                    "0 96 240 240 240 240 240 910",
                    "0 96 240 240 240 240 586 910",
                    "0 96 240 240 240 240 910",
                    "0 96 240 240 240 586 586 586 910",
                    "0 96 240 240 240 586 910",
                    "0 96 240 240 240 910",
                    "0 96 240 240 586 586 586 910",
                    "0 96 240 240 586 586 910",
                    "0 96 240 240 586 910",
                    "0 96 240 456 456 456 456 456 910",
                    "0 96 240 456 456 456 456 910",
                    "0 96 240 456 456 456 910",
                    "0 96 240 456 910",
                    "0 96 240 910",
                    "0 96 910",
                    "0 96 96 96 292 292 292 910",
                    "0 96 96 96 292 910",
                    "0 96 96 96 910",
                    "0 96 96 96 96 292 910",
                    "0 96 96 96 96 910",
                    "0 96 96 96 96 96 292 910",
                    "0 96 96 96 96 96 910",
                    "96 240 240 240 240 240 586 910",
                    "96 240 240 240 240 240 910",
                    "96 240 240 240 240 586 910",
                    "96 240 240 240 240 910",
                    "96 240 240 240 586 586 586 910",
                    "96 240 240 240 586 910",
                    "96 240 240 240 910",
                    "96 240 456 456 456 456 456 910",
                    "96 240 456 456 456 456 910",
                    "96 240 456 456 456 910",
                    "96 240 456 456 910",
                    "96 240 456 910",
                    "96 240 910",
                    "96 910",
                    "96 96 292 292 292 910",
                    "96 96 292 292 910",
                    "96 96 292 910",
                    "96 96 96 292 292 292 910",
                    "96 96 96 292 910",
                    "96 96 96 910",
                    "96 96 96 96 292 910",
                    "96 96 96 96 910",
                    "96 96 96 96 96 292 910",
                    "96 96 96 96 96 910"));


    final ArrayList<String> ForceOOPBetTestResults = new ArrayList<>(
            Arrays.asList(""));


    final ArrayList<String> ForceIPBetTestResults = new ArrayList<>(
            Arrays.asList(""));

    final String OOPRange1 = "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0 0 0 0 0 0.5 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0 0 " +
            "0 0 0 0 0 0.5 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0.25 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0.25 0.25 0 0 0 " +
            "0 0 0 0 0.5 0 0 0 0.75 0.25 0.25 0.25 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0.25 0 0 0 0 0 " +
            "0 0 0 0 0 0.5 0 0 0 0.75 0 0.25 0.25 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0.25 0.25 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 " +
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0" +
            " 0 0 0.75 0.5 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0.75 0" +
            " 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0.75 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0.75 0.75 0.75 0 0 0" +
            " 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0.75 0 0 0 0 0 0 " +
            "0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0.75 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0.75 0.75 0.75 " +
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 " +
            "0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0" +
            " 0 0.75 0 0 0 0.75 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0" +
            " 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 0 1 1 0" +
            " 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 0.75 0 0 0 0.75 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 1 0 0 0 0 0 0 " +
            "0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "0 0 0.5 0 0 0 0.75 0 0 0 0.75 1 1 1 0 0 0 0 0 0 0 0 0.25 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 1 0 0 0 1 0.5 0.5 0.5" +
            " 0 0 0 0 0 0 0 0 0 0.25 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 1 0 0 0.5 1 0.5 0.5 1 0 0 0 0 0 0 0 0 0 0 0.25 0 0 0 " +
            "0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 1 0 0.5 0.5 1 0.5 1 1 0 0 0 0 0 0 0 0 0 0 0 0.25 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "0 0 0 0 0 0 0 0.75 0 0 0 1 0.5 0.5 0.5 1 1 1 1 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0 1 0.5 " +
            "0.5 0.5 1 0.75 0.75 0.75 1 1 1 1 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0 0.5 1 0.5 0.5 0.75 1 " +
            "0.75 0.75 1 1 1 1 1 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0 0.5 0.5 1 0.5 0.75 0.75 1 0.75 1 1" +
            " 1 1 1 1 0 0 0 0.5 0 0 0 0.5 0 0 0 0.75 0 0 0 0.75 0 0 0 0.25 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.75 0.5 0.5 0.5 1 0.75 0.75 0.75 1 1 1 1 1 1 1 1";

    final String IPRange1 = "1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 0 0 0 0 1 1 0 0 0 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 1 0 1 1 0 0 0 0 0 0 0" +
            " 1 1 1 1 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 1 0 0 0 " +
            "1 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 " +
            "1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0" +
            " 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 " +
            "0 1 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1" +
            " 0 0 0 1 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 " +
            "0 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0.5 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1" +
            " 0 0 0 1 0 0 0.5 1 0.5 0.5 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0.5 0.5 1 0.5 0.75 0.75 0 0 0 0 0 0 0 0 0 0 0" +
            " 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0.5 0.5 0.5 1 0.75 0.75 0.75 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0" +
            " 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 1 1 1 1 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 " +
            "0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 1 0.5 0.5 0.5 0.5 0 0 0 " +
            "0.5 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0.5 0.5 0.5 1 1 1 1 1 1 1 1 0 0.5 0 0 0 0.5 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0" +
            " 0 1 0 0 0 1 0 0 0.5 1 0.5 0.5 1 1 1 1 1 1 1 1 0 0 0 0.5 0 0 0 0.5 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0.5 0.5 1 0.5 1 1 1 1 1 " +
            "1 1 1 0 0 0 0 0 0.5 0 0 0 0.5 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0.5 0.5 0.5 1 1 1 1 1 1 1 1 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 " +
            "0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0.75 0.75 0.75 1 0.75 0.75 0.75 1 0.75 0.75 0.75 0 0.25 0.25 0.25 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0" +
            " 0 1 0 0 0 1 0 0 0 1 0 0 0.75 1 0.75 0.75 0.75 1 0.75 0.75 0.75 1 0.75 0.75 0.25 0 0.25 0.25 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0" +
            " 0 1 0 0 0 1 0 0.75 0.75 1 0.75 0.75 0.75 1 0.75 0.75 0.75 1 0.75 0.25 0.25 0 0.25 0 0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0" +
            " 0 0 1 0.75 0.75 0.75 1 0.75 0.75 0.75 1 0.75 0.75 0.75 1 0.25 0.25 0.25 0 0 0 0";

    final String OOPRange2 = "0.9 0.9 0.9 0.9 0.9 0.9 0 0 0 0 0 0 0 0 1 0 0 0 0 1 1 0 0 0 0 1 1 1 0 0 0 0 0.8 0 0 0 0 0 0 0 0 0.8 0 0 1 0 0 0 0 0 0 0.8 0 1 1 0 0 0 0 0 0 0 " +
            "0.8 1 1 1 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 1 0 0 0 0 0 0 0.8 0 0 0 0.8 0 1 1 0 0 0 0 0 0 0 0.8 0 0 0 0.8 1 1 1 0 0 0 0 0.8 0 0 0 0.8 0 " +
            "0 0 0.8 0 0 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 0 0 1 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 0 1 1 0 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 1 1 1 0 0 0 0 0 0 0 0" +
            " 0.8 0 0 0 0.8 0 0 0 0.8 0 0 0 0 0 0 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 0 0 0.9 0 0 0 0 0 0 0 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 0 0.9 0.9 0 0 0 0 0 0 0 0 0 0 " +
            "0 0.8 0 0 0 0.8 0 0 0 0.8 0.9 0.9 0.9 0 0 0 0 0 0 0 0 0.9 0 0 0 0.8 0 0 0 0.8 0 0 0 0.8 1 1 1 0 0 0 0 0 0 0 0 0 0.9 0 0 0 0.8 0 0 0 0.8 0 0 1 0.8 1 1 0.5 0 0" +
            " 0 0 0 0 0 0 0 0 0.9 0 0 0 0.8 0 0 0 0.8 0 1 1 0.8 1 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 0.8 0 0 0 0.8 1 1 1 0.8 0.5 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0" +
            " 0 0 0.9 0 0 0 0.8 1 1 1 0.8 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 0.9 0 0 1 0.8 1 1 1 0.8 1 1 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 0.9 0 1 1 0.8 1 1 " +
            "1 0.8 1 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 0.9 1 1 1 0.8 1 1 1 0.8 0.5 0.5 0.5 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 1 0 0 0 0.9 0 0 0 0.8 1 1 1 0.8 1" +
            " 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 1 0 0 0 0.9 0 0 1 0.8 1 1 1 0.8 1 1 0.3 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 1 0 0 0 0.9 0 1 1 0.8 1 " +
            "1 1 0.8 1 0.3 0.3 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.9 0 0 0 1 0 0 0 0.9 1 1 1 0.8 1 1 1 0.8 0.3 0.3 0.3 0 0 0 0 0.9 0 0 0 0.9 0 0 0 1 0 0 0 1 0 0 " +
            "0 1 0 0 0 0.9 0 0 0 0.8 1 1 1 0.8 1 1 1 0 0 0 0 0 0.9 0 0 0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0.9 0 0 1 0.8 1 1 1 0.8 1 1 0.1 0 0 0 0 0 0 0.9 0 " +
            "0 0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0.9 0 1 1 0.8 1 1 1 0.8 1 0.1 0.1 0 0 0 0 0 0 0 0.9 0 0 0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0.9 1 1 1 0.8 1" +
            " 1 1 0.8 0.1 0.1 0.1 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 0.9 1 1 1 0.8 1 1 1 0.8 1 1 1 0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 " +
            "1 0 0 0 1 0 0 1 1 1 1 1 0.9 1 1 1 0.8 1 1 1 0.8 1 1 0.1 0 0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 0.9 1 1 1 0.8 1 1 1 0.8 1 0.1 0.1 0 0 " +
            "0 0.9 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 1 1 1 1 0.9 1 1 1 0.8 1 1 1 0.8 0.1 0.1 0.1 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 1 " +
            "1 1 1 0.8 1 1 1 0.5 1 1 1 0.5 0.8 0.8 0.8 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0.8 1 1 1 0.5 1 1 0.8 0.5 0.8 0.8 0.1 0 0 1" +
            " 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 1 1 1 1 0.8 1 1 1 0.5 1 0.8 0.8 0.5 0.8 0.1 0.1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1" +
            " 1 1 1 1 1 1 1 1 1 1 0.8 1 1 1 0.5 0.8 0.8 0.8 0.5 0.1 0.1 0.1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.8 1 1 1 0.5 0.8 0.8 0.8" +
            " 0.1 0.5 0.5 0.5 0.1 0.1 0.1 0.1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.8 1 1 0.8 0.5 0.8 0.8 0.5 0.1 0.5 0.5 0.1 0.1 0.1 " +
            "0.1 0.1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.8 1 0.8 0.8 0.5 0.8 0.5 0.5 0.1 0.5 0.1 0.1 0.1 0.1 0.1 0.1 1 1 1 1 1 1 1 " +
            "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.8 0.8 0.8 0.8 0.5 0.5 0.5 0.5 0.1 0.1 0.1 0.1 0.1 0.1 0.1 0.1";

    final String IPRange2 = "1 1 1 1 1 1 0 0 0 0 0 0 0 0 1 0 0 0 0 1 1 0 0 0 0 1 1 1 0 0 0 0 1 0 0 0 0 0 0 0 0 1 0 0 1 0 0 0 0 0 0 1 0 1 1 0 0 0 0 0 0 0 1 1 1 1 0 0 0 0 1 0 0 0 1 0 " +
            "0 0 0 0 0 0 0 1 0 0 0 1 0 0 1 0 0 0 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 1 0 0 " +
            "0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 1 0" +
            " 0 0 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0.5 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 0.3 0.3 0.3 0 0 0 0 0 0 0 " +
            "0 0 0.3 0 0 0 1 0 0 0 1 0 0 0.3 1 0.3 0.3 1 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0.3 0.3 1 0.3 1 1 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 0.3 0.3 0.3 1 1 " +
            "1 1 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 " +
            "0 0 0 1 0 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 " +
            "0 0 0 0 0 0 0 0 0 0 0" +
            " 0 0 0.3 0 0 0 1 0 0 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 0 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 1 1" +
            " 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0.3 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0.3 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 0 0 1 1 " +
            "1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0.3 0 0 0 0.3 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 " +
            "1 1 1 1 1 1 1 1 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 1 1 " +
            "1 1 1 1 1 1 1 1 1 1 1 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0.5 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 " +
            "0 0 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0.3 0.3 0.3 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 " +
            "0 1 0 0 0.3 1 0.3 0.3 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0.3 0.3 1 0.3 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
            "0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 1 0.3 0.3 0.3 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.5 0.5 0.5 1 0.5 0.5 0.5 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
            "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.5 1 0.5 0.5 0.5 1 0.5 0.5 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.5 " +
            "0.5 1 0.5 0.5 0.5 1 0.5 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0.5 0.5 0.5 1 0.5 0.5 0.5 1 1 1 1 1 1 1 1 1 1 1 1 1 " +
            "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
}
