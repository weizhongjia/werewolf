package com.msh.room.cache;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
@Component
public class RoomStateDataRepository {
    //TODO 目前仅为测试mock，需要修改至redis或者其他缓存服务
    Map<String, RoomStateData> cache = new HashMap<>();

    public RoomStateData loadRoomStateData(String roomCode) {
        RoomStateData stateData = cache.get(roomCode);
        if (stateData == null) {
            stateData = new RoomStateData();
            stateData.setRoomCode(roomCode);
            stateData.setStatus(RoomStatus.VACANCY);
            cache.put(roomCode, stateData);
        }
        return stateData;
    }

    public boolean putRoomStateData(String roomCode, RoomStateData roomStateData) {
        //TODO 基于版本号锁，推荐用redis
        cache.put(roomCode, roomStateData);
        return true;
    }
}
