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
import com.msh.room.model.room.RoomStateFactory;
import com.msh.room.service.WereWolfRoomService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by zhangruiqian on 2017/5/19.
 */
public class DaytimeTest {
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
        service.resolveJudgeEvent(createRoomEvent, roomCode);
    }

    @Test
    public void testSimpleDayTimeWithoutSheriff() {
        simpleKillVillagerNight();
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(daytimeEvent, roomCode);
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        //支持进入投票事件
        assertEquals(JudgeEventType.DAYTIME_VOTING, judgeDisplayInfo.getAcceptableEventTypes().get(0));

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo seatInfo = stateData.getPlaySeatInfoBySeatNumber(i);
            //第一个村民死了
            int villagerSeat = stateData.getFirstSeatByRole(Roles.VILLAGER);
            if (i == villagerSeat) {
                assertFalse(seatInfo.isAlive());
            }
            //每个玩家的视角里，第一个村民死了
            List<PlayerSeatInfo> seatInfoList = displayInfo.getPlayerSeatInfoList();
            assertFalse(seatInfoList.get(villagerSeat - 1).isAlive());
            assertEquals(Integer.valueOf(villagerSeat), displayInfo.getNightRecord().getDiedNumber().get(0));
            if (Roles.SEER.equals(seatInfo.getRole())) {
                for (PlayerSeatInfo info : seatInfoList) {
                    //预言家昨晚验到的是一个狼人,体现在座位中
                    int seatNumber = stateData.getLastNightRecord().getSeerVerify();
                    if (seatNumber == info.getSeatNumber()) {
                        assertEquals(Roles.WEREWOLVES, info.getRole());
                    } else {
                        //预言家视角，其他角色未知
                        if (i != info.getSeatNumber()) {
                            assertNull(info.getRole());
                        }
                    }
                }
            } else if (Roles.WEREWOLVES.equals(seatInfo.getRole())) {
                //狼人视角里，有狼同伴
                for (PlayerSeatInfo info : seatInfoList) {
                    if (!Roles.WEREWOLVES.equals(info.getRole()))
                        assertNull(info.getRole());
                }
            } else {
                //其他身份看不到任何信息
                for (PlayerSeatInfo info : seatInfoList) {
                    if (i != info.getSeatNumber()) {
                        assertNull(info.getRole());
                    }
                }
            }
        }


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
    public void testDaytimeVoting() {
        simpleKillVillagerNight();
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(daytimeEvent, roomCode);
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(daytimeVotingEvent, roomCode);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            if (displayInfo.getPlayerInfo().isAlive()) {
                assertEquals(PlayerEventType.DAYTIME_VOTE, displayInfo.getAcceptableEventTypeList().get(0));
            } else {
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }
    }

    @Test
    public void testDaytimeVoteSomeone() {
//        Room room = roomManager.loadRoom(roomCode);
        simpleKillVillagerNight();
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(daytimeEvent, roomCode);
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        service.resolveJudgeEvent(daytimeVotingEvent, roomCode);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        int wolf = stateData.getAliveSeatByRole(Roles.WEREWOLVES);
        int seer = stateData.getAliveSeatByRole(Roles.SEER);
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (playerInfo.isAlive()) {
                PlayerEvent playerEvent = new PlayerEvent(PlayerEventType.DAYTIME_VOTE, i, playerInfo.getUserID());
                if (Roles.WEREWOLVES.equals(playerInfo.getRole())) {
                    //狼投预言家
                    playerEvent.setDaytimeVoteNumber(seer);
                } else {
                    //其他投狼
                    playerEvent.setDaytimeVoteNumber(wolf);
                }
                service.resolvePlayerEvent(playerEvent, roomCode);
            }
        }
        JudgeDisplayInfo judgeDisplayResult = service.getJudgeDisplayResult(roomCode);
        assertNotNull(judgeDisplayResult.getDaytimeRecord());
        assertEquals(Integer.valueOf(wolf), judgeDisplayResult.getDaytimeRecord().getDiedNumber());
        assertEquals(RoomStatus.VOTING, judgeDisplayResult.getStatus());

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (playerInfo.isAlive()) {
                assertNotNull(displayInfo.getDaytimeRecord());
                assertEquals(Integer.valueOf(wolf), displayInfo.getDaytimeRecord().getDiedNumber());
            }
        }
    }


    @Test
    public void testDaytimeVotePK() {
        simpleKillVillagerNight();
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(daytimeEvent, roomCode);
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        service.resolveJudgeEvent(daytimeVotingEvent, roomCode);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        int wolf = stateData.getAliveSeatByRole(Roles.WEREWOLVES);
        int seer = stateData.getAliveSeatByRole(Roles.SEER);
        simpleVoteWolfAndSeer(wolf, seer);
        JudgeDisplayInfo judgeDisplayResult = service.getJudgeDisplayResult(roomCode);
        assertEquals(RoomStatus.PK, judgeDisplayResult.getStatus());
        assertNull(judgeDisplayResult.getDaytimeRecord().getDiedNumber());
        assertEquals(JudgeEventType.DAYTIME_PK_VOTING, judgeDisplayResult.getAcceptableEventTypes().get(0));
        Map<Integer, List<Integer>> pkVotingResult = judgeDisplayResult.getDaytimeRecord().getPkVotingRecord().get(0);
        assertEquals(2, pkVotingResult.size());
        assertTrue(pkVotingResult.containsKey(wolf));
        assertTrue(pkVotingResult.containsKey(seer));

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            assertNull(judgeDisplayResult.getDaytimeRecord().getDiedNumber());
            assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            Map<Integer, List<Integer>> pkVotingRecord = displayInfo.getDaytimeRecord().getPkVotingRecord().get(0);
            assertEquals(2, pkVotingRecord.size());
            assertTrue(pkVotingRecord.containsKey(wolf));
            assertTrue(pkVotingRecord.containsKey(seer));
        }
        JudgeEvent pkVoteEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_PK_VOTING);
        JudgeDisplayInfo judgeDisplayInfo = service.resolveJudgeEvent(pkVoteEvent, roomCode);

        assertEquals(Arrays.asList(JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                judgeDisplayInfo.getAcceptableEventTypes());
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (!pkVotingResult.containsKey(i) && playerInfo.isAlive()) {
                assertEquals(Arrays.asList(PlayerEventType.PK_VOTE), displayInfo.getAcceptableEventTypeList());
                PlayerEvent playerPKVoteEvent = new PlayerEvent(PlayerEventType.PK_VOTE, i, playerInfo.getUserID());
                playerPKVoteEvent.setPkVoteNumber(wolf);
                PlayerDisplayInfo votedDisplayInfo = service.resolvePlayerEvent(playerPKVoteEvent, roomCode);
                assertEquals(0, votedDisplayInfo.getAcceptableEventTypeList().size());
            } else {
                assertEquals(0, displayInfo.getAcceptableEventTypeList().size());
            }
        }
        JudgeDisplayInfo result = service.getJudgeDisplayResult(roomCode);
        assertEquals(Arrays.asList(JudgeEventType.NIGHT_COMING, JudgeEventType.RESTART_GAME, JudgeEventType.DISBAND_GAME),
                result.getAcceptableEventTypes());
        assertEquals(Integer.valueOf(wolf), result.getDaytimeRecord().getDiedNumber());
        assertFalse(result.getPlayerSeatInfoList().get(wolf - 1).isAlive());
    }

    @Test
    public void testTwoTimesPK() {
        simpleKillVillagerNight();
        JudgeEvent daytimeEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_COMING);
        service.resolveJudgeEvent(daytimeEvent, roomCode);
        JudgeEvent daytimeVotingEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_VOTING);
        service.resolveJudgeEvent(daytimeVotingEvent, roomCode);

        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        int wolf = stateData.getAliveSeatByRole(Roles.WEREWOLVES);
        int seer = stateData.getAliveSeatByRole(Roles.SEER);
        simpleVoteWolfAndSeer(wolf, seer);

        JudgeDisplayInfo judgeDisplayResult = service.getJudgeDisplayResult(roomCode);
        Map<Integer, List<Integer>> pkVotingResult = judgeDisplayResult.getDaytimeRecord().getPkVotingRecord().get(0);

        JudgeEvent pkVoteEvent = new JudgeEvent(roomCode, JudgeEventType.DAYTIME_PK_VOTING);
        service.resolveJudgeEvent(pkVoteEvent, roomCode);

        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (!pkVotingResult.containsKey(i) && playerInfo.isAlive()) {
                PlayerEvent playerPKVoteEvent = new PlayerEvent(PlayerEventType.PK_VOTE, i, playerInfo.getUserID());
                //TODO 处理PK投票人需要平票,人不够，需要女巫开毒
                playerPKVoteEvent.setPkVoteNumber(wolf);
                service.resolvePlayerEvent(playerPKVoteEvent, roomCode);
            }
        }

    }

    public void simpleVoteWolfAndSeer(int wolf, int seer) {
        for (int i = 1; i < 13; i++) {
            PlayerDisplayInfo displayInfo = service.getPlayerDisplayResult(roomCode, i);
            PlayerSeatInfo playerInfo = displayInfo.getPlayerInfo();
            if (playerInfo.isAlive()) {
                PlayerEvent playerEvent = new PlayerEvent(PlayerEventType.DAYTIME_VOTE, i, playerInfo.getUserID());
                if (Roles.WEREWOLVES.equals(playerInfo.getRole())) {
                    //狼投预言家
                    playerEvent.setDaytimeVoteNumber(seer);
                } else if (Roles.VILLAGER.equals(playerInfo.getRole())) {
                    //民弃票
                    playerEvent.setDaytimeVoteNumber(0);
                } else {
                    //其他投狼
                    playerEvent.setDaytimeVoteNumber(wolf);
                }
                service.resolvePlayerEvent(playerEvent, roomCode);
            }
        }
    }
}
