package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.SheriffRecord;
import com.msh.room.exception.RoomBusinessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffPkVotingStateRoom extends AbstractStateRoom {
    public SheriffPkVotingStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_PK_VOTE:
                playerPKVote(event);
                break;
        }
        return roomState;
    }

    private void playerPKVote(PlayerEvent event) {
        Integer voteNumber = event.getSheriffPKVoteNumber();
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        if (sheriffRecord.isPKVoted(event.getSeatNumber())) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        Map<Integer, List<Integer>> lastPKRecord = sheriffRecord.lastPKVotingRecord();
        sheriffRecord.addPKVote(event.getSeatNumber(), voteNumber);
        if (sheriffRecord.isPKVoteComplete(roomState.getEnableVoteCount() - lastPKRecord.size())) {
            pkVoteResult();
        }
    }

    private void pkVoteResult() {
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        List<Integer> voteResult = sheriffRecord.resolvePKVoteResult();
        //如果有平票
        if (voteResult.size() > 1) {
            //没到两轮
            if (sheriffRecord.getPkVotingRecord().size() < 2) {
                sheriffRecord.newPK();
                for (Integer number : voteResult) {
                    sheriffRecord.addPkNumber(number);
                }
                roomState.setStatus(RoomStatus.SHERIFF_PK);
            } else {
                //两轮pk平票 警徽流失
                sheriffRecord.setSheriff(0);
                //天亮
                resolveDaytimeComing(null);
            }
        } else {
            //没可能所有人弃票
            if (voteResult.size() == 0) {
                throw new RoomBusinessException("投票状态错误，pk阶段无法弃票");
            }
            Integer number = voteResult.get(0);
            sheriffRecord.setSheriff(number);
            //天亮
            resolveDaytimeComing(null);
        }
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        if (!roomState.getSheriffRecord().lastPKVotingRecord().keySet().contains(seatNumber)) {
            //还没投票且还活着
            if (!roomState.getSheriffRecord().isPKVoted(seatNumber) &&
                    roomState.getPlaySeatInfoBySeatNumber(seatNumber).isAlive()) {
                //隐藏投票信息
                displayInfo.setSheriffRecord(copyNewSheriffRecord(displayInfo.getSheriffRecord()));
                displayInfo.addAcceptableEventType(PlayerEventType.SHERIFF_PK_VOTE);
            }
        }
        return displayInfo;
    }

    private SheriffRecord copyNewSheriffRecord(SheriffRecord oldSheriffRecord) {
        SheriffRecord newSheriffRecord = new SheriffRecord();
        newSheriffRecord.setSheriffRegisterList(oldSheriffRecord.getSheriffRegisterList());
        newSheriffRecord.setVotingRecord(oldSheriffRecord.getVotingRecord());

        Map<Integer, List<Integer>> newPKRecord = new HashMap<>();
        for (Integer key : oldSheriffRecord.lastPKVotingRecord().keySet()) {
            newPKRecord.put(key, null);
        }
        List<Map<Integer, List<Integer>>> newPKVotingRecord = new ArrayList<>();
        newPKVotingRecord.add(newPKRecord);
        newSheriffRecord.setPkVotingRecord(newPKVotingRecord);
        return newSheriffRecord;
    }
}
