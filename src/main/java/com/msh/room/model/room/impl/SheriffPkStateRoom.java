package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
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

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class SheriffPkStateRoom extends AbstractStateRoom {
    public SheriffPkStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case SHERIFF_PK_VOTEING:
                resolveSheriffPkVoting(event);
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

    private void resolveSheriffPkVoting(JudgeEvent event) {
//        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
//        //由于狼人自爆，导致PK无对手
//        if (sheriffRecord.lastPKVotingRecord().size() == 1) {
//            sheriffRecord.lastPKVotingRecord().keySet().forEach(seatNumber -> {
//                sheriffRecord.setSheriff(seatNumber);
//            });
//            roomState.setStatus(RoomStatus.DAYTIME);
//        } else {
        roomState.setStatus(RoomStatus.SHERIFF_PK_VOTING);
//        }
    }

    private void resolveWereWolfExplode(JudgeEvent event) {
        Integer seatNumber = event.getExplodeWereWolf();
        PlayerSeatInfo seatInfo = roomState.getPlaySeatInfoBySeatNumber(seatNumber);
        if (seatInfo.isAlive() && Roles.WEREWOLVES.equals(seatInfo.getRole())) {
            if (roomState.getSheriffRecord().getSheriffRunningTime() > 0) {
                //已经有一轮自爆，则流失警徽
                roomState.getSheriffRecord().setSheriff(0);
            } else {
                if (roomState.getSheriffRecord().lastPKVotingRecord().containsKey(seatNumber)) {
                    roomState.getSheriffRecord().lastPKVotingRecord().remove(seatNumber);
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

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        //可以触发上警PK投票
        displayInfo.addAcceptableEventType(JudgeEventType.SHERIFF_PK_VOTEING);
        //警上PK发言狼人自爆,由法官操作
        displayInfo.addAcceptableEventType(JudgeEventType.WEREWOLVES_EXPLODE);
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        return displayInfo;
    }
}
