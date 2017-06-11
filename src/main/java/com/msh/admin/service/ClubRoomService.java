package com.msh.admin.service;

import com.msh.common.mapper.RoomMapper;
import com.msh.common.model.Room;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubRoomService {

    @Autowired
    private RoomMapper roomMapper;

    public List<Room> getRooms (int p, int ps) {
        RowBounds rowBounds = new RowBounds(p * ps, ps);
        return roomMapper.selectAll();
    }

    public void editRoom (Room room) {
        if (room.getId() > 0) {
            roomMapper.updateByPrimaryKey(room);
        } else {
            roomMapper.insert(room);
        }

    }
}
