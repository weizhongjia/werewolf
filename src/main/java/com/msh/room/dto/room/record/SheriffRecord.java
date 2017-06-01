package com.msh.room.dto.room.record;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/30.
 */
public class SheriffRecord {
    //
    private int sheriffRuningTime = 0;
    private List<Integer> sheriffRegisterList;
    private Map<Integer, List<Integer>> votingRecord;
    private List<Map<Integer, List<Integer>>> pkVotingRecord;
    private Integer sheriff;

    public SheriffRecord() {
        sheriffRegisterList = new ArrayList<>();
        votingRecord = new LinkedHashMap<>();
        pkVotingRecord = new ArrayList<>();
    }

    public List<Integer> getSheriffRegisterList() {
        return sheriffRegisterList;
    }

    public void setSheriffRegisterList(List<Integer> sheriffRegisterList) {
        this.sheriffRegisterList = sheriffRegisterList;
    }

    public Map<Integer, List<Integer>> getVotingRecord() {
        return votingRecord;
    }

    public void setVotingRecord(Map<Integer, List<Integer>> votingRecord) {
        this.votingRecord = votingRecord;
    }

    public List<Map<Integer, List<Integer>>> getPkVotingRecord() {
        return pkVotingRecord;
    }

    public void setPkVotingRecord(List<Map<Integer, List<Integer>>> pkVotingRecord) {
        this.pkVotingRecord = pkVotingRecord;
    }

    public Integer getSheriff() {
        return sheriff;
    }

    public void setSheriff(Integer sheriff) {
        this.sheriff = sheriff;
    }

    public void registSheriff(int seatNumber) {
        sheriffRegisterList.add(seatNumber);
        votingRecord.put(seatNumber, new ArrayList<>());
    }

}
