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
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/21.
 */
public class ProcessTest {
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
        //joinAll
        room.resolveJudgeEvent(createRoomEvent);
        for (int i = 1; i <= 12; i++) {
            String userId = "Richard_" + i;
            PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, i, userId);
            room.resolvePlayerEvent(event);
        }
        JudgeEvent completeEvent = new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
        room.resolveJudgeEvent(completeEvent);
    }

    private void simpleKillVillagerNight(Room room) {
        //天黑
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀第一个民
        RoomStateData roomStateData = repository.loadRoomStateData(roomCode);
        int seat = roomStateData.getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);
        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验第一个狼
        int wolfSeat = roomStateData.getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        room.resolveJudgeEvent(seerVerifyEvent);
        //没救
        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(false);
        room.resolveJudgeEvent(witchSaveEvent);
        JudgeEvent witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        //也没毒
        witchPoisonEvent.setWitchPoisonNumber(0);
        room.resolveJudgeEvent(witchPoisonEvent);
        return;
    }

    @Test
    public void testWholeProcessWithoutSheriff() {
        Room room = roomManager.loadRoom(roomCode);
        simpleKillVillagerNight(room);
        daytimeVote(room);
        simpleKillVillagerNight(room);
        daytimeVote(room);
        simpleKillVillagerNight(room);
        daytimeVote(room);
        //天黑
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        room.resolveJudgeEvent(nightComingEvent);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //不杀
        RoomStateData roomStateData = repository.loadRoomStateData(roomCode);
        int seat = 0;
        wolfKillEvent.setWolfKillNumber(seat);
        room.resolveJudgeEvent(wolfKillEvent);
        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验第一个狼
        int wolfSeat = roomStateData.getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        room.resolveJudgeEvent(seerVerifyEvent);
        //没救
        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(false);
        room.resolveJudgeEvent(witchSaveEvent);
        JudgeEvent witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        //也没毒
        witchPoisonEvent.setWitchPoisonNumber(0);
        room.resolveJudgeEvent(witchPoisonEvent);
//        simpleKillVillagerNight(room);
        daytimeVote(room);
//        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
//        room.resolveJudgeEvent(daytimeEvent);
        JudgeDisplayInfo judgeDisplayResult = room.getJudgeDisplayResult();
        judgeDisplayResult.getAcceptableEventTypes();
    }

    private void daytimeVote(Room room) {
        //天亮
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        room.resolveJudgeEvent(daytimeEvent);
        //开始投票
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        room.resolveJudgeEvent(daytimeVotingEvent);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        //第一个狼
        int wolf = stateData.getAliveSeatByRole(Roles.WEREWOLVES);
//        int seer = stateData.getAliveSeatByRole(Roles.SEER);
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = room.getPlayerDisplayResult(i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (playerInfo.isAlive()) {
                PlayerEvent playerEvent = new PlayerEvent(PlayerEventType.DAYTIME_VOTE, i, playerInfo.getUserID());
                //大家都投狼
                playerEvent.setDaytimeVoteNumber(wolf);
                room.resolvePlayerEvent(playerEvent);
            }
        }
    }
}
