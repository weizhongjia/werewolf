package com.msh.admin.service;

import com.msh.common.mapper.ClubMapper;
import com.msh.common.mapper.RoomMapper;
import com.msh.common.model.Club;
import com.msh.common.model.Room;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubService {

    @Autowired
    private ClubMapper clubMapper;

    public List<Club> getClubs () {
        return clubMapper.selectAll();
    }

    public void editClub (Club club) {
        if (club.getId() > 0) {
            clubMapper.updateByPrimaryKey(club);
        } else {
            clubMapper.insert(club);
        }

    }

    public Club getClub(String id){
        Club club = new Club();
        club.setId(Long.parseLong(id));
        return clubMapper.selectByPrimaryKey(club);
    }
}
