package com.gtohelper.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public class Session {
    public int id_session;
    public LocalDateTime session_date_start;
    public int cnt_hands;
    public int cnt_hands_flopped;
    public float amt_won_curr_conv;
    public float amt_won;
    public LocalDateTime session_date_end;

    public Duration duration;

}
