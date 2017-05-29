package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.RoomState;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public abstract class AbstractStateRoom implements RoomState {
    protected RoomStateData roomState;

    public AbstractStateRoom(RoomStateData data) {
        this.roomState = data;
    }

    protected void filterJudgeEventType(JudgeEvent event) {
        JudgeDisplayInfo judgeDisplayInfo = displayJudgeInfo();
        if (!judgeDisplayInfo.getAcceptableEventTypes().contains(event.getEventType())) {
            throw new RoomBusinessException("此时无法接受该事件类型:" + event.getEventType());
        }
    }

    protected JudgeDisplayInfo judgeCommonDisplayInfo() {
        JudgeDisplayInfo displayInfo = new JudgeDisplayInfo(roomState.getRoomCode());
        displayInfo.setStatus(roomState.getStatus());
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        //夜晚和白天信息拿到无论是否为null
        displayInfo.setNightRecord(roomState.getLastNightRecord());
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        return displayInfo;
    }

    protected void resolveNightComing() {
        roomState.addNightRecord(new NightRecord());
        roomState.setStatus(RoomStatus.NIGHT);
    }

    protected void resolveDisbandGameEvent(JudgeEvent event) {
        //房间清空
        roomState = new RoomStateData();
        roomState.setStatus(RoomStatus.VACANCY);
        roomState.setRoomCode(event.getRoomCode());
    }

    protected void resolveRestartGameEvent(JudgeEvent event) {
        //重置所有座位
        roomState.getPlayerSeatInfo().forEach(seatInfo -> {
            if (!seatInfo.isSeatAvailable()) {
                seatInfo.setRole(Roles.UNASSIGN);
                seatInfo.setAlive(true);
            }
        });
        roomState.setDaytimeRecordList(null);
        roomState.setNightRecordList(null);

        roomState.setHunterState(null);
        roomState.setMoronState(null);
        roomState.setWitchState(null);
        roomState.setGameResult(null);

        roomState.setStatus(RoomStatus.CRATING);
    }

    protected void resolveGameEnding(JudgeEvent event) {
        this.roomState.setStatus(RoomStatus.GAME_OVER);
    }


    protected void filterPlayerEventType(PlayerEvent event) {
        PlayerDisplayInfo displayInfo = this.displayPlayerInfo(event.getSeatNumber());
        if (!displayInfo.getAcceptableEventTypeList().contains(event.getEventType())) {
            throw new RoomBusinessException("非法的事件类型");
        }
    }

    protected PlayerDisplayInfo playerCommonDisplayInfo(int seatNumber) {
        CommonPlayer player = PlayerRoleFactory.createPlayerInstance(roomState, seatNumber);
        return player.displayInfo();
    }
}
