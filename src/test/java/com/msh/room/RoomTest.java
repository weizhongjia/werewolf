package com.msh.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.response.seat.PlayerSeatInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.Roles;
import com.msh.room.model.room.Room;
import com.msh.room.model.room.RoomManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void testCreateRoom() throws RoomBusinessException {
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
        List<PlayerSeatInfo> seatInfoList = judgeDisplayInfo.getPlayerSeatInfo();
        assertEquals(12, seatInfoList.size());
        for (int i = 0; i < seatInfoList.size(); i++) {
            //每个座位均可用
            assertTrue(seatInfoList.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), seatInfoList.get(i).getSeatNumber());
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
        }

    }

    private JudgeEvent constructCreateRoomEvent(String roomCode) {
        JudgeEvent event = new JudgeEvent(roomCode, JudgeEventType.CREATE_ROOM);
        Map<Roles, Integer> gameConfig = new HashMap<>();
        gameConfig.put(Roles.VILLAGER, 4);
        gameConfig.put(Roles.WEREWOLVE, 4);
        gameConfig.put(Roles.WITCH, 1);
        gameConfig.put(Roles.HUNTER, 1);
        gameConfig.put(Roles.SEER, 1);
        gameConfig.put(Roles.MORON, 1);
        event.setGameConfig(gameConfig);
        return event;
    }

    @Test
    public void testJoinGame() throws RoomBusinessException {
        //创建房间
        JudgeEvent createRoomEvent = constructCreateRoomEvent(roomCode);
        Room room = roomManager.loadRoom(createRoomEvent.getRoomCode());
        room.resolveJudgeEvent(createRoomEvent);
        String userId = "Richard";
        int seatNumber = 1;
        PlayerEvent event = constructPlayerJoinEvent(seatNumber, userId);
        //处理事件
        PlayerDisplayInfo playerDisplayInfo = room.resolvePlayerEvent(event);
        /**
         * 返回值校验
         */
        PlayerSeatInfo playerInfo = playerDisplayInfo.getPlayerInfo();
        assertEquals(Roles.NONE, playerInfo.getRole());
        assertEquals(false, playerInfo.isSeatAvailable());
        assertEquals(true, playerInfo.isAlive());
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
                assertEquals(Roles.NONE, stateDataPlayerSeatInfo.get(i).getRole());
                assertEquals(false, stateDataPlayerSeatInfo.get(i).isSeatAvailable());
                assertEquals(true, stateDataPlayerSeatInfo.get(i).isAlive());
                assertEquals(userId, stateDataPlayerSeatInfo.get(i).getUserID());
                continue;
            }
            //其他每个座位均可用
            assertTrue(stateDataPlayerSeatInfo.get(i).isSeatAvailable());
            //每个座位号均不重复
            assertEquals(Integer.valueOf(i + 1), stateDataPlayerSeatInfo.get(i).getSeatNumber());
        }
    }

    private PlayerEvent constructPlayerJoinEvent(int seatNumber, String userId) {
        PlayerEvent event = new PlayerEvent(seatNumber, userId);
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
