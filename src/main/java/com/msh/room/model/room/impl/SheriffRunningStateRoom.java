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

import java.util.List;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffRunningStateRoom extends AbstractStateRoom {
    public SheriffRunningStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_VOTEING:
                resolveSheriffVoting(event);
                break;
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    private void resolveSheriffVoting(JudgeEvent event) {
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        Map<Integer, List<Integer>> votingRecord = sheriffRecord.getVotingRecord();
        //可投票人数
        int voteCount = roomState.getAliveCount() - sheriffRecord.getSheriffRegisterList().size();
        //无人可选或无人可投票
        if (votingRecord.isEmpty() || voteCount == 0) {
            //直接流票
            sheriffRecord.setSheriff(0);
            resolveDaytimeComing(event);
        } else {
            if (votingRecord.size() == 1) {
                //仅有一人,直接设置警长。进入白天
                votingRecord.keySet().forEach(seatNumber -> sheriffRecord.setSheriff(seatNumber));
                resolveDaytimeComing(event);
            } else {
                roomState.setStatus(RoomStatus.SHERIFF_VOTING);
            }
        }
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        //可以触发上警投票
        displayInfo.addAcceptableEventType(JudgeEventType.SHERIFF_VOTEING);
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_UNREGISTER:
                playerUnRegister(event);
                break;
        }
        return roomState;
    }

    private void playerUnRegister(PlayerEvent event) {
        int seatNumber = event.getSeatNumber();
        if (roomState.getSheriffRecord().getVotingRecord().keySet().contains(seatNumber)) {
            roomState.getSheriffRecord().unRegisterSheriff(seatNumber);
        }
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        //竞选阶段需要看竞选状态
        displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        if (roomState.getSheriffRecord().getVotingRecord().keySet().contains(seatNumber)) {
            displayInfo.addAcceptableEventType(PlayerEventType.SHERIFF_UNREGISTER);
        }
        return displayInfo;
    }
}
