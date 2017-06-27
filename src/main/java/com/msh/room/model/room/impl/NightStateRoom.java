package com.msh.room.model.room.impl;

import com.msh.room.dto.event.JudgeEvent;
import com.msh.room.dto.event.JudgeEventType;
import com.msh.room.dto.event.PlayerEvent;
import com.msh.room.dto.event.PlayerEventType;
import com.msh.room.dto.response.JudgeDisplayInfo;
import com.msh.room.dto.response.PlayerDisplayInfo;
import com.msh.room.dto.room.RoomStateData;
import com.msh.room.dto.room.record.NightRecord;
import com.msh.room.exception.RoomBusinessException;
import com.msh.room.model.role.PlayerRoleFactory;
import com.msh.room.model.role.Roles;
import com.msh.room.model.role.impl.Seer;
import com.msh.room.model.role.impl.Witch;

import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/25.
 */
public class NightStateRoom extends AbstractStateRoom {
    public NightStateRoom(RoomStateData data) {
        super(data);
    }

    @Override
    public RoomStateData resolveJudgeEvent(JudgeEvent event) {
        filterJudgeEventType(event);
        switch (event.getEventType()) {
            case WOLF_KILL:
                resolveWolfKill(event);
                break;
            case SEER_VERIFY:
                resolveSeerVerify(event);
                break;
            case FAKE_SEER_VERIFY:
                resolveFakeSeerVerify(event);
                break;
            case WITCH_SAVE:
                resolveWitchSave(event);
                break;
            case FAKE_WITCH_SAVE:
                resolveFakeWitchSave(event);
                break;
            case WITCH_POISON:
                resolveWitchPoison(event);
                break;
            case FAKE_WITCH_POISON:
                resolveFakeWitchPoison(event);
                break;
            case HUNTER_STATE:
                resolveHunterStateNotify(event);
                break;
            case DAYTIME_COMING:
                resolveDaytimeComing(event);
                break;
            case RESTART_GAME:
                resolveRestartGameEvent(event);
                break;
            case DISBAND_GAME:
                resolveDisbandGameEvent(event);
                break;
        }
        return roomState;
    }

    private void resolveHunterStateNotify(JudgeEvent event) {
        roomState.getLastNightRecord().setHunterNotified(true);
    }


    private void resolveFakeWitchSave(JudgeEvent event) {
        Witch witch = getWitch();
        witch.fakeSave();
    }

    private void resolveFakeWitchPoison(JudgeEvent event) {
        Witch witch = getWitch();
        witch.fakePoison();
    }

    private void resolveWitchPoison(JudgeEvent event) {
        Witch witch = getWitch();
        witch.poison(event.getWitchPoisonNumber());
    }

    private void resolveWitchSave(JudgeEvent event) {
        Witch witch = getWitch();
        witch.save(event.isWitchSave());
    }

    private Witch getWitch() {
        int witchNumber = roomState.getFirstSeatByRole(Roles.WITCH);
        if (witchNumber == 0) {
            throw new RoomBusinessException("女巫不存在，无法救人");
        }
        return (Witch) PlayerRoleFactory.createPlayerInstance(roomState, witchNumber);
    }

    private void resolveSeerVerify(JudgeEvent event) {
        Integer seerVerifyNumber = event.getSeerVerifyNumber();
        int seerNumber = roomState.getAliveSeatByRole(Roles.SEER);
        if (seerNumber > 0) {
            Seer seer = (Seer) PlayerRoleFactory.createPlayerInstance(roomState, seerNumber);
            seer.verify(seerVerifyNumber);
        } else {
            throw new RoomBusinessException("预言家已死或没有预言家，无法验人");
        }
    }

    private void resolveFakeSeerVerify(JudgeEvent event) {
        int seerNumber = roomState.getFirstSeatByRole(Roles.SEER);
        Seer seer = (Seer) PlayerRoleFactory.createPlayerInstance(roomState, seerNumber);
        seer.fakeVerify();
    }

    private void resolveWolfKill(JudgeEvent event) {
        Integer wolfKillNumber = event.getWolfKillNumber();
        if (wolfKillNumber == 0 || roomState.getPlaySeatInfoBySeatNumber(wolfKillNumber).isAlive()) {
            roomState.getLastNightRecord().setWolfKilledSeat(wolfKillNumber);
        } else {
            throw new RoomBusinessException("无法杀掉一个死人");
        }

    }

    @Override
    public JudgeDisplayInfo displayJudgeInfo() {
        JudgeDisplayInfo displayInfo = judgeCommonDisplayInfo();
        /**
         * 夜晚法官需要的动作
         * TODO 逻辑不应该放在此处. 应该是拿到各个角色的acceptableEventType，然后来判断法官这边下一步应该的事件. 角色按GameConfig中顺序来逐个检查
         * TODO 后续前端将夜晚事件做成一体，允许法官整体一次提交。不需要这样做
         */
        NightRecord nightRecord = displayInfo.getNightRecord();
        if (nightRecord.getWolfKilledSeat() == null) {
            //狼刀
            displayInfo.addAcceptableEventType(JudgeEventType.WOLF_KILL);
        } else if (nightRecord.getSeerVerify() == null) {
            //预言家
            int seerSeat = roomState.getAliveSeatByRole(Roles.SEER);
            //预言家已死则假装验人环节
            JudgeEventType type = (seerSeat > 0) ? JudgeEventType.SEER_VERIFY : JudgeEventType.FAKE_SEER_VERIFY;
            displayInfo.addAcceptableEventType(type);
        } else if (nightRecord.getWitchSaved() == null || nightRecord.getWitchPoisoned() == null) {
            int witchNumber = roomState.getAliveSeatByRole(Roles.WITCH);
            //女巫已死或没有女巫
            if (witchNumber <= 0) {
                if (nightRecord.getWitchSaved() == null)
                    displayInfo.addAcceptableEventType(JudgeEventType.FAKE_WITCH_SAVE);
                else if (nightRecord.getWitchPoisoned() == null)
                    displayInfo.addAcceptableEventType(JudgeEventType.FAKE_WITCH_POISON);
            }else{
                Witch witch = (Witch) PlayerRoleFactory.createPlayerInstance(roomState, witchNumber);
                //根据女巫的状态判断
                PlayerDisplayInfo witchDisplayInfo = witch.displayInfo();
                List<PlayerEventType> acceptableEventTypeList = witchDisplayInfo.getAcceptableEventTypeList();
                //女巫有可能已经闭眼
                if (acceptableEventTypeList.size() > 0) {
                    JudgeEventType judgeEventType = JudgeEventType.valueOf(acceptableEventTypeList.get(0).name());
                    displayInfo.addAcceptableEventType(judgeEventType);
                }
            }

        } else if (!nightRecord.isHunterNotified()) {
            //猎人还没问
            displayInfo.addAcceptableEventType(JudgeEventType.HUNTER_STATE);
            nightRecord.setHunterState(calculateHunterState(nightRecord));
        }
        if (nightRecord.getSeerVerify() != null && nightRecord.getWolfKilledSeat() != null
                && nightRecord.getWitchSaved() != null && nightRecord.getWitchPoisoned() != null
                && nightRecord.isHunterNotified()) {
            displayInfo.addAcceptableEventType(JudgeEventType.DAYTIME_COMING);
        }
        displayInfo.addAcceptableEventType(JudgeEventType.RESTART_GAME);
        displayInfo.addAcceptableEventType(JudgeEventType.DISBAND_GAME);
        return displayInfo;
    }

    private boolean calculateHunterState(final NightRecord nightRecord) {
        Integer witchPoisoned = nightRecord.getWitchPoisoned();
        int hunterNumber = roomState.getFirstSeatByRole(Roles.HUNTER);
        return !witchPoisoned.equals(hunterNumber);
    }

    @Override
    public RoomStateData resolvePlayerEvent(PlayerEvent event) {
        filterPlayerEventType(event);
        return roomState;
    }

    @Override
    public PlayerDisplayInfo displayPlayerInfo(int seatNumber) {
        return playerCommonDisplayInfo(seatNumber);
    }
}
