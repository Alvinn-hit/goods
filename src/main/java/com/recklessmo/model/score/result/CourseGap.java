package com.recklessmo.model.score.result;


import javafx.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hpf on 1/8/17.
 */
public class CourseGap {

    //名字
    private String name;

    private List<ScoreGap> gapList = new LinkedList<>();
    private List<GapInner> gapInnerList = new LinkedList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ScoreGap> getGapList() {
        return gapList;
    }

    public void setGapList(List<ScoreGap> gapList) {
        this.gapList = gapList;
    }

    public List<GapInner> getGapInnerList() {
        return gapInnerList;
    }

    public void setGapInnerList(List<GapInner> gapInnerList) {
        this.gapInnerList = gapInnerList;
    }
}
