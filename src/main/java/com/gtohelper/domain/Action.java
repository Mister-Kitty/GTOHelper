package com.gtohelper.domain;

import java.io.Serializable;

public enum Action implements Serializable {
    FOLD,
    CHECK,
    CALL,
    BET,
    RAISE;

    private static final long serialVersionUID = 1L;
}
