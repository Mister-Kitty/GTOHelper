package com.gtohelper.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class SessionBundle {

    private ArrayList<Session> sessions = new ArrayList<>();
    private LocalDateTime minSessionStartTime, maxSessionEndTime;

    public Duration getDuration() {
        if(maxSessionEndTime != null && minSessionStartTime != null)
            return Duration.between(minSessionStartTime, maxSessionEndTime);
        else
            return Duration.ZERO;
    }

    public int getHandCount() {
        return sessions.stream().mapToInt(s -> s.cnt_hands).sum();
    }

    public int getFlopsCount() {
        return sessions.stream().mapToInt(s -> s.cnt_hands_flopped).sum();
    }

    public double getAmountWon() {
        return sessions.stream().mapToDouble(s -> s.amt_won).sum();
    }

    @Override
    public String toString() {
        return String.format("%d sessions, %s length", sessions.size(), getDuration().toString());
    }

    public void addSession(Session s) {
        if(minSessionStartTime == null || s.session_date_start.isBefore(minSessionStartTime))
            minSessionStartTime = s.session_date_start;

        if(maxSessionEndTime == null || s.session_date_end.isAfter(maxSessionEndTime))
            maxSessionEndTime = s.session_date_end;

        sessions.add(s);
    }
    
    public LocalDateTime getMinSessionStartTime() {
        if(sessions.size() == 0) {
            assert false;
            return null;
        } else {
            return minSessionStartTime;
        }
    }

    public LocalDateTime getMaxSessionEndTime() {
        if(sessions.size() == 0) {
            assert false;
            return null;
        } else {
            return maxSessionEndTime;
        }
    }

    public ArrayList<Session> getSessions() { return sessions; }

    public boolean isEmpty() { return sessions.isEmpty(); }

    public SessionBundle() { }




}
