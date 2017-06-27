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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffVotingStateRoom extends AbstractStateRoom {
    public SheriffVotingStateRoom(RoomStateData data) {
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
        displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_VOTE:
                playerVote(event);
                break;
        }
        return roomState;
    }

    private void playerVote(PlayerEvent event) {
        Integer voteNumber = event.getSheriffVoteNumber();
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        if (sheriffRecord.isVoted(event.getSeatNumber())) {
            throw new RoomBusinessException("您已经投票，请勿重复投票");
        }
        sheriffRecord.addVote(event.getSeatNumber(), voteNumber);
        //需投票人数 = 存活人数 - 上警人数
        int voteCount = roomState.getAliveCount() - sheriffRecord.getSheriffRegisterList().size();
        //投票结束
        if (sheriffRecord.isVoteComplete(voteCount)) {
            sheriffVoteResult();
        }
    }

    private void sheriffVoteResult() {
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        List<Integer> voteResult = sheriffRecord.resolveVoteResult();
        //如果有平票,进入PK阶段
        if (voteResult.size() > 1) {
            sheriffRecord.newPK();
            for (Integer number : voteResult) {
                sheriffRecord.addPkNumber(number);
                roomState.setStatus(RoomStatus.SHERIFF_PK);
            }
        } else {
            Integer number = 0;
            //否则代表所有人弃票，警徽流失=0
            if (voteResult.size() > 0) {
                number = voteResult.get(0);
            }
            sheriffRecord.setSheriff(number);
            resolveDaytimeComing(null);
        }
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        if (!roomState.getSheriffRecord().getSheriffRegisterList().contains(seatNumber)) {
            if (!roomState.getSheriffRecord().isVoted(seatNumber)
                    && !roomState.getPlaySeatInfoBySeatNumber(seatNumber).isAlive()) {
                //隐藏投票信息
                displayInfo.setSheriffRecord(copyNewSheriffRecord(displayInfo.getSheriffRecord()));
                displayInfo.addAcceptableEventType(PlayerEventType.SHERIFF_VOTE);
            }
        }
        return displayInfo;
    }

    protected SheriffRecord copyNewSheriffRecord(SheriffRecord oldSheriffRecord) {
        SheriffRecord newSheriffRecord = new SheriffRecord();
        newSheriffRecord.setSheriffRegisterList(oldSheriffRecord.getSheriffRegisterList());

        Map<Integer, List<Integer>> newVotingRecord = new LinkedHashMap<>();
        for (Integer key : oldSheriffRecord.getVotingRecord().keySet()) {
            if (key != 0) {
                newVotingRecord.put(key, null);
            }
        }

        newSheriffRecord.setVotingRecord(newVotingRecord);
        return newSheriffRecord;
    }
}
