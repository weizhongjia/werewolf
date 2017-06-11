package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.impl.Moron;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class MoronTimeState extends AbstractStateRoom {
    public MoronTimeState(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case MORON_SHOW:
                resolveMoronShow(event);
                break;
            case GAME_ENDING:
                resolveGameEnding(event);
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

    private void resolveMoronShow(JudgeEvent event) {
        Boolean moronShow = event.getMoronShow();
        moronShow(moronShow);
    }

    private void moronShow(Boolean moronShow) {
        //涉及到可能存在移交警徽等情况，先将状态还原
        roomState.setStatus(roomState.getMoronState().getLastRoomStatus());
        int moronSeat = roomState.getFirstSeatByRole(Roles.MORON);
        Moron moron = (Moron) PlayerRoleFactory.createPlayerInstance(roomState, moronSeat);
        //白痴放弃
        if (!moronShow) {
            //放弃翻牌
            moron.giveUp();
        }else{
            //白痴翻牌
            moron.moronShow();
        }
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        displayInfo.addAcceptableEventType(JudgeEventType.MORON_SHOW);
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        throw new RoomBusinessException("暂时无法处理事件");
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        if (Roles.MORON.equals(roomState.getPlaySeatInfoBySeatNumber(seatNumber).getRole())) {
            displayInfo.addAcceptableEventType(PlayerEventType.MORON_SHOW);
        }
        //此时投票有结果了.公布投票信息
        displayInfo.setDaytimeRecord(roomState.getLastDaytimeRecord());
        return displayInfo;
    }
}
