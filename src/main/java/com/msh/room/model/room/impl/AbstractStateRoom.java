package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.DaytimeRecord;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.record.SheriffRecord;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.RoomState;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象房间状态类，用于放置多个状态下均有可能使用的逻辑方法
 * Created by zhangruiqian on 2017/5/25.
 */
public abstract class AbstractStateRoom implements RoomState {
    protected RoomStateData roomState;

    public AbstractStateRoom(RoomStateData data) {
        this.roomState = data;
    }

    /**
     * 判断是否法官可以处理该事件
     *
     * @param event
     */
    protected void filterJudgeEventType(JudgeEvent event) {
        JudgeDisplayInfo judgeDisplayInfo = displayJudgeInfo();
        if (!judgeDisplayInfo.getAcceptableEventTypes().contains(event.getEventType())) {
            throw new RoomBusinessException("此时无法接受该事件类型:" + event.getEventType());
        }
    }

    /**
     * 法官通用的display信息
     *
     * @return
     */
    protected JudgeDisplayInfo judgeCommonDisplayInfo() {
        JudgeDisplayInfo displayInfo = new JudgeDisplayInfo(roomState.getRoomCode());
        displayInfo.setStatus(roomState.getStatus());
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        //夜晚\白天\警长信息拿到无论是否为null
        displayInfo.setNightRecord(roomState.getLastNightRecord());
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        displayInfo.setSheriffRecord(roomState.getSheriffRecord());
        return displayInfo;
    }

    /**
     * 狼人自爆，白天投票完成
     */
    protected void resolveNightComing() {
        roomState.addNightRecord(new NightRecord());
        roomState.setStatus(RoomStatus.NIGHT);
    }

    /**
     * 法官随时可以解散游戏
     *
     * @param event
     */
    protected void resolveDisbandGameEvent(JudgeEvent event) {
        //房间清空
        roomState = new RoomStateData();
        roomState.setStatus(RoomStatus.VACANCY);
        roomState.setRoomCode(event.getRoomCode());
    }

    /**
     * 法官在任何状态下均可以重启游戏
     *
     * @param event
     */
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

    /**
     * 游戏结束处理，多个状态下均可能涉及
     *
     * @param event
     */
    protected void resolveGameEnding(JudgeEvent event) {
        this.roomState.setStatus(RoomStatus.GAME_OVER);
    }

    /**
     * 每个阶段都要处理，过滤本状态无法处理的事件
     *
     * @param event
     */
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

    /**
     * 天亮了事件,可能在夜晚、竞选警长的各个阶段发生
     *
     * @param event
     */
    protected void resolveDaytimeComing(JudgeEvent event) {
        //判断是否需要竞选警长
        RoomStatus roomStatus = sheriffCompetitionStatus();
        roomState.setStatus(roomStatus);
        //如果不需要竞选
        if (RoomStatus.DAYTIME.equals(roomStatus)) {
            roomState.addDaytimeRecord(new DaytimeRecord());
            //直接公布夜晚信息
            calculateNightInfo();
        }
    }

    private void calculateNightInfo() {
        NightRecord lastNightRecord = roomState.getLastNightRecord();
        List<Integer> dieList = new ArrayList();
        Integer wolfKilledSeat = lastNightRecord.getWolfKilledSeat();
        Integer witchSaved = lastNightRecord.getWitchSaved();
        if (wolfKilledSeat != 0 && wolfKilledSeat != witchSaved) {
            dieList.add(wolfKilledSeat);
        }
        if (lastNightRecord.getWitchPoisoned() != 0) {
            dieList.add(lastNightRecord.getWitchPoisoned());
        }
        lastNightRecord.setDiedNumber(dieList);
        for (Integer number : dieList) {
            CommonPlayer commonPlayer = PlayerRoleFactory.createPlayerInstance(roomState, number);
            commonPlayer.killed();
        }
    }

    /**
     * 判断是否需要上警
     *
     * @return
     */
    private RoomStatus sheriffCompetitionStatus() {
        if (roomState.isSheriff()) {
            return sheriffInfoResolve();
        } else {
            return RoomStatus.DAYTIME;
        }
    }

    private RoomStatus sheriffInfoResolve() {
        SheriffRecord sheriffRecord = roomState.getSheriffRecord();
        //还没有任何竞选信息,开始竞选
        if (sheriffRecord == null) {
            roomState.setSheriffRecord(new SheriffRecord());
            return RoomStatus.SHERIFF_REGISTER;
        } else {
            //已有结果,直接进入白天
            if (sheriffRecord.getSheriff() != null) {
                return RoomStatus.DAYTIME;
            } else {
                //有竞选信息，但没有结果。说明狼人自爆，且还没有流失警徽。继续竞选
                //如果有PK信息，说明狼人PK阶段自爆。再次PK
                //TODO PK阶段自爆与竞选阶段自爆
                if (sheriffRecord.getPkVotingRecord() != null) {
                    return RoomStatus.SHERIFF_PK;
                }
                return RoomStatus.SHERIFF_RUNNING;
            }
        }
    }
}
