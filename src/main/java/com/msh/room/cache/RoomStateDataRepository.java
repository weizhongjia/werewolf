package com.msh.room.cache;

import com.msh.room.dto.room.RoomStateData;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
public class RoomStateDataRepository {
    public RoomStateData loadRoomStateData(String roomCode) {
        return null;
    }

    public boolean putRoomStateData(String roomCode, RoomStateData roomStateData) {
        //基于版本号锁，推荐用redis
        return false;
    }
}
