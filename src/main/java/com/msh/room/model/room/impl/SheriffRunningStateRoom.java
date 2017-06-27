package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.SheriffRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.impl.Werewolves;

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
            case WEREWOLVES_EXPLODE:
                resolveWereWolfExplode(event);
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

    private void resolveWereWolfExplode(JudgeEvent event) {
        Integer seatNumber = event.getExplodeWereWolf();
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seatNumber);
        if (seatInfo.isAlive() && Roles.WEREWOLVES.equals(seatInfo.getRole())) {
            if (roomState.getSheriffRecord().getSheriffRunningTime() > 0) {
                //已经有一轮自爆，则流失警徽
                roomState.getSheriffRecord().setSheriff(0);
            } else {
                //该位置的退水
                if (roomState.getSheriffRecord().getVotingRecord().containsKey(seatNumber)) {
                    roomState.getSheriffRecord().unRegisterSheriff(seatNumber);
                }
                roomState.getSheriffRecord().addSheriffTime();
            }
            //添加白天记录
            DaytimeRecord daytimeRecord = new DaytimeRecord();
            roomState.addDaytimeRecord(daytimeRecord);

            Werewolves werewolves = (Werewolves) PlayerRoleFactory.createPlayerInstance(roomState, seatNumber);
            werewolves.explode();
            //狼人自爆的房间状态
            roomState.setStatus(RoomStatus.WOLF_EXPLODE);
            calculateNightInfo();
        } else {
            throw new RuntimeException("该角色无法自爆");
        }
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
        //警上发言狼人自爆,由法官操作
        displayInfo.addAcceptableEventType(JudgeEventType.WEREWOLVES_EXPLODE);
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
