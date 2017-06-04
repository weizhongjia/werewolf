package com.msh.room.dto.room.record;

import com.msh.room.exception.RoomBusinessException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/30.
 */
public class SheriffRecord {
    //共竞选几轮（狼人自爆第几轮）
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

    public void registerSheriff(int seatNumber) {
        if (!sheriffRegisterList.contains(seatNumber)) {
            sheriffRegisterList.add(seatNumber);
            votingRecord.put(seatNumber, new ArrayList<>());
        }
    }

    //退选，仅是投票环节不参与，曾经上警记录需要保留
    public void unRegisterSheriff(int seatNumber) {
        if (sheriffRegisterList.contains(seatNumber)) {
            votingRecord.remove(seatNumber);
        }
    }

    public boolean isVoted(int seatNumber) {
        for (Integer key : votingRecord.keySet()) {
            if (votingRecord.get(key).contains(seatNumber)) {
                return true;
            }
        }
        return false;
    }

    public void addVote(int seatNumber, Integer voteNumber) {
        if (voteNumber == 0 && !votingRecord.containsKey(0)) {
            //弃票
            votingRecord.put(0, new ArrayList<>());
            votingRecord.get(0).add(seatNumber);
            return;
        }
        if (!votingRecord.containsKey(voteNumber)) {
            throw new RoomBusinessException("该玩家不在竞选列表，无法投票");
        }
        if (votingRecord.containsKey(seatNumber)) {
            throw new RoomBusinessException("您在竞选列表，无法投票");
        }
        votingRecord.get(voteNumber).add(seatNumber);
    }

    public boolean isVoteComplete(int voteCount) {
        int count = 0;
        for (Integer key : votingRecord.keySet()) {
            count += votingRecord.get(key).size();
        }
        if (count == voteCount) {
            return true;
        }
        return false;
    }

    public List<Integer> resolveVoteResult() {
        return calculateVoteResult(this.votingRecord);
    }

    protected List<Integer> calculateVoteResult(Map<Integer, List<Integer>> votingRecord) {
        List<Integer> result = new ArrayList<>();
        int biggestNumber = 0;
        for (Integer key : votingRecord.keySet()) {
            int number = votingRecord.get(key).size();
            if (number > biggestNumber && key != 0) {
                biggestNumber = number;
                result = new ArrayList<>();
                result.add(key);
            } else if (number == biggestNumber && key != 0) {
                result.add(key);
            }
        }
        return result;
    }

    public void newPK() {
        if (pkVotingRecord.size() < 2) {
            pkVotingRecord.add(new LinkedHashMap<>());
        } else {
            throw new RoomBusinessException("已无法再添加PK轮次");
        }
    }

    public void addPkNumber(Integer number) {
        if (lastPKVotingRecord() != null) {
            lastPKVotingRecord().put(number, new ArrayList<>());
        }
    }

    public Map<Integer, List<Integer>> lastPKVotingRecord() {
        int size = pkVotingRecord.size();
        if (size > 0) {
            return pkVotingRecord.get(size - 1);
        } else {
            return null;
        }
    }

    public boolean isPKVoted(int seatNumber) {
        for (Integer key : lastPKVotingRecord().keySet()) {
            if (lastPKVotingRecord().get(key).contains(seatNumber)) {
                return true;
            }
        }
        return false;
    }

    public void addPKVote(int seatNumber, Integer voteNumber) {
        if (voteNumber == 0) {
            throw new RoomBusinessException("PK阶段不能弃票");
        }
        if (!lastPKVotingRecord().containsKey(voteNumber)) {
            throw new RoomBusinessException("该玩家不在竞选列表，无法投票");
        }
        if (lastPKVotingRecord().containsKey(seatNumber)) {
            throw new RoomBusinessException("您在竞选列表，无法投票");
        }
        lastPKVotingRecord().get(voteNumber).add(seatNumber);
    }

    public boolean isPKVoteComplete(int voteCount) {
        int count = 0;
        for (Integer key : lastPKVotingRecord().keySet()) {
            count += lastPKVotingRecord().get(key).size();
        }
        if (count == voteCount) {
            return true;
        }
        return false;
    }

    public List<Integer> resolvePKVoteResult() {
        return calculateVoteResult(lastPKVotingRecord());
    }
}
