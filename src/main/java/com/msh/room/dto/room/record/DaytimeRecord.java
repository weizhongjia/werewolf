package com.msh.room.dto.room.record;

import java.util.*;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class DaytimeRecord {
    private Map<Integer, List<Integer>> votingRecord;
    private Integer diedNumber;
    private List<Map<Integer, List<Integer>>> pkVotingRecord;

    public DaytimeRecord() {
        votingRecord = new LinkedHashMap<>();
    }

    public Map<Integer, List<Integer>> getVotingRecord() {
        return votingRecord;
    }

    public void setVotingRecord(Map<Integer, List<Integer>> votingRecord) {
        this.votingRecord = votingRecord;
    }

    public void addVote(Integer seatNumber, Integer voteNumber) {
        List<Integer> voteRecord = votingRecord.get(voteNumber);
        if (voteRecord == null) {
            voteRecord = new ArrayList<>();
            votingRecord.put(voteNumber, voteRecord);
        }
        voteRecord.add(seatNumber);
    }

    public Integer getDiedNumber() {
        return diedNumber;
    }

    public void setDiedNumber(Integer diedNumber) {
        this.diedNumber = diedNumber;
    }

    public List<Map<Integer, List<Integer>>> getPkVotingRecord() {
        return pkVotingRecord;
    }

    public void setPkVotingRecord(List<Map<Integer, List<Integer>>> pkVotingRecord) {
        this.pkVotingRecord = pkVotingRecord;
    }

    public void addNewPk() {
        if (pkVotingRecord == null) {
            pkVotingRecord = new ArrayList<>();
        }
        pkVotingRecord.add(new HashMap<>());
    }

    public void addPkNumber(int number) {
        int size = pkVotingRecord.size();
        Map<Integer, List<Integer>> voteMap = pkVotingRecord.get(size - 1);
        voteMap.put(number, new ArrayList<>());
    }


    public boolean isDaytimeVoteComplete(int aliveNumber) {
        int count = 0;
        for (Integer key : votingRecord.keySet()) {
            count += votingRecord.get(key).size();
        }
        if (count == aliveNumber) {
            return true;
        }
        return false;
    }

    public boolean isDaytimeVoted(int seatNumber) {
        long count = votingRecord.keySet().parallelStream()
                .filter(key -> votingRecord.get(key).contains(seatNumber)).count();
        return count != 0;
    }

    public List<Integer> getVoteResult() {
        List<Integer> result = new ArrayList<>();
        int biggestNumber = 0;
        for (Integer key : votingRecord.keySet()) {
            int number = votingRecord.get(key).size();
            if (number >= biggestNumber && key != 0) {
                biggestNumber = number;
                result.add(key);
            }
        }
        return result;
    }
}
