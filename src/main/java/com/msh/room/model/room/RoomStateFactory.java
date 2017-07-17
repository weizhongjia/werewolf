package com.msh.room.model.room;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.room.impl.*;
import com.msh.room.service.DataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
@Component
public class RoomStateFactory {
    @Autowired
    private DataBaseService dataBaseService;

    public RoomState createRoomInstance(RoomStateData data) {
        RoomState roomState;
        switch (data.getStatus()) {
            case VACANCY:
                roomState = new VacancyStateRoom(data);
                break;
            case CRATING:
                roomState = new CreatingStateRoom(data);
                break;
            case CRATED:
                roomState = new CreatedStateRoom(data);
                break;
            case NIGHT:
                roomState = new NightStateRoom(data);
                break;
            case DAYTIME:
                roomState = new DaytimeStateRoom(data);
                break;
            case VOTING:
                roomState = new VotingStateRoom(data);
                break;
            case PK:
                roomState = new PKStateRoom(data);
                break;
            case PK_VOTING:
                roomState = new PKVotingStateRoom(data);
                break;
            case SHERIFF_REGISTER:
                roomState = new SheriffRegisterStateRoom(data);
                break;
            case SHERIFF_RUNNING:
                roomState = new SheriffRunningStateRoom(data);
                break;
            case SHERIFF_VOTING:
                roomState = new SheriffVotingStateRoom(data);
                break;
            case SHERIFF_PK:
                roomState = new SheriffPkStateRoom(data);
                break;
            case SHERIFF_PK_VOTING:
                roomState = new SheriffPkVotingStateRoom(data);
                break;
            case SHERIFF_SWITCH_TIME:
                roomState = new SheriffSwitchTimeStateRoom(data);
                break;
            case HUNTER_SHOOT:
                roomState = new HunterShootStateRoom(data);
                break;
            case MORON_TIME:
                roomState = new MoronTimeState(data);
                break;
            case WOLF_EXPLODE:
                roomState = new WolfExplodeStateRoom(data);
                break;
            case GAME_OVER:
                roomState = new GameOverStateTime(data);
                break;
            default:
                return null;
        }
        roomState.setDataService(dataBaseService);
        return roomState;
    }

    /**
     * 用于testUnit注入mock
     * @param dataBaseService
     */
    public void setDataBaseService(DataBaseService dataBaseService) {
        this.dataBaseService = dataBaseService;
    }
}
