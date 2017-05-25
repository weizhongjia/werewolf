package com.msh.admin.controller;

import com.msh.admin.service.ClubService;
import com.msh.admin.service.RoomService;
import com.msh.common.model.Club;
import com.msh.common.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "werewolf/admin")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @RequestMapping(value = "club", method = RequestMethod.GET)
    public List<Club> getClubs (@RequestParam int p, @RequestParam int ps) {
        return clubService.getClubs(p, ps);
    }

    @RequestMapping(value = "club", method = RequestMethod.PUT)
    public ResponseEntity<String> editClub (@RequestBody Club club) {
        clubService.editClub(club);
        return ResponseEntity.ok("编辑成功");
    }

}
