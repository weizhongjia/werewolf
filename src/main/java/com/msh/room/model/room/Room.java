package com.msh.room.model.room;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.CommonUser;
import com.msh.room.model.role.JudgeUser;
import com.msh.room.model.role.RoleFactory;

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
        CommonUser commonUser = generatePlayerRole(event);
        RoomStateData stateData = commonUser.resolveEvent(event);
        dataRepository.putRoomStateData(roomCode, stateData);
        return commonUser.displayInfo();
    }

    private CommonUser generatePlayerRole(PlayerEvent event) {
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        return RoleFactory.createPlayerInstance(data, event.getSeatNumber());
    }

    public JudgeDisplayInfo resolveJudgeEvent(JudgeEvent event) throws RoomBusinessException {
        JudgeUser judgeUser = genenrateJudgeUser();

        RoomStateData newData = judgeUser.resolveEvent(event);
        dataRepository.putRoomStateData(roomCode, newData);

        return judgeUser.displayInfo();
    }

    private JudgeUser genenrateJudgeUser() {
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        return new JudgeUser(data);
    }

    public JudgeDisplayInfo getJudgeResult() {
        JudgeUser judgeUser = genenrateJudgeUser();
        return judgeUser.displayInfo();
    }

}
