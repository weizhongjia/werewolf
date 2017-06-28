package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.MoronState;
import com.msh.room.dto.room.state.WitchState;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class CreatingStateRoom extends AbstractStateRoom {
    public CreatingStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case COMPLETE_CREATE:
                resolveCompleteCreateEvent(event);
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

    private void resolveCompleteCreateEvent(JudgeEvent event) {
        if (RoomStatus.CRATING.equals(roomState.getStatus()) || allPlayersReady()) {
            assignRoleCard();
            initRoleState();
            //TODO 持久化角色GAME信息，获取GAME_ID
            roomState.setStatus(RoomStatus.CRATED);
        } else {
            throw new RoomBusinessException("房间目前状态还无法开始游戏");
        }
    }

    private void initRoleState() {
        if (roomState.getGameConfig().get(Roles.WITCH) == 1) {
            WitchState witchState = new WitchState();
            witchState.setAlive(true);
            witchState.setPoisonAvailable(true);
            witchState.setAntidoteAvailable(true);
            witchState.setSaveBySelf(false);
            roomState.setWitchState(witchState);
        }
        if (roomState.getGameConfig().get(Roles.MORON) == 1) {
            MoronState moronState = new MoronState();
            moronState.setBeanVoted(false);
            roomState.setMoronState(moronState);
        }
    }

    private void assignRoleCard() {
        Map<Roles, Integer> gameConfig = roomState.getGameConfig();
        List<Roles> cardList = new ArrayList<>();
        gameConfig.keySet().forEach(role -> {
            Integer count = gameConfig.get(role);
            for (int i = 0; i < count; i++) {
                cardList.add(role);
            }
        });

        if (cardList.size() != roomState.getPlayerSeatInfo().size()) {
            throw new RoomBusinessException("游戏配置与游戏人数不一致");
        }
        Random random = new Random();
        roomState.getPlayerSeatInfo().stream().forEach(seatInfo -> {
            int cardNum = 0;
            if (cardList.size() > 1) {
                cardNum = random.nextInt(cardList.size());
            }
            seatInfo.setRole(cardList.get(cardNum));
            cardList.remove(cardNum);
        });
    }

    private boolean allPlayersReady() {
        long count = roomState.getPlayerSeatInfo().stream().filter(seatInfo -> seatInfo.isSeatAvailable()).count();
        if (count == 0) {
            return true;
        }
        return false;
    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();

        if (allPlayersReady()) {
            displayInfo.addAcceptableEventType(JudgeEventType.COMPLETE_CREATE);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }


    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        switch (event.getEventType()) {
            case JOIN_ROOM:
                resolveJoinRoom(event);
                break;
            case Exit_ROOM:
                resolveExitRoom(event);
                break;
        }
        return roomState;
    }

    private void resolveExitRoom(PlayerEvent event) {
        PlayerSeatInfo seatInfo = roomState.getPlayerSeatInfo().get(event.getSeatNumber() - 1);
        seatInfo.setRole(Roles.NONE);
        seatInfo.setUserID(null);
        seatInfo.setAlive(false);
        seatInfo.setSeatAvailable(true);
    }

    private void resolveJoinRoom(PlayerEvent event) {
        if (event.getSeatNumber() < 0 || event.getSeatNumber() > roomState.getPlayerSeatInfo().size()) {
            throw new RoomBusinessException("该房间无法容纳该座位号玩家,请检查游戏配置");
        }
        PlayerSeatInfo seatInfo = roomState.getPlayerSeatInfo().get(event.getSeatNumber() - 1);
        if (seatInfo.isSeatAvailable() && seatInfo.getSeatNumber() == event.getSeatNumber()) {
            seatInfo.setRole(Roles.UNASSIGN);
            seatInfo.setSeatAvailable(false);
            seatInfo.setAlive(true);
            seatInfo.setUserID(event.getUserID());
        } else {
            throw new RoomBusinessException("该座位已被占用，请联系法官");
        }
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        PlayerDisplayInfo displayInfo = playerCommonDisplayInfo(seatNumber);
        return displayInfo;
    }
}
