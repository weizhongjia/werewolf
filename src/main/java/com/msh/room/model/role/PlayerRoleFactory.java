package com.msh.room.model.role;

import com.msh.room.dto.room.RoomStateData;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.impl.*;

/**
 * Created by zhangruiqian on 2017/5/4.
 */
public class PlayerRoleFactory {
    public static CommonPlayer createPlayerInstance(RoomStateData roomState, int number) {
        if (number > 0 && number <= roomState.getPlayerSeatInfo().size()) {
            switch (roomState.getPlaySeatInfoBySeatNumber(number).getRole()) {
                case NONE:
                    return new NonePlayer(roomState, number);
                case UNASSIGN:
                    return new UnAssignPlayer(roomState, number);
                case WEREWOLVES:
                    return new Werewolves(roomState, number);
                case VILLAGER:
                    return new Villagers(roomState, number);
                case SEER:
                    return new Seer(roomState, number);
                case WITCH:
                    return new Witch(roomState, number);
                case HUNTER:
                    return new Hunter(roomState, number);
                case MORON:
                    return new Moron(roomState, number);
                default:
                    throw new RoomBusinessException("不存在的角色");

            }
        } else {
            throw new RoomBusinessException("错误的座位信息");
        }
    }
}
