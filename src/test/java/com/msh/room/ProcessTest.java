package com.msh.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.cache.RoomStateLockRepository;
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
import com.msh.room.model.room.RoomStateFactory;
import com.msh.room.service.WereWolfRoomService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by zhangruiqian on 2017/5/21.
 */
public class ProcessTest {
    private RoomStateDataRepository repository;
    private String roomCode = "abc";
    private WereWolfRoomService service = new WereWolfRoomService();

    @Before
    public void setup() {
        repository = new RoomStateDataRepository();
        //房间空闲
        RoomStateData data = new RoomStateData();
        data.setRoomCode(roomCode);
        data.setStatus(RoomStatus.VACANCY);
        repository.putRoomStateData(roomCode, data);
        service.setDataRepository(repository);
        service.setRoomFactory(new RoomStateFactory());
        service.setLockRepository(new RoomStateLockRepository());

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
        service.resolveJudgeEvent(createRoomEvent, roomCode);
        for (int i = 1; i <= 12; i++) {
            String userId = "Richard_" + i;
            PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, i, userId);
            service.resolvePlayerEvent(event, roomCode);
        }
        JudgeEvent completeEvent = new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
        service.resolveJudgeEvent(completeEvent, roomCode);
    }

    private void simpleKillVillagerNight(boolean save) {
        //天黑
        JudgeEvent nightComingEvent = new JudgeEvent(roomCode, JudgeEventType.NIGHT_COMING);
        service.resolveJudgeEvent(nightComingEvent, roomCode);

        JudgeEvent wolfKillEvent = new JudgeEvent(roomCode, JudgeEventType.WOLF_KILL);
        //杀第一个民
        RoomStateData roomStateData = repository.loadRoomStateData(roomCode);
        int seat = roomStateData.getAliveSeatByRole(Roles.VILLAGER);
        wolfKillEvent.setWolfKillNumber(seat);
        service.resolveJudgeEvent(wolfKillEvent, roomCode);
        JudgeEvent seerVerifyEvent = new JudgeEvent(roomCode, JudgeEventType.SEER_VERIFY);
        //验第一个狼
        int wolfSeat = roomStateData.getAliveSeatByRole(Roles.WEREWOLVES);
        seerVerifyEvent.setSeerVerifyNumber(wolfSeat);
        service.resolveJudgeEvent(seerVerifyEvent, roomCode);
        //没救
        JudgeEvent witchSaveEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_SAVE);
        witchSaveEvent.setWitchSave(save);
        service.resolveJudgeEvent(witchSaveEvent, roomCode);
        JudgeEvent witchPoisonEvent;
        if (!save) {
            witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.WITCH_POISON);
        } else {
            witchPoisonEvent = new JudgeEvent(roomCode, JudgeEventType.FAKE_WITCH_POISON);
        }
        //也没毒
        witchPoisonEvent.setWitchPoisonNumber(0);
        service.resolveJudgeEvent(witchPoisonEvent, roomCode);

        //猎人询问时间
        service.resolveJudgeEvent(new JudgeEvent(roomCode, JudgeEventType.HUNTER_STATE), roomCode);
        return;
    }

    @Test
    public void testWholeProcessWithoutSheriff() {
        simpleKillVillagerNight(false);
        daytimeVote();
        simpleKillVillagerNight(false);
        daytimeVote();
        simpleKillVillagerNight(false);
        daytimeVote();
        simpleKillVillagerNight(true);
        daytimeVote();
//        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
//        service.resolveJudgeEvent(daytimeEvent, roomCode);
        JudgeDisplayInfo judgeDisplayResult = service.getJudgeDisplayResult(roomCode);
        assertEquals(Arrays.asList(JudgeEventType.GAME_ENDING, JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                judgeDisplayResult.getAcceptableEventTypes());
        JudgeEvent judgeEvent = new JudgeEvent(roomCode, JudgeEventType.GAME_ENDING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(judgeEvent, roomCode);
        System.out.println(repository.loadRoomStateData(roomCode).getGameResult());
        for (PlayerSeatInfo info : judgeDisplayInfo.getPlayerSeatInfoList()) {
            System.out.println(info.getFinalScore() + ":" + info.getRole());
        }
    }

    private void daytimeVote() {
        //天亮
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(daytimeEvent, roomCode);
        //开始投票
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        service.resolveJudgeEvent(daytimeVotingEvent, roomCode);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        //第一个狼
        int wolf = stateData.getAliveSeatByRole(Roles.WEREWOLVES);
//        int seer = stateData.getAliveSeatByRole(Roles.SEER);
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (playerInfo.isAlive()) {
                PlayerEvent playerEvent =
                        new PlayerEvent(PlayerEventType.DAYTIME_VOTE, i, playerInfo.getUserID());
                //大家都投狼
                playerEvent.setDaytimeVoteNumber(wolf);
                service.resolvePlayerEvent(playerEvent, roomCode);
            }
        }
    }
}
