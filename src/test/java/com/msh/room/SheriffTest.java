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
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.RoomStateFactory;
import com.msh.room.service.RoomService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by zhangruiqian on 2017/6/1.
 */
public class SheriffTest {
    private RoomStateDataRepository repository;
    private String roomCode = "abc";
    private RoomService service = new RoomService();

    @Before
    public void setup() {
        repository = new RoomStateDataRepository();
        //房间空闲
        RoomStateData data = new RoomStateData();
        data.setRoomCode(roomCode);
        data.setStatus(RoomStatus.VACANCY);
        data.setSheriff(true);
        repository.putRoomStateData(roomCode, data);
        service.setDataRepository(repository);
        service.setRoomFactory(new RoomStateFactory());
        //create
        createRoom();
        //joinAll
        joinAll();
    }

    private void joinAll() {
        for (int i = 1; i <= 12; i++) {
            String userId = "Richard_" + i;
            PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, i, userId);
            service.resolvePlayerEvent(event, roomCode);
        }
        JudgeEvent completeEvent = new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
        service.resolveJudgeEvent(completeEvent, roomCode);
    }

    private void createRoom() {
        JudgeEvent createRoomEvent = new JudgeEvent(roomCode, JudgeEventType.CREATE_ROOM);
        Map<Roles, Integer> gameConfig = new HashMap<>();
        gameConfig.put(Roles.VILLAGER, 4);
        gameConfig.put(Roles.WEREWOLVES, 4);
        gameConfig.put(Roles.WITCH, 1);
        gameConfig.put(Roles.HUNTER, 1);
        gameConfig.put(Roles.SEER, 1);
        gameConfig.put(Roles.MORON, 1);
        createRoomEvent.setGameConfig(gameConfig);
        //TODO 要处理创建房间event事件
        createRoomEvent.setSheriffSwich(true);
        service.resolveJudgeEvent(createRoomEvent, roomCode);
    }

    private void simpleKillVillagerNight() {
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        service.resolveJudgeEvent(nightComingEvent, roomCode);
        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀个民
        int seat = repository.loadRoomStateData(roomCode).getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        service.resolveJudgeEvent(wolfKillEvent, roomCode);
        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验个狼
        int wolfSeat = repository.loadRoomStateData(roomCode).getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        service.resolveJudgeEvent(seerVerifyEvent, roomCode);
        //没救
        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(false);
        service.resolveJudgeEvent(witchSaveEvent, roomCode);
        JudgeEvent witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        //也没毒
        witchPoisonEvent.setWitchPoisonNumber(0);
        service.resolveJudgeEvent(witchPoisonEvent, roomCode);
        return;
    }

    @Test
    public void testDaytimeWithSheriff() {
        simpleKillVillagerNight();
        JudgeEvent dayTimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(dayTimeEvent, roomCode);
        assertEquals(RoomStatus.SHERIFF_REGISTER, judgeDisplayInfo.getStatus());
        assertNotNull(judgeDisplayInfo.getSheriffRecord());
        assertEquals(Arrays.asList(JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                judgeDisplayInfo.getAcceptableEventTypes());
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo playerDisplayInfo = service.getPlayerDisplayResult(roomCode, i);
            assertEquals(Arrays.asList(PlayerEventType.SHERIFF_REGISTER), playerDisplayInfo.getAcceptableEventTypeList());
        }

        //前六个上警
        for (int i = 1; i < 7; i++) {
            PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, i);
            PlayerEvent playerEvent
                    = new PlayerEvent(PlayerEventType.SHERIFF_REGISTER, i, info.getPlayerInfo().getUserID());
            PlayerDisplayInfo result = service.resolvePlayerEvent(playerEvent, roomCode);
            assertTrue(result.getSheriffRecord().getSheriffRegisterList().contains(i));
            assertTrue(result.getSheriffRecord().getVotingRecord().keySet().contains(i));
        }
        for (int i = 7; i < 13; i++) {
            PlayerDisplayInfo info = service.getPlayerDisplayResult(roomCode, i);
            assertNull(info.getSheriffRecord());
        }
    }
}
