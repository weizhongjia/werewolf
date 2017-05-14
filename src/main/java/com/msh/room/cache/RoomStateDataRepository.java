package com.msh.room.cache;

import com.msh.room.dto.room.RoomStateData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


/**
 * Created by zhangruiqian on 2017/5/5.
 */

public interface RoomStateDataRepository {
    public RoomStateData loadRoomStateData(String roomCode);

    public boolean putRoomStateData(String roomCode, RoomStateData roomStateData);
}
