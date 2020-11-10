package com.gtohelper.solver;

import java.io.IOException;

public interface ISolver {
    public void connectAndInit(String pioLocation) throws IOException;
    public void setRange(String position, String range) throws IOException;
    public void disconnect();
}
