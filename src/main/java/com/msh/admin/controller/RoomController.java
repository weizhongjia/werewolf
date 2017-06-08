package com.msh.admin.controller;

import com.msh.admin.service.ClubRoomService;
import com.msh.common.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "werewolf/admin")
public class RoomController {

    @Autowired
    private ClubRoomService clubRoomService;

    @RequestMapping(value = "room", method = RequestMethod.GET)
    public List<Room> getRooms (@RequestParam int p, @RequestParam int ps) {
        return clubRoomService.getRooms(p, ps);
    }

    @RequestMapping(value = "room", method = RequestMethod.PUT)
    public ResponseEntity<String> editRoom (@RequestBody Room room) {
        clubRoomService.editRoom(room);
        return ResponseEntity.ok("编辑成功");
    }

}
