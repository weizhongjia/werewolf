package com.msh.room.dto.room.record;

import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
//TODO 这个结构不好，应该是Map<Roles:<PlayerEventType:Object>>,这样能满足以后角色的扩展
public class NightRecord {
    private Integer wolfKilledSeat;
    //0表示没有操作 null表示还没有处理事件
    private Integer witchSaved;
    //0表示没有操作 null表示还没有处理事件
    private Integer witchPoisoned;
    private Integer seerVerify;
    //true 狼人 false 好人
    private boolean seerVerifyResult;
    //猎人开枪状态
    private boolean hunterState;
    //猎人状态已通知
    private boolean hunterNotified;

    //死者座位号
    // 此信息为夜晚信息，前端请根据此信息的值来判断是否需要展示昨夜死亡信息 (null表示没有信息，数组为空表示平安夜)
    private List<Integer> diedNumber;

    public Integer getWolfKilledSeat() {
        return wolfKilledSeat;
    }

    public void setWolfKilledSeat(Integer wolfKilledSeat) {
        this.wolfKilledSeat = wolfKilledSeat;
    }

    public Integer getWitchSaved() {
        return witchSaved;
    }

    public void setWitchSaved(Integer witchSaved) {
        this.witchSaved = witchSaved;
    }

    public Integer getWitchPoisoned() {
        return witchPoisoned;
    }

    public void setWitchPoisoned(Integer witchPoisoned) {
        this.witchPoisoned = witchPoisoned;
    }

    public Integer getSeerVerify() {
        return seerVerify;
    }

    public void setSeerVerify(Integer seerVerify) {
        this.seerVerify = seerVerify;
    }

    public boolean isSeerVerifyResult() {
        return seerVerifyResult;
    }

    public void setSeerVerifyResult(boolean seerVerifyResult) {
        this.seerVerifyResult = seerVerifyResult;
    }

    public List<Integer> getDiedNumber() {
        return diedNumber;
    }

    public void setDiedNumber(List<Integer> diedNumber) {
        this.diedNumber = diedNumber;
    }

    public boolean isHunterState() {
        return hunterState;
    }

    public void setHunterState(boolean hunterState) {
        this.hunterState = hunterState;
    }

    public boolean isHunterNotified() {
        return hunterNotified;
    }

    public void setHunterNotified(boolean hunterNotified) {
        this.hunterNotified = hunterNotified;
    }
}
