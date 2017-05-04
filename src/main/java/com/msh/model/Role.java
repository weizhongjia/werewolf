package com.msh.model;

import javax.persistence.*;

public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer uid;

    private String role;

    @Column(name = "player_no")
    private Integer playerNo;

    @Column(name = "player_state")
    private Integer playerState;

    private Integer inserttime;

    private Integer overtime;

    private Integer overround;

    @Column(name = "player_result")
    private Integer playerResult;

    /**
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return uid
     */
    public Integer getUid() {
        return uid;
    }

    /**
     * @param uid
     */
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * @return role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return player_no
     */
    public Integer getPlayerNo() {
        return playerNo;
    }

    /**
     * @param playerNo
     */
    public void setPlayerNo(Integer playerNo) {
        this.playerNo = playerNo;
    }

    /**
     * @return player_state
     */
    public Integer getPlayerState() {
        return playerState;
    }

    /**
     * @param playerState
     */
    public void setPlayerState(Integer playerState) {
        this.playerState = playerState;
    }

    /**
     * @return inserttime
     */
    public Integer getInserttime() {
        return inserttime;
    }

    /**
     * @param inserttime
     */
    public void setInserttime(Integer inserttime) {
        this.inserttime = inserttime;
    }

    /**
     * @return overtime
     */
    public Integer getOvertime() {
        return overtime;
    }

    /**
     * @param overtime
     */
    public void setOvertime(Integer overtime) {
        this.overtime = overtime;
    }

    /**
     * @return overround
     */
    public Integer getOverround() {
        return overround;
    }

    /**
     * @param overround
     */
    public void setOverround(Integer overround) {
        this.overround = overround;
    }

    /**
     * @return player_result
     */
    public Integer getPlayerResult() {
        return playerResult;
    }

    /**
     * @param playerResult
     */
    public void setPlayerResult(Integer playerResult) {
        this.playerResult = playerResult;
    }
}