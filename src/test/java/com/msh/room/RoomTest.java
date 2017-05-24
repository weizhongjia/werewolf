package com.msh.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
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
 * Created by zhangruiqian on 2017/5/5.
 */
public class RoomTest {
    private RoomManager roomManager;
    private MockRoomStateDataRepository repository;
    private String roomCode = "abc";

    @Before
    public void setup() {
        repository = new MockRoomStateDataRepository();
        roomManager = new RoomManager();
        roomManager.setDataRepository(repository);
        //房间空闲
        RoomStateData data = new RoomStateData();
        data.setRoomCode(roomCode);
        data.setStatus(RoomStatus.VACANCY);
        repository.data = data;
    }

    @Test
    public void testNone() {
        Room room = roomManager.loadRoom("abc");
        assertEquals("abc", room.getRoomCode());
    }

    @Test
    public void testCreateRoom() {
        JudgeEvent event = constructCreateRoomEvent(roomCode);
        Room room = roomManager.loadRoom(event.getRoomCode());
        //执行事件
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(event);
        /**
         * 返回值校验
         */
        //房间状态
        assertEquals(RoomStatus.CRATING, judgeDisplayInfo.getStatus());
        //座位校验
        List<PlayerSeatInfo> seatInfoList = judgeDisplayInfo.getPlayerSeatInfoList();
        assertEquals(12, seatInfoList.size());
        for (int i = 0; i < seatInfoList.size(); i++) {
            //每个座位均可用
            assertTrue(seatInfoList.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), seatInfoList.get(i).getSeatNumber());
            assertEquals(Roles.NONE, seatInfoList.get(i).getRole());
        }

        /**
         * stateDate数据校验
         */
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        //房间状态
        assertEquals(RoomStatus.CRATING, stateData.getStatus());
        //游戏配置一致
        Map<Roles, Integer> inputGameConfig = event.getGameConfig();
        Map<Roles, Integer> gameConfig = stateData.getGameConfig();
        for (Roles role : inputGameConfig.keySet()) {
            assertEquals(inputGameConfig.get(role), gameConfig.get(role));
        }
        //座位校验
        List<PlayerSeatInfo> stateDataPlayerSeatInfo = stateData.getPlayerSeatInfo();
        assertEquals(12, stateDataPlayerSeatInfo.size());
        for (int i = 0; i < stateDataPlayerSeatInfo.size(); i++) {
            //每个座位均可用
            assertTrue(stateDataPlayerSeatInfo.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), stateDataPlayerSeatInfo.get(i).getSeatNumber());
            assertEquals(Roles.NONE, stateDataPlayerSeatInfo.get(i).getRole());
        }

        /**
         * 普通用户视角
         */
        for (int i = 1; i <= 12; i++) {
            PlayerDisplayInfo playerDisplayResult = room.getPlayerDisplayResult(i);
            assertTrue(playerDisplayResult.getPlayerInfo().isSeatAvailable());
            assertEquals(PlayerEventType.JOIN_ROOM, playerDisplayResult.getAcceptableEventTypeList().get(0));
        }
    }

    private JudgeEvent constructCreateRoomEvent(String roomCode) {
        JudgeEvent event = new JudgeEvent(roomCode, JudgeEventType.CREATE_ROOM);
        Map<Roles, Integer> gameConfig = new HashMap<>();
        gameConfig.put(Roles.VILLAGER, 4);
        gameConfig.put(Roles.WEREWOLVES, 4);
        gameConfig.put(Roles.WITCH, 1);
        gameConfig.put(Roles.HUNTER, 1);
        gameConfig.put(Roles.SEER, 1);
        gameConfig.put(Roles.MORON, 1);
        event.setGameConfig(gameConfig);
        return event;
    }

    @Test
    public void testJoinGame() {
        Room room = createRoom();
        String userId = "Richard";
        int seatNumber = 1;
        PlayerEvent event = constructPlayerJoinEvent(seatNumber, userId);
        //处理事件
        PlayerDisplayInfo playerDisplayInfo = room.resolvePlayerEvent(event);
        /**
         * 返回值校验
         */
        //可接受离开事件
        assertEquals(1, playerDisplayInfo.getAcceptableEventTypeList().size());
        assertEquals(PlayerEventType.Exit_ROOM, playerDisplayInfo.getAcceptableEventTypeList().get(0));

        PlayerSeatInfo playerInfo = playerDisplayInfo.getPlayerInfo();
        assertEquals(Roles.UNASSIGN, playerInfo.getRole());
        assertFalse(playerInfo.isSeatAvailable());
        assertTrue(playerInfo.isAlive());
        assertEquals(userId, playerInfo.getUserID());

        List<PlayerSeatInfo> playerSeatInfoList = playerDisplayInfo.getPlayerSeatInfoList();
        assertEquals(playerInfo, playerSeatInfoList.get(playerInfo.getSeatNumber() - 1));
        for (int i = 0; i < playerSeatInfoList.size(); i++) {
            if (i == (seatNumber - 1)) {
                assertEquals(playerInfo, playerSeatInfoList.get(i));
                continue;
            }
            //其他每个座位均可用
            assertTrue(playerSeatInfoList.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), playerSeatInfoList.get(i).getSeatNumber());
        }
        /**
         * stateData数据校验
         */
        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        List<PlayerSeatInfo> stateDataPlayerSeatInfo = stateData.getPlayerSeatInfo();
        assertEquals(12, stateDataPlayerSeatInfo.size());
        for (int i = 0; i < stateDataPlayerSeatInfo.size(); i++) {
            if (i == (seatNumber - 1)) {
                assertEquals(Roles.UNASSIGN, stateDataPlayerSeatInfo.get(i).getRole());
                assertFalse(stateDataPlayerSeatInfo.get(i).isSeatAvailable());
                assertTrue(stateDataPlayerSeatInfo.get(i).isAlive());
                assertEquals(userId, stateDataPlayerSeatInfo.get(i).getUserID());
                continue;
            }
            //其他每个座位均可用
            assertTrue(stateDataPlayerSeatInfo.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), stateDataPlayerSeatInfo.get(i).getSeatNumber());
        }

        /**
         * 法官界面校验
         */
        JudgeDisplayInfo judgeResult = room.getJudgeDisplayResult();
        PlayerSeatInfo seatInfo = judgeResult.getPlayerSeatInfoList().get(seatNumber - 1);
        assertEquals(Roles.UNASSIGN, seatInfo.getRole());
        assertEquals(userId, seatInfo.getUserID());
        assertFalse(seatInfo.isSeatAvailable());
    }

    private Room createRoom() {
        //创建房间
        JudgeEvent createRoomEvent = constructCreateRoomEvent(roomCode);
        Room room = roomManager.loadRoom(createRoomEvent.getRoomCode());
        room.resolveJudgeEvent(createRoomEvent);
        return room;
    }

    private PlayerEvent constructPlayerJoinEvent(int seatNumber, String userId) {
        PlayerEvent event = new PlayerEvent(PlayerEventType.JOIN_ROOM, seatNumber, userId);
        return event;
    }

    @Test
    public void testExitRoom() {
        int seatNumber = 1;
        String userId = "Richard";
        Room room = createRoom();
        PlayerEvent event = constructPlayerJoinEvent(seatNumber, userId);
        //加入
        room.resolvePlayerEvent(event);

        PlayerEvent exitEvent = constructPlayerExitEvent(seatNumber, userId);
        //处理离开事件
        PlayerDisplayInfo playerDisplayInfo = room.resolvePlayerEvent(exitEvent);

        assertEquals(PlayerEventType.JOIN_ROOM, playerDisplayInfo.getAcceptableEventTypeList().get(0));
        assertTrue(playerDisplayInfo.getPlayerInfo().isSeatAvailable());
        assertEquals(Roles.NONE, playerDisplayInfo.getPlayerInfo().getRole());
        assertNull(playerDisplayInfo.getPlayerInfo().getUserID());
        assertEquals(Integer.valueOf(seatNumber), playerDisplayInfo.getPlayerInfo().getSeatNumber());


        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        PlayerSeatInfo seatInfo = stateData.getPlayerSeatInfo().get(seatNumber - 1);
        assertEquals(playerDisplayInfo.getPlayerInfo(), seatInfo);
    }

    private PlayerEvent constructPlayerExitEvent(int seatNumber, String userId) {
        PlayerEvent event = new PlayerEvent(PlayerEventType.Exit_ROOM, seatNumber, userId);
        return event;
    }

    @Test
    public void testJoinAllPlayers() {
        Room room = createRoom();
        joinAllPlayers(room);
        JudgeDisplayInfo judgeDisplayResult = room.getJudgeDisplayResult();

        assertEquals(JudgeEventType.COMPLETE_CREATE, judgeDisplayResult.getAcceptableEventTypes().get(0));
        judgeDisplayResult.getPlayerSeatInfoList().stream().forEach(seatInfo -> {
            assertTrue(seatInfo.isAlive());
            assertFalse(seatInfo.isSeatAvailable());
            assertEquals(Roles.UNASSIGN, seatInfo.getRole());
        });
    }

    private void joinAllPlayers(Room room) {
        for (int i = 1; i <= 12; i++) {
            int seatNumber = i;
            String userId = "Richard_" + i;
            PlayerEvent event = constructPlayerJoinEvent(seatNumber, userId);
            room.resolvePlayerEvent(event);
        }
    }

    @Test
    public void testCompleteCreateEvent() {
        Room room = createRoom();
        joinAllPlayers(room);
        JudgeEvent event = generateCompleteCreateEvent();
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(event);

        assertEquals(RoomStatus.CRATED, judgeDisplayInfo.getStatus());
        assertEquals(JudgeEventType.NIGHT_COMING, judgeDisplayInfo.getAcceptableEventTypes().get(0));
        List<PlayerSeatInfo> playerSeatInfoList = judgeDisplayInfo.getPlayerSeatInfoList();
        playerSeatInfoList.forEach(seatInfo -> {
            assertTrue(seatInfo.isAlive());
            assertFalse(seatInfo.isSeatAvailable());
            assertNotNull(seatInfo.getUserID());
        });

        long werewolvesCount = playerSeatInfoList.stream().filter(seatInfo -> Roles.WEREWOLVES.equals(seatInfo.getRole())).count();
        long villagerCount = playerSeatInfoList.stream().filter(seatInfo -> Roles.VILLAGER.equals(seatInfo.getRole())).count();
        long seerCount = playerSeatInfoList.stream().filter(seatInfo -> Roles.SEER.equals(seatInfo.getRole())).count();
        long hunterCount = playerSeatInfoList.stream().filter(seatInfo -> Roles.HUNTER.equals(seatInfo.getRole())).count();
        long moronCount = playerSeatInfoList.stream().filter(seatInfo -> Roles.MORON.equals(seatInfo.getRole())).count();

        assertEquals(4, werewolvesCount);
        assertEquals(4, villagerCount);
        assertEquals(1, seerCount);
        assertEquals(1, hunterCount);
        assertEquals(1, moronCount);

        for (int i = 1; i <= 12; i++) {
            PlayerDisplayInfo playerDisplayResult = room.getPlayerDisplayResult(i);
            assertEquals(playerSeatInfoList.get(i - 1).getRole(), playerDisplayResult.getPlayerInfo().getRole());
            assertEquals(playerSeatInfoList.get(i - 1).getRole(), playerDisplayResult.getPlayerSeatInfoList().get(i - 1).getRole());
            long unKnowCount = playerDisplayResult.getPlayerSeatInfoList().stream().filter(seatInfo -> seatInfo.getRole() == null).count();
            if (Roles.WEREWOLVES.equals(playerDisplayResult.getPlayerInfo().getRole())) {
                assertEquals(8, unKnowCount);
            } else {
                assertEquals(11, unKnowCount);
            }
        }


        RoomStateData stateData = repository.loadRoomStateData(roomCode);
        assertTrue(stateData.getWitchState().isAlive());
        assertTrue(stateData.getWitchState().isAntidoteAvailable());
        assertTrue(stateData.getWitchState().isPoisonAvailable());
        assertFalse(stateData.getWitchState().isSaveBySelf());


        assertFalse(stateData.getMoronState().isBeanVoted());
    }

    private JudgeEvent generateCompleteCreateEvent() {
        return new JudgeEvent(roomCode, JudgeEventType.COMPLETE_CREATE);
    }

    @Test
    public void testRestartGameEvent() {
        Room room = createRoom();
        joinAllPlayers(room);
        JudgeEvent event = generateCompleteCreateEvent();
        JudgeDisplayInfo judgeDisplayInfo = room.resolveJudgeEvent(event);

        JudgeEvent restartEvent = generateRestartGame();
        JudgeDisplayInfo judgeDisplayResult = room.resolveJudgeEvent(restartEvent);
        assertEquals(JudgeEventType.COMPLETE_CREATE, judgeDisplayResult.getAcceptableEventTypes().get(0));
        judgeDisplayResult.getPlayerSeatInfoList().stream().forEach(seatInfo -> {
            assertTrue(seatInfo.isAlive());
            assertFalse(seatInfo.isSeatAvailable());
            assertEquals(Roles.UNASSIGN, seatInfo.getRole());
        });
    }

    @Test
    public void testDisbandGameEvent() {
        Room room = createRoom();
        joinAllPlayers(room);
        JudgeEvent event = generateCompleteCreateEvent();
        room.resolveJudgeEvent(event);

        JudgeEvent disbandEvent = generateDisbandGame();
        JudgeDisplayInfo judgeDisplayResult = room.resolveJudgeEvent(disbandEvent);
        assertEquals(JudgeEventType.CREATE_ROOM, judgeDisplayResult.getAcceptableEventTypes().get(0));
        assertEquals(RoomStatus.VACANCY, judgeDisplayResult.getStatus());
        assertNull(judgeDisplayResult.getPlayerSeatInfoList());
    }

    private JudgeEvent generateDisbandGame() {
        JudgeEvent event = new JudgeEvent();
        event.setRoomCode(roomCode);
        event.setEventType(JudgeEventType.DISBAND_GAME);
        return event;
    }

    private JudgeEvent generateRestartGame() {
        JudgeEvent event = new JudgeEvent();
        event.setRoomCode(roomCode);
        event.setEventType(JudgeEventType.RESTART_GAME);
        return event;
    }

    class MockRoomStateDataRepository extends RoomStateDataRepository {
        RoomStateData data;

        @Override
        public RoomStateData loadRoomStateData(String roomCode) {
            return data;
        }

        @Override
        public boolean putRoomStateData(String roomCode, RoomStateData roomStateData) {
            data = roomStateData;
            return true;
        }
    }
}
