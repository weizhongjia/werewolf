package com.msh.room.cache;

import com.msh.room.dto.room.RoomStateData;
import org.springframework.stereotype.Component;


/**
 * Created by zhangruiqian on 2017/5/5.
 */
@Component
public class RoomStateDataRepository {
    public RoomStateData loadRoomStateData(String roomCode) {
        return null;
    }

    public boolean putRoomStateData(String roomCode, RoomStateData roomStateData) {
        //基于版本号锁，推荐用redis
        return false;
    }
}
