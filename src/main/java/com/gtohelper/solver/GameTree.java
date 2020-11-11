package com.gtohelper.solver;


import java.util.ArrayList;
import com.gtohelper.solver.GameTreeData.Bets;
import com.gtohelper.solver.GameTreeData.Street;
import com.gtohelper.solver.GameTreeData.StreetAction;
import com.gtohelper.solver.GameTreeData.OOPStreetAction;

public class GameTree {
    private Node root;

    public void buildGameTree(GameTreeData data) {
        root = new Node(data);






    }






}

class Node {
    private enum Actor {
        IP,
        OOP;

        public static Actor nextActor(Actor t) {
            if(t.equals(IP))
                return OOP;
            else
                return IP;
        }
    }

    private enum Action {
        FOLD,
        CHECK_CALL,
        BET,
        RAISE
    }

    final int startingPot;
    int currentPot;
    int effectiveStack;
    GameTreeData.Street street;
    Actor curActor;
    Action lastAction;
    int facingBet;
    Node parent;

    Node foldNode;
    Node checkCallNode;
    ArrayList<Node> betNodes = new ArrayList<Node>();

    //root node
    public Node(GameTreeData data) {
        startingPot = data.pot;
        currentPot = data.pot;
        effectiveStack = data.effectiveStack;
        street = GameTreeData.Street.FLOP;
        curActor = Actor.OOP;
        lastAction = Action.CHECK_CALL;

        // TODO: Add flag to disable folding as a child

        // TODO: test check/call possibility
        checkCallNode = new Node(this, data, currentPot, currentPot, effectiveStack, Actor.nextActor(curActor), getStreetAfterChecking(street, curActor, Action.CHECK_CALL), Action.CHECK_CALL, 0);

        generateBetNodes(data, street, lastAction, curActor, currentPot, effectiveStack);
    }

    public Node(Node p, GameTreeData data, int startPot, int curPot, int effStack, Actor t, Street s, Action l, int facing) {
        parent = p;
        startingPot = startPot;
        currentPot = curPot;
        effectiveStack = effStack;
        curActor = t;
        street = s;
        lastAction = l;
        facingBet = facing;

        if(s != Street.SHOWDOWN) {
            if(lastAction.equals(Action.BET) || lastAction.equals(Action.RAISE)) {
                foldNode = new Node(this, data, startingPot, currentPot, effectiveStack, curActor, Street.SHOWDOWN, Action.FOLD, facing);
            }
            checkCallNode = new Node(this, data, startingPot,currentPot + (2 * facingBet), effectiveStack, Actor.nextActor(curActor), getStreetAfterChecking(street, curActor, l), Action.CHECK_CALL, 0);
            generateBetNodes(data, street, lastAction, curActor, currentPot, effectiveStack);
        }
        else {
            System.out.println(this);
        }
    }

    private void generateBetNodes(GameTreeData data, Street currentStreet, Action lastAction, Actor curActor, int currentPot, int effectiveStack) {
        if(effectiveStack == 0)
            return;

        StreetAction actions = getStreetActions(data, currentStreet, curActor);

        // Are we generating bets based on donk, bet, or raise values?
        if(lastAction.equals(Action.CHECK_CALL) && curActor.equals(Actor.OOP)) {
            // We're donking
            Bets donkBets = ((OOPStreetAction) actions).getDonks();
            for(Integer betSize : donkBets.getSizeOfAllBets(currentPot, effectiveStack, actions.canAllIn)) {
                Node donkNode = new Node(this, data, startingPot, currentPot, effectiveStack - betSize,
                        Actor.nextActor(curActor), currentStreet, Action.BET, betSize);
                betNodes.add(donkNode);
            }

        } else if (lastAction.equals(Action.CHECK_CALL)) {
            // We're betting
            Bets bets = actions.getBets();
            for(Integer betSize : bets.getSizeOfAllBets(currentPot, effectiveStack, actions.canAllIn)) {
                Node betNode = new Node(this, data, startingPot,currentPot, effectiveStack - betSize,
                        Actor.nextActor(curActor), currentStreet, Action.BET, betSize);
                betNodes.add(betNode);
            }

        } else {
            // we must be raising.


        }


    }

    private static Street getStreetAfterChecking(Street curStreet, Actor act, Action lastAction) {
        if(curStreet.equals(Street.FLOP)) {
            if(act.equals(Actor.IP))
                return Street.TURN;
            else
                return Street.FLOP;
        } else if(curStreet.equals(Street.TURN)) {
            if (act.equals(Actor.IP))
                return Street.RIVER;
            else
                return Street.TURN;
        } else {
            if (act.equals(Actor.IP)) {
                return Street.SHOWDOWN;
            }
            else {
                if(lastAction.equals(Action.CHECK_CALL))
                    return Street.RIVER;
                else
                    return Street.SHOWDOWN;
            }
        }
    }

    public static StreetAction getStreetActions(GameTreeData data, Street currentStreet, Actor act) {
        if(currentStreet.equals(Street.FLOP)) {
            if(act.equals(Actor.IP))
                return data.IPFlop;
            else
                return data.OOPFlop;
        } else if(currentStreet.equals(Street.TURN)) {
            if(act.equals(Actor.IP))
                return data.IPTurn;
            else
                return data.OOPTurn;
        } else {
            if(act.equals(Actor.IP))
                return data.IPRiver;
            else
                return data.OOPRiver;
        }
    }


    @Override
    public String toString() {
        if(parent == null)
            return "root";

        if(lastAction.equals(Action.FOLD))
            return parent.toString() + " 0";

        // our toString is known as the actions taken to get to this node.
        // as such, the root's children don't want "root" in their toString.
        String parentString = parent.toString();
        if(parentString == "root")
            return (currentPot + facingBet - startingPot) + " ";
        else
            return parentString + (currentPot + facingBet - startingPot) + " ";
    }
}
