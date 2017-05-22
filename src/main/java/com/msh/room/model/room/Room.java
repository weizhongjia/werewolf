package com.msh.room.model.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonPlayer;
import com.msh.room.model.role.Judge;
import com.msh.room.model.role.PlayerRoleFactory;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class Room {
    private String roomCode;
    private RoomStateDataRepository dataRepository;

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public void setDataRepository(RoomStateDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public Room(String roomCode) {
        this.roomCode = roomCode;
    }

    synchronized public PlayerDisplayInfo resolvePlayerEvent(PlayerEvent event) {
        CommonPlayer commonPlayer = generatePlayerRole(event.getSeatNumber());
        RoomStateData stateData = commonPlayer.resolveEvent(event);
        dataRepository.putRoomStateData(roomCode, stateData);
        //用户角色可能变化
        CommonPlayer newCommonPlayer = generatePlayerRole(event.getSeatNumber());
        return newCommonPlayer.displayInfo();
    }

    private CommonPlayer generatePlayerRole(int seatNumber) {
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        return PlayerRoleFactory.createPlayerInstance(data, seatNumber);
    }

    public JudgeDisplayInfo resolveJudgeEvent(JudgeEvent event) throws RoomBusinessException {
        Judge judge = generateJudgeUser();
        RoomStateData newData = judge.resolveEvent(event);
        dataRepository.putRoomStateData(roomCode, newData);
        return judge.displayInfo();
    }

    private Judge generateJudgeUser() {
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        return new Judge(data);
    }

    public JudgeDisplayInfo getJudgeDisplayResult() {
        Judge judge = generateJudgeUser();
        return judge.displayInfo();
    }


    public PlayerDisplayInfo getPlayerDisplayResult(int seatNumber) {
        CommonPlayer player = generatePlayerRole(seatNumber);
        return player.displayInfo();
    }
}
