package com.msh.room.model.role;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.state.HunterState;
import com.msh.room.dto.room.state.MoronState;
import com.msh.room.dto.room.state.WitchState;
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
        switch (event.getEventType()) {
            case CREATE_ROOM:
                resolveCreateRoomEvent(event);
                break;
            case COMPLETE_CREATE:
                resolveCompleteCreateEvent(event);
                break;
            case NIGHT_COMING:
                resolveNightComing();
                break;
            case WOLF_KILL:
                resolveWolfKill(event);
                break;
            case SEER_VERIFY:
                break;
            case FAKE_SEER_VERIFY:
                break;
            case WITCH_SAVE:
                break;
            case WITCH_POISON:
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

    private void resolveWolfKill(JudgeEvent event) {
        Integer wolfKillNumber = event.getWolfKillNumber();
        if (roomState.getPlaySeatInfoBySeatNumber(wolfKillNumber).isAlive()) {
            roomState.getLastNightRecord().setWolfKilledSeat(wolfKillNumber);
        } else {
            throw new RoomBusinessException("无法杀掉一个死人");
        }

    }

    private void resolveNightComing() {
        roomState.addNightRecord(new NightRecord());
        roomState.setStatus(RoomStatus.NIGHT);
    }

    private void resolveDisbandGameEvent(JudgeEvent event) {
        //房间清空
        roomState = new RoomStateData();
        roomState.setStatus(RoomStatus.VACANCY);
        roomState.setRoomCode(event.getRoomCode());
    }

    private void resolveRestartGameEvent(JudgeEvent event) {
        //重置所有座位
        roomState.getPlayerSeatInfo().forEach(seatInfo -> {
            if (!seatInfo.isSeatAvailable()) {
                seatInfo.setRole(Roles.UNASSIGN);
                seatInfo.setAlive(true);
            }
        });
        roomState.setStatus(RoomStatus.CRATING);
    }

    private void resolveCompleteCreateEvent(JudgeEvent event) {
        if (RoomStatus.CRATING.equals(roomState.getStatus()) || allPlayersReady()) {
            assignRoleCard();
            initRoleState();
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
        if (roomState.getGameConfig().get(Roles.HUNTER) == 1) {
            HunterState hunterState = new HunterState();
            hunterState.setAlive(true);
            hunterState.setShotAvailable(false);
            roomState.setHunterState(hunterState);
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
        if (RoomStatus.VACANCY.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.CREATE_ROOM);
        }
        if (RoomStatus.CRATING.equals(roomState.getStatus())) {
            if (allPlayersReady()) {
                displayInfo.addAcceptableEventType(JudgeEventType.COMPLETE_CREATE);
            }
        }
        if (RoomStatus.CRATED.equals(roomState.getStatus())) {
            displayInfo.addAcceptableEventType(JudgeEventType.NIGHT_COMING);
        }
        if (RoomStatus.NIGHT.equals(roomState.getStatus())) {
            NightRecord nightRecord = roomState.getLastNightRecord();
            if (nightRecord.getWolfKilledSeat() == null) {
                //狼刀
                displayInfo.addAcceptableEventType(JudgeEventType.WOLF_KILL);
            } else if (nightRecord.getSeerVerify() == null) {
                //预言家
                int seerSeat = roomState.getAliveSeatByRole(Roles.SEER);
                //预言家已死则没有验人环节
                JudgeEventType type = (seerSeat == 0) ? JudgeEventType.FAKE_SEER_VERIFY : JudgeEventType.SEER_VERIFY;
                displayInfo.addAcceptableEventType(type);
            } else {
                //女巫用药询问逻辑,此处逻辑仅限不能同时用两种药的情况
                //女巫本轮未询问用解药==null，女巫解药可用: WITCH_SAVE
                //女巫本轮未询问用解药==null，女巫解药不可用 FAKE_WITCH_SAVE
                //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药可用 WITCH_POISON
                //女巫本轮已询问用解药,但未用==0，但未询问用毒药==null，女巫毒药不可用 FAKE_WITCH_POISON
                //女巫本轮已询问用解药，但已用!=0，但未询问是否用毒药 FAKE_WITCH_POISON
                //其他情况 null
            }
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }
}
