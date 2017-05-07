package com.msh.room.model.room;

import com.msh.room.cache.RoomStateDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
@Component
public class RoomManager {
    @Autowired
    private RoomStateDataRepository dataRepository;

    private Map<String, Room> roomCache = new HashMap<>();

    public void setDataRepository(RoomStateDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public RoomManager() {
    }

    public Room loadRoom(String roomCode) {
        //此处加锁或缓存,保证相同roomCode仅有一个对象实例
        Room room = roomCache.get(roomCode);

        if (room == null) {
            room = new Room(roomCode);
            room.setDataRepository(dataRepository);
            roomCache.put(roomCode, room);
        }
        return room;
    }
}
