package com.gtohelper.domain;

import java.util.ArrayList;
import java.util.List;

public class Work {

    List<SolveData> workList;
    // TODO: Progress, etc.

    public Work() {
        workList = new ArrayList<SolveData>();
    }

    public Work(List<SolveData> w) {
        workList = w;
    }

    public List<SolveData> getWorkList() {
        return workList;
    }


}
