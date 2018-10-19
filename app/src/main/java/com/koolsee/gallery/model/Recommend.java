package com.koolsee.gallery.model;

/**
 * 推荐model
 * @author zengxiaotao
 */
public class Recommend {

    /**
     * 资源id
     */
    private int res;

    /**
     * 名字
     */
    private String name;

    public Recommend() {
    }

    public Recommend(int res, String name) {
        super();
        this.res = res;
        this.name = name;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
