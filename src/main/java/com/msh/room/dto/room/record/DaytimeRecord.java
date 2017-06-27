package com.msh.room.dto.room.record;

import com.msh.room.exception.RoomBusinessException;
import io.swagger.models.auth.In;

import java.util.*;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class DaytimeRecord {
    private Map<Integer, List<Integer>> votingRecord;
    //被投票死的
    private Integer diedNumber;
    //白天有猎人,猎人带走的人
    private Integer hunterShoot;
    //自爆狼人
    private Integer wolfExplode;
    private List<Map<Integer, List<Integer>>> pkVotingRecord;

    public DaytimeRecord() {
        votingRecord = new LinkedHashMap<>();
        pkVotingRecord = new ArrayList<>();
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

    public Integer getHunterShoot() {
        return hunterShoot;
    }

    public void setHunterShoot(Integer hunterShoot) {
        this.hunterShoot = hunterShoot;
    }

    public Integer getWolfExplode() {
        return wolfExplode;
    }

    public void setWolfExplode(Integer wolfExplode) {
        this.wolfExplode = wolfExplode;
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

    public Map<Integer, List<Integer>> lastPKRecord() {
        if (pkVotingRecord == null) {
            return null;
        }
        int size = pkVotingRecord.size();
        return pkVotingRecord.get(size - 1);
    }

    public void addPKVote(Integer seatNumber, Integer voteNumber) {
        if (voteNumber == 0) {
            throw new RoomBusinessException("PK环节不能弃票");
        }
        List<Integer> voteRecord = lastPKRecord().get(voteNumber);
        if (voteRecord == null) {
            throw new RoomBusinessException("该玩家非PK玩家，无法投票");
        }
        voteRecord.add(seatNumber);
    }

    public boolean isPKVoteComplete(int aliveNumber) {
        int count = 0;
        for (Integer key : lastPKRecord().keySet()) {
            count += lastPKRecord().get(key).size();
        }
        if (count == aliveNumber) {
            return true;
        }
        return false;
    }

    public boolean isPKVoted(int seatNumber) {
        long count = lastPKRecord().keySet().parallelStream()
                .filter(key -> lastPKRecord().get(key).contains(seatNumber)).count();
        return count != 0;
    }

    public List<Integer> resolvePKVoteResult(int sheriffNumber) {
        return calculateVoteResult(lastPKRecord(), sheriffNumber);
    }

    public List<Integer> calculateVoteResult(Map<Integer, List<Integer>> voteRecord, int sheriffNumber) {
        List<Integer> result = new ArrayList<>();
        int biggestNumber = 0;
        for (Integer key : voteRecord.keySet()) {
            //统一乘以10，警长加5.
            int number = voteRecord.get(key).size() * 10;
            if (voteRecord.get(key).contains(sheriffNumber)) {
                number += 5;
            }

            if (number > biggestNumber && key != 0) {
                biggestNumber = number;
                result.clear();
                result.add(key);
            } else if (number == biggestNumber && key != 0) {
                result.add(key);
            }
        }
        return result;
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

    public List<Integer> resolveVoteResult(int sheriffNumber) {
        return calculateVoteResult(votingRecord, sheriffNumber);
    }
}
