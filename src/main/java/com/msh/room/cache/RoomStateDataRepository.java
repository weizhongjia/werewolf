package com.msh.room.cache;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.RoomStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangruiqian on 2017/5/5.
 */
@Component
public class RoomStateDataRepository {
    //TODO 需要修改至redis或者其他缓存服务
    Map<String, RoomStateData> cache = new ConcurrentHashMap<>();

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
        cache.put(roomCode, roomStateData);
        return true;
    }
}
