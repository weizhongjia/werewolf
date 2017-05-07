package com.msh.room.model.role;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.exception.RoomBusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class Judge {
    private RoomStateData roomState;

    public Judge(RoomStateData roomState) {
        this.roomState = roomState;
    }

    public RoomStateData resolveEvent(JudgeEvent event) throws RoomBusinessException {
        if (JudgeEventType.CREATE_ROOM.equals(event.getEventType())) {
            resolveCreateRoomEvent(event);
        }
        if (JudgeEventType.COMPLETE_CREATE.equals(event.getEventType())) {
            resolveCompleteCreateEvent(event);
        }
        return roomState;
    }

    private void resolveCompleteCreateEvent(JudgeEvent event) {
        if (RoomStatus.CRATING.equals(roomState.getStatus()) || allPlayersReady()) {
            assignRoleCard();
            roomState.setStatus(RoomStatus.CRATED);
        } else {
            throw new RoomBusinessException("房间目前状态还无法开始游戏");
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
                cardNum = random.nextInt(cardList.size() - 1);
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

    private void resolveCreateRoomEvent(JudgeEvent event) throws RoomBusinessException {
        if (roomState.getStatus().equals(RoomStatus.VACANCY)) {
            roomState.setStatus(RoomStatus.CRATING);
            roomState.setGameConfig(event.getGameConfig());
            //初始化座位
            roomState.setPlayerSeatInfo(initSeatInfo(event.getGameConfig()));
        } else {
            throw new RoomBusinessException("房间非空闲，无法创建游戏");
        }
    }

    private List<PlayerSeatInfo> initSeatInfo(Map<Roles, Integer> gameConfig) {
        List playerInfoList = new ArrayList();
        int seatNumber = 1;
        for (Roles role : gameConfig.keySet()) {
            Integer num = gameConfig.get(role);
            for (int i = 0; i < num; i++) {
                PlayerSeatInfo seatInfo = new PlayerSeatInfo(seatNumber, true);
                seatInfo.setAlive(false);
                seatInfo.setRole(Roles.NONE);
                playerInfoList.add(seatInfo);
                seatNumber++;
            }
        }
        return playerInfoList;
    }


    public JudgeDisplayInfo displayInfo() {
        JudgeDisplayInfo displayInfo = new JudgeDisplayInfo(roomState.getRoomCode());
        displayInfo.setStatus(roomState.getStatus());
        displayInfo.setPlayerSeatInfoList(roomState.getPlayerSeatInfo());
        return acceptableEventCalculate(displayInfo);
    }

    private JudgeDisplayInfo acceptableEventCalculate(JudgeDisplayInfo displayInfo) {
        if (RoomStatus.CRATING.equals(roomState.getStatus())) {
            if (allPlayersReady()) {
                displayInfo.addAcceptableEventType(JudgeEventType.COMPLETE_CREATE);
            }
        }
        if(RoomStatus.CRATED.equals(roomState.getStatus())){
            displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMMING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }
}
