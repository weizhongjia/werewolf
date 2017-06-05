package com.msh.room.model.room;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.model.room.impl.*;
import org.springframework.stereotype.Component;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
@Component
public class RoomStateFactory {
    public RoomState createRoomInstance(RoomStateData data) {
        switch (data.getStatus()) {
            case VACANCY:
                return new VacancyStateRoom(data);
            case CRATING:
                return new CreatingStateRoom(data);
            case CRATED:
                return new CreatedStateRoom(data);
            case NIGHT:
                return new NightStateRoom(data);
            case DAYTIME:
                return new DaytimeStateRoom(data);
            case VOTING:
                return new VotingStateRoom(data);
            case PK:
                return new PKStateRoom(data);
            case PK_VOTING:
                return new PKVotingStateRoom(data);
            case SHERIFF_REGISTER:
                return new SheriffRegisterStateRoom(data);
            case SHERIFF_RUNNING:
                return new SheriffRunningStateRoom(data);
            case SHERIFF_VOTING:
                return new SheriffVotingStateRoom(data);
            case SHERIFF_PK:
                return new SheriffPkStateRoom(data);
            case SHERIFF_PK_VOTING:
                return new SheriffPkVotingStateRoom(data);
            case SHERIFF_SWITCH_TIME:
                return new SheriffSwitchTimeStateRoom(data);
            case HUNTER_SHOOT:
                return new HunterShootStateRoom(data);
            case MORON_TIME:
                return new MoronTimeState(data);
            case GAME_OVER:
                return new GameOverStateTime(data);
            default:
                return null;
        }
    }
}
