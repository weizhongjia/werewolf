package com.msh.room.service;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.cache.RoomStateLockRepository;
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
    @Autowired
    private RoomStateLockRepository lockRepository;

    @Autowired
    private DataBaseService dataBaseService;

    public JudgeDisplayInfo resolveJudgeEvent(JudgeEvent event, String roomCode) {
        //房间锁
        synchronized (lockRepository.getLock(roomCode)) {
            RoomStateData data = dataRepository.loadRoomStateData(roomCode);
            RoomState roomInstance = roomFactory.createRoomInstance(data);
            RoomStateData newData = roomInstance.resolveJudgeEvent(event);
            newData.addVersion();
            dataRepository.putRoomStateData(roomCode, newData);
            //持久化记录Event + 版本号
            if (newData.getGameID() != null) {
                dataBaseService.saveJudgeEvent(newData.getGameID(), event, newData.getVersion());
            }
            return getJudgeDisplayResult(roomCode);
        }
    }


    public JudgeDisplayInfo getJudgeDisplayResult(String roomCode) {
        RoomStateData data = dataRepository.loadRoomStateData(roomCode);
        RoomState roomInstance = roomFactory.createRoomInstance(data);
        return roomInstance.displayJudgeInfo();
    }

    public PlayerDisplayInfo resolvePlayerEvent(PlayerEvent event, String roomCode) {
        //房间锁
        synchronized (lockRepository.getLock(roomCode)) {
            RoomStateData data = dataRepository.loadRoomStateData(roomCode);
            RoomState roomInstance = roomFactory.createRoomInstance(data);
            RoomStateData newData = roomInstance.resolvePlayerEvent(event);
            newData.addVersion();
            dataRepository.putRoomStateData(roomCode, newData);
            //持久化记录Event + 版本号
            if (newData.getGameID() != null) {
                dataBaseService.savePlayerEvent(newData.getGameID(), event, newData.getVersion());
            }
            return getPlayerDisplayResult(roomCode, event.getSeatNumber());
        }
    }

    public PlayerDisplayInfo getPlayerDisplayResult(String roomCode, int seatNumber) {
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

    public void setLockRepository(RoomStateLockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    public void setDataBaseService(DataBaseService dataBaseService) {
        this.dataBaseService = dataBaseService;
    }
}
