package com.msh.admin.controller;

import com.msh.admin.service.ClubService;
import com.msh.common.model.Club;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "werewolf/admin")
public class ClubController {

    @Autowired
    private ClubService clubService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Authorization token",
                    required = true, dataType = "string", paramType = "header")
    })
    @RequestMapping(value = "club", method = RequestMethod.GET)
    public List<Club> getClubs () {
        return clubService.getClubs();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "Authorization token",
                    required = true, dataType = "string", paramType = "header")
    })
    @RequestMapping(value = "club", method = RequestMethod.PUT)
    public ResponseEntity<String> editClub (@RequestBody Club club) {
        clubService.editClub(club);
        return ResponseEntity.ok("编辑成功");
    }
    @RequestMapping(value = "club/_get", method = RequestMethod.GET)
    public Club getClub (@RequestParam String id) {
        return clubService.getClub(id);
    }

}
