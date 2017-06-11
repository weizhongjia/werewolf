package com.msh.room.service;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.room.RoomState;
import com.msh.room.model.room.RoomStateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
@Component
public class WereWolfRoomService {
    @Autowired
    private RoomStateDataRepository dataRepository;
    @Autowired
    private RoomStateFactory roomFactory;

    public JudgeDisplayInfo resolveJudgeEvent(JudgeEvent event, String roomCode) {
        //TODO 此处需要锁数据
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        RoomState roomInstance = roomFactory.createRoomInstance(data);
        RoomStateData newData = roomInstance.resolveJudgeEvent(event);
        //TODO 此处需要释放锁
        dataRepository.putRoomStateData(roomCode, newData);
        return getJudgeDisplayResult(roomCode);
    }


    public JudgeDisplayInfo getJudgeDisplayResult(String roomCode) {
        //此处不需要锁
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        RoomState roomInstance = roomFactory.createRoomInstance(data);
        return roomInstance.displayJudgeInfo();
    }

    public PlayerDisplayInfo resolvePlayerEvent(PlayerEvent event, String roomCode) {
        //TODO 此处需要锁数据
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        RoomState roomInstance = roomFactory.createRoomInstance(data);
        RoomStateData newData = roomInstance.resolvePlayerEvent(event);
        //TODO 此处需要释放锁
        dataRepository.putRoomStateData(roomCode, newData);
        return getPlayerDisplayResult(roomCode, event.getSeatNumber());
    }

    public PlayerDisplayInfo getPlayerDisplayResult(String roomCode, int seatNumber) {
        //此处不需要锁
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        RoomState roomInstance = roomFactory.createRoomInstance(data);
        return roomInstance.displayPlayerInfo(seatNumber);
    }

    public void setDataRepository(RoomStateDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void setRoomFactory(RoomStateFactory roomFactory) {
        this.roomFactory = roomFactory;
    }
}
