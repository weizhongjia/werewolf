package com.msh.room.dto.room.record;

import java.util.List;

/**
 * Created by zhangruiqian on 2017/5/14.
 */
public class NightRecord {
    private Integer wolfKilledSeat;
    //0表示没有操作 null表示还没有处理事件
    private Integer witchSaved;
    //0表示没有操作 null表示还没有处理事件
    private Integer witchPoisoned;
    private Integer seerVerify;
    //true 狼人 false 好人
    private boolean seerVerifyResult;
    //死者座位号，0表示无人死亡
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
}
