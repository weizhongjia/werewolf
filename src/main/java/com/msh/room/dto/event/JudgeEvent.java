package com.msh.room.dto.event;

import com.msh.room.model.role.Roles;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Created by zhangruiqian on 2017/5/3.
 */
public class JudgeEvent {
    private String roomCode;
    private JudgeEventType eventType;
    private Map<Roles, Integer> gameConfig;
    private boolean sheriffSwitch = false;

    private Integer wolfKillNumber;
    private Integer seerVerifyNumber;

    //true是救人，false是不救
    private boolean witchSave;
    //女巫毒人号码
    private Integer witchPoisonNumber;
    //如果由法官操作竞选者，可以使用SheriffRunning事件包装该数据
    private List<Integer> sheriffApplyList;
    //猎人开枪
    private Integer hunterShoot;
    //警徽移交
    private Integer sheriffSwitchNumber;

    //自爆狼人
    private Integer explodeWereWolf;

    //白痴是否翻牌
    private Boolean moronShow;

    //默认构造方法
    public JudgeEvent() {
    }

    public JudgeEvent(String roomCode, JudgeEventType eventType) {
        this.roomCode = roomCode;
        this.eventType = eventType;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public JudgeEventType getEventType() {
        return eventType;
    }

    public void setEventType(JudgeEventType eventType) {
        this.eventType = eventType;
    }

    public Map<Roles, Integer> getGameConfig() {
        return gameConfig;
    }

    public void setGameConfig(Map<Roles, Integer> gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Integer getWolfKillNumber() {
        return wolfKillNumber;
    }

    public void setWolfKillNumber(Integer wolfKillNumber) {
        this.wolfKillNumber = wolfKillNumber;
    }

    public Integer getSeerVerifyNumber() {
        return seerVerifyNumber;
    }

    public void setSeerVerifyNumber(Integer seerVerifyNumber) {
        this.seerVerifyNumber = seerVerifyNumber;
    }

    public boolean isWitchSave() {
        return witchSave;
    }

    public void setWitchSave(boolean witchSave) {
        this.witchSave = witchSave;
    }

    public Integer getWitchPoisonNumber() {
        return witchPoisonNumber;
    }

    public void setWitchPoisonNumber(Integer witchPoisonNumber) {
        this.witchPoisonNumber = witchPoisonNumber;
    }

    public boolean isSheriffSwitch() {
        return sheriffSwitch;
    }

    public void setSheriffSwitch(boolean sheriffSwitch) {
        this.sheriffSwitch = sheriffSwitch;
    }

    public List<Integer> getSheriffApplyList() {
        return sheriffApplyList;
    }

    public void setSheriffApplyList(List<Integer> sheriffApplyList) {
        this.sheriffApplyList = sheriffApplyList;
    }

    public Integer getHunterShoot() {
        return hunterShoot;
    }

    public void setHunterShoot(Integer hunterShoot) {
        this.hunterShoot = hunterShoot;
    }

    public Integer getSheriffSwitchNumber() {
        return sheriffSwitchNumber;
    }

    public void setSheriffSwitchNumber(Integer sheriffSwitchNumber) {
        this.sheriffSwitchNumber = sheriffSwitchNumber;
    }

    public Integer getExplodeWereWolf() {
        return explodeWereWolf;
    }

    public void setExplodeWereWolf(Integer explodeWereWolf) {
        this.explodeWereWolf = explodeWereWolf;
    }

    public Boolean getMoronShow() {
        return moronShow;
    }

    public void setMoronShow(Boolean moronShow) {
        this.moronShow = moronShow;
    }
}
