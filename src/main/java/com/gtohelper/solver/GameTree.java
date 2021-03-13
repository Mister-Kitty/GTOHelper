package com.gtohelper.solver;


import java.util.ArrayList;
import java.util.function.Function;

import com.gtohelper.domain.Action;
import com.gtohelper.domain.BettingOptions;
import com.gtohelper.domain.BettingOptions.Bets;
import com.gtohelper.domain.BettingOptions.Raises;
import com.gtohelper.domain.BettingOptions.StreetAction;
import com.gtohelper.domain.BettingOptions.OOPStreetAction;
import com.gtohelper.domain.BettingOptions.IPStreetAction;
import com.gtohelper.domain.Street;

enum Actor {
    IP,
    OOP;

    public static Actor nextActor(Actor t) {
        if(t == null)
            return OOP;
        else if(t.equals(IP))
            return OOP;
        else
            return IP;
    }
}

public class GameTree {
    private Node root;

    public void buildGameTree(BettingOptions data, int pot, int effectiveStack) {
        root = new Node(data, pot, effectiveStack);
    }

    public ArrayList<String> getAllInLeaves(BettingOptions options) {
        ArrayList<String> results = root.getPrintOfAllInLeaves(options);
        return results;
    }
}

class Node {
    class NodeData {
        Action parentAction;
        int effectiveStack;
        int currentPot;
        int maxChipsInvestedByAPlayer;
        Street street;
        Actor curActor;
        int facingBet;
        public NodeData() {}
        public NodeData(NodeData d) { parentAction = d.parentAction; effectiveStack = d.effectiveStack; currentPot = d.currentPot;
                    maxChipsInvestedByAPlayer = d.maxChipsInvestedByAPlayer; street = d.street; curActor = d.curActor; facingBet = d.facingBet; }
    }

    NodeData nodeData;

    Node parent;
    Node foldNode;
    Node checkCallNode;
    ArrayList<Node> betRaiseNodes;

    //root node
    public Node(BettingOptions treeData, int pot, int effectiveStack) {
        nodeData = new NodeData();
        nodeData.currentPot = pot;
        nodeData.effectiveStack = effectiveStack;
        nodeData.street = Street.PRE;
        nodeData.curActor = null;
        nodeData.parentAction = Action.CHECK; // Helps the logic. Could maybe be null and add checks...

        // Notice that we start as Preflop on root node, so that our flop checks/donks have a parent street of Preflop.
        checkCallNode = generateCheckNode(treeData);
        if(nodeData.effectiveStack > 0)
            betRaiseNodes = generateBetRaiseNodes(treeData);
    }

    public Node(Node p, BettingOptions treeData, NodeData d) {
        parent = p;
        nodeData = d;

        if(nodeData.street == Street.SHOWDOWN) {
            return;
        }

        if(nodeData.parentAction == Action.BET || nodeData.parentAction == Action.RAISE) {
            foldNode = generateFoldNode(treeData);
            checkCallNode = generateCallNode(treeData);
        } else if (nodeData.parentAction == Action.CALL || nodeData.parentAction == Action.CHECK) {
            checkCallNode = generateCheckNode(treeData);
        }

        if(nodeData.effectiveStack > 0)
            betRaiseNodes = generateBetRaiseNodes(treeData);

    }

    public ArrayList<String> getPrintOfAllInLeaves(BettingOptions options) {
        boolean dontThreeBetRiver = options.IPRiver.getDont3BetPlus();

        ArrayList<String> results =
            getArrayListOfFunctionOnAllNodes(
                    (node) -> {
                        if(node.isShowdownNode() && node.nodeData.parentAction == Action.CALL && node.nodeData.effectiveStack == 0)
                            return node.parent.toString();
                        // the don't 3bet edge case, where we add_lines that are not all in.
                        // Since we don't generate a raise node from IP, we find the node where IP calls a 2-bet and add that.
                        // Node curActor == OOP because it alternates on nodes.
                        else if (dontThreeBetRiver && node.isShowdownNode() && node.nodeData.parentAction == Action.CALL &&
                                node.parent.nodeData.parentAction == Action.RAISE && node.nodeData.curActor == Actor.OOP && node.nodeData.effectiveStack > 0)
                            return node.parent.toString();
                        else
                            return "";
            });

        ArrayList<String> nonEmptyResults = new ArrayList<>();
        for(String s : results) {
            if (!s.isEmpty()) {
                nonEmptyResults.add(s);
            }
        }
        return nonEmptyResults;
    }

    public <T> ArrayList<T> getArrayListOfFunctionOnAllNodes(Function<Node, T> function) {
        ArrayList<T> results = new ArrayList<T>();

        if(foldNode != null) {
            results.add(function.apply(foldNode));
            results.addAll(foldNode.getArrayListOfFunctionOnAllNodes(function));
        }

        if(checkCallNode != null) {
            results.add(function.apply(checkCallNode));
            results.addAll(checkCallNode.getArrayListOfFunctionOnAllNodes(function));
        }

        if(betRaiseNodes != null) {
            for (Node n : betRaiseNodes) {
                results.add(function.apply(n));
                results.addAll(n.getArrayListOfFunctionOnAllNodes(function));
            }
        }

        return results;
    }

    public boolean isShowdownNode() {
        return nodeData.street == Street.SHOWDOWN;
    }

    @Override
    public String toString() {
        if(parent == null)
            return "";

        if(nodeData.parentAction == Action.FOLD)
            return parent.toString() + " 0";

        String parentString = parent.toString();
        if(parentString.isEmpty())
            return "" + nodeData.maxChipsInvestedByAPlayer;
        else
            return parentString + " " + nodeData.maxChipsInvestedByAPlayer;
    }

    /*

        Below is code to generate the tree

     */

    private Node generateFoldNode(BettingOptions treeData) {
        return new Node(this, treeData, getNextFoldNodeData(this, treeData));
    }

    private Node generateCheckNode(BettingOptions treeData) {
         return new Node(this, treeData, getNextCheckNodeData(this, treeData));
    }

    private Node generateCallNode(BettingOptions treeData) {
        return new Node(this, treeData, getNextCallNodeData(this, treeData));
    }

    private Node generateBetNode(BettingOptions treeData, int betSize) {
        return new Node(this, treeData, getNextBetNodeData(this, treeData, betSize));
    }

    private ArrayList<Node> generateBetRaiseNodes(BettingOptions treeData) {
        ArrayList<Node> betRaiseNodes = new ArrayList<>();
        StreetAction actions = getStreetActions(treeData, nodeData.street, nodeData.curActor);

        // Are we generating sizes based on donk, bet, or raise values?
        boolean firstNode = (nodeData.curActor == null);
        boolean facingCheckOrCall = (nodeData.parentAction == Action.CALL || nodeData.parentAction == Action.CHECK);
        boolean OOPandWeCalledBetLastStreet = (nodeData.curActor == Actor.OOP && parent.nodeData.curActor == Actor.OOP && nodeData.parentAction == Action.CALL);
        if(firstNode || OOPandWeCalledBetLastStreet) {
            // We're donking
            Bets donkBets = ((OOPStreetAction) actions).getDonks();
            for(Integer betSize : donkBets.getSizeOfAllBets(nodeData.currentPot, nodeData.effectiveStack, 0, actions.getAddAllIn())) {
                Node donkNode = new Node(this, treeData, getNextBetNodeData(this, treeData, betSize));
                betRaiseNodes.add(donkNode);
            }

        } else if (facingCheckOrCall) {
            // We're betting
            Bets bets = actions.getBets();
            for(Integer betSize : bets.getSizeOfAllBets(nodeData.currentPot, nodeData.effectiveStack, 0, actions.getAddAllIn())) {
                Node betNode = new Node(this, treeData, getNextBetNodeData(this, treeData, betSize));
                betRaiseNodes.add(betNode);
            }

        } else {
            // We must be raising.
            // But first, let's check the "don't 3-bet" IP option triggers.
            if(nodeData.curActor == Actor.IP) {
                boolean dontThreeBet = ((IPStreetAction) actions).getDont3BetPlus();
                boolean facingTwoBet = nodeData.parentAction == Action.RAISE; // This is, technically, facing 2 or more bet. But the logic works.
                if(dontThreeBet && facingTwoBet)
                    return betRaiseNodes;
            }

            Raises bets = actions.getRaises();

            // It's easier to break down a raise as a call + bet.
            int currentPotAfterCall = nodeData.currentPot + nodeData.facingBet;

            for(Integer raiseSize : bets.getSizeOfRaisesOntop(currentPotAfterCall, nodeData.effectiveStack, nodeData.facingBet, actions.getAddAllIn())) {
                Node raiseNode = new Node(this, treeData, getNextRaiseNodeData(this, treeData, raiseSize));
                betRaiseNodes.add(raiseNode);
            }

        }

        return betRaiseNodes;
    }

    // In the next few functions it is very very important to remember that we use the copy constructor for ease,
    // but we are making these fields to be successor fields for a set parent node's actions.
    private NodeData getNextFoldNodeData(Node parent, BettingOptions treeData) {
        NodeData newNode = new NodeData(parent.nodeData);
        newNode.parentAction = Action.FOLD;
        newNode.facingBet = parent.nodeData.facingBet;
        newNode.curActor = Actor.nextActor(newNode.curActor);
        newNode.street = Street.SHOWDOWN;

        return newNode;
    }

    private NodeData getNextCheckNodeData(Node parent, BettingOptions treeData) {
        NodeData newNode = new NodeData(parent.nodeData);
        newNode.parentAction = Action.CHECK;

        // If our first action ever is a check, our parent is root and has null currentActor.
        if(newNode.curActor == null) {
            newNode.curActor = Actor.IP;
            newNode.street = Street.FLOP;
        } else {
            newNode.curActor = Actor.nextActor(newNode.curActor);
        }

        // if our parent was checking IP, we move streets.
        if(parent.nodeData.curActor == Actor.IP) {
            newNode.street = Street.nextStreet(newNode.street);
        }

        return newNode;
    }

    private NodeData getNextCallNodeData(Node parent, BettingOptions treeData) {
        int bet = parent.nodeData.facingBet;

        NodeData newNode = new NodeData(parent.nodeData);
        newNode.parentAction = Action.CALL;
        newNode.currentPot += bet;
        if(newNode.effectiveStack > 0)
            newNode.street = Street.nextStreet(newNode.street);
        else
            newNode.street = Street.SHOWDOWN; // we're calling an all in.

        // Bit of an edge case here. Calls normally reset next actor to OOP as the street changes.
        // But if it's showdown, we want to alternate the actor for convenience of 'dont 3bet' logic above
        if(newNode.street == Street.SHOWDOWN)
            newNode.curActor = Actor.nextActor(newNode.curActor);
        else
            newNode.curActor = Actor.OOP;
        newNode.facingBet = 0;

        return newNode;
    }

    private NodeData getNextBetNodeData(Node parent, BettingOptions treeData, int betsize) {
        NodeData newNode = new NodeData(parent.nodeData);
        newNode.parentAction = Action.BET;
        newNode.maxChipsInvestedByAPlayer += betsize;
        newNode.effectiveStack -= betsize;
        newNode.currentPot += betsize;
        newNode.facingBet = betsize;

        // If our first action ever is a bet, our parent is root and has null currentActor.
        if(newNode.curActor == null) {
            newNode.curActor = Actor.IP;
            newNode.street = Street.FLOP;
        } else {
            newNode.curActor = Actor.nextActor(newNode.curActor);
        }

        return newNode;
    }

    private NodeData getNextRaiseNodeData(Node parent, BettingOptions treeData, int raiseATotalOf) {
        // We need 'how much more on top' to adjust node stats properly.
        // So if we 3x from 50 to 150, the effective stack only drops by 100.
        int currentRaiseAfterCall = raiseATotalOf - nodeData.facingBet;

        NodeData newNode = new NodeData(parent.nodeData);
        newNode.parentAction = Action.RAISE;
        newNode.currentPot += raiseATotalOf;
        newNode.maxChipsInvestedByAPlayer += currentRaiseAfterCall;
        newNode.effectiveStack -= currentRaiseAfterCall;
        newNode.curActor = Actor.nextActor(newNode.curActor);
        newNode.facingBet = currentRaiseAfterCall;

        return newNode;
    }

    public static StreetAction getStreetActions(BettingOptions data, Street currentStreet, Actor act) {
        if(currentStreet == Street.PRE) {
            return data.OOPFlop;
        } else if(currentStreet.equals(Street.FLOP)) {
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
}
