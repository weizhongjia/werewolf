package com.msh.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.Room;
import com.msh.room.model.room.RoomManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class NightTest {
    private RoomManager roomManager;
    private RoomStateDataRepository repository;
    private String roomCode = "abc";

    @Before
    public void setup() {
        repository = new RoomStateDataRepository();
        roomManager = new RoomManager();
        roomManager.setDataRepository(repository);
        //房间空闲
        RoomStateData data = new RoomStateData();
        data.setRoomCode(roomCode);
        data.setStatus(RoomStatus.VACANCY);
        repository.putRoomStateData(roomCode, data);
        Room room = roomManager.loadRoom(roomCode);
        //create
        JudgeEvent createRoomEvent = new JudgeEvent(roomCode, JudgeEventType.CREATE_ROOM);
        Map<Roles, Integer> gameConfig = new HashMap<>();
        gameConfig.put(Roles.VILLAGER, 4);
        gameConfig.put(Roles.WEREWOLVES, 4);
        gameConfig.put(Roles.WITCH, 1);
        gameConfig.put(Roles.HUNTER, 1);
        gameConfig.put(Roles.SEER, 1);
        gameConfig.put(Roles.MORON, 1);
        createRoomEvent.setGameConfig(gameConfig);
        room.resolveJudgeEvent(createRoomEvent);
        //joinAll
        for (int i = 1; i <= 12; i++) {
            int seatNumber = i;
            String userId = "Richard_" + i;
            PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, seatNumber, userId);
            room.resolvePlayerEvent(event);
        }

        JudgeEvent completeEvent = new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
        room.resolveJudgeEvent(completeEvent);
    }

    @Test
    public void testFirstNightComing() {
        Room room = roomManager.loadRoom(roomCode);
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(nightComingEvent);
        List<JudgeEventType> acceptableEventTypes = judgeDisplayInfo.getAcceptableEventTypes();
        JudgeEventType firstAcceptableJudgeEvent = acceptableEventTypes.get(0);
        assertEquals(JudgeEventType.WOLF_KILL, firstAcceptableJudgeEvent);
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.WOLF_KILL, playerEventType);
            } else if (Roles.SEER.equals(seatInfo.getRole())) {
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.SEER_VERIFY, playerEventType);
            } else if (Roles.WITCH.equals(stateData.getPlaySeatInfoBySeatNumber(i).getRole())){
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.WITCH_SAVE, playerEventType);
            } else {
                //闭眼
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }
    }

    @Test
    public void testWolfKillVillager() {
        Room room = roomManager.loadRoom(roomCode);
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        int seat = getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        assertEquals(Integer.valueOf(seat), stateData.getLastNightRecord().getWolfKilledSeat());

        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(wolfKillEvent);
        JudgeEventType eventType = judgeDisplayInfo.getAcceptableEventTypes().get(0);
        assertEquals(JudgeEventType.SEER_VERIFY, eventType);
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.SEER.equals(seatInfo.getRole())) {
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.SEER_VERIFY, playerEventType);
            } else if (Roles.WITCH.equals(seatInfo.getRole())) {
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.WITCH_SAVE, playerEventType);
            } else {
                //闭眼
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }
    }

    @Test
    public void testSeerVerifyWolf() {
        Room room = roomManager.loadRoom(roomCode);
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀个民
        int seat = getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);

        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验个狼
        int wolfSeat = getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(seerVerifyEvent);
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        assertEquals(JudgeEventType.WITCH_SAVE, judgeDisplayInfo.getAcceptableEventTypes().get(0));
        assertTrue(judgeDisplayInfo.getNightRecord().isSeerVerifyResult());
        assertEquals(Integer.valueOf(wolfSeat), stateData.getLastNightRecord().getSeerVerify());
        assertTrue(stateData.getLastNightRecord().isSeerVerifyResult());
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.SEER.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.WITCH.equals(seatInfo.getRole())) {
                //睁眼状态
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.WITCH_SAVE, playerEventType);
            } else {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }
    }

    @Test
    public void testWitchSaveEvent() {
        Room room = roomManager.loadRoom(roomCode);
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀个民
        int seat = getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);

        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验个狼
        int wolfSeat = getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        room.resolveJudgeEvent(seerVerifyEvent);

        JudgeEvent witchEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchEvent.setWitchSave(true);
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(witchEvent);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        assertEquals(JudgeEventType.FAKE_WITCH_POISON, judgeDisplayInfo.getAcceptableEventTypes().get(0));
        assertEquals(Integer.valueOf(seat), stateData.getLastNightRecord().getWitchSaved());


        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.SEER.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.WITCH.equals(seatInfo.getRole())) {
                //女巫还要假装毒人，睁眼状态(现实情况是闭眼状态)
                PlayerEventType playerEventType = displayInfo.getAcceptableEventTypeList().get(0);
                assertEquals(PlayerEventType.FAKE_WITCH_POISON, playerEventType);
            } else {
                //闭眼
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }

    }


    @Test
    public void testWitchNoSaveAndPoisonOneEvent() {
        Room room = roomManager.loadRoom(roomCode);
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀个民
        int seat = getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);

        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验个狼
        int wolfSeat = getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        room.resolveJudgeEvent(seerVerifyEvent);

        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(false);
        room.resolveJudgeEvent(witchSaveEvent);

        JudgeEvent witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        //毒个狼
        witchPoisonEvent.setWitchPoisonNumber(wolfSeat);
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(witchPoisonEvent);


        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        assertEquals(JudgeEventType.DAYTIME_COMING, judgeDisplayInfo.getAcceptableEventTypes().get(0));
        //没救人
        assertEquals(Integer.valueOf(0), stateData.getLastNightRecord().getWitchSaved());
        //狼被毒了
        assertEquals(Integer.valueOf(wolfSeat), stateData.getLastNightRecord().getWitchPoisoned());

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.SEER.equals(seatInfo.getRole())) {
                //闭眼了
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else if (Roles.WITCH.equals(seatInfo.getRole())) {
                //女巫毕眼状态
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            } else {
                //闭眼
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }

    }

    private int getAliveSeatByRole(Roles role) {
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        for (PlayerSeatInfo seatInfo : stateData.getPlayerSeatInfo()) {
            if (seatInfo.getRole().equals(role) && seatInfo.isAlive()) {
                return seatInfo.getSeatNumber();
            }
        }
        return 0;
    }
}
