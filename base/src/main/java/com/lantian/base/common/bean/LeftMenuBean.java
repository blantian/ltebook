package com.lantian.base.common.bean;

import java.io.Serializable;

/**
 * Created by Sherlock·Holmes on 2020/5/26
 */
public class LeftMenuBean implements Serializable {

    private String name;
    private int id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
