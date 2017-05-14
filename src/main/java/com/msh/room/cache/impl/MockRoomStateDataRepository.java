package com.msh.room.cache.impl;

import com.msh.room.cache.RoomStateDataRepository;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by weizhongjia on 2017/5/14.
 */
public class MockRoomStateDataRepository extends RoomStateDataRepository{

    private RoomStateData roomStateData;

    private static final String roomCode = "test";

    @PostConstruct
    public void initRoomState(){
        roomStateData = new RoomStateData();
        roomStateData.setRoomCode(roomCode);
        roomStateData.setStatus(RoomStatus.VACANCY);
    }

    @Override
    public RoomStateData loadRoomStateData(String roomCode) {
        return roomStateData;
    }

    @Override
    public boolean putRoomStateData(String roomCode, RoomStateData roomStateData) {
        return false;
    }
}
