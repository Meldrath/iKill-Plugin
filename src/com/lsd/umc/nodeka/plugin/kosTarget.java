/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lsd.umc.nodeka.plugin;

/**
 *
 * @author Leviticus
 */
public class kosTarget {

    private int priority;
    private final String target;
    private String attack;

    kosTarget(String t, String a) {
        this.attack = a;
        this.target = t;
        this.priority = 50;
    }

    kosTarget(String t, String a, int p) {
        this.attack = a;
        this.target = t;
        this.priority = p;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return the attack
     */
    public String getAttack() {
        return attack;
    }

    /**
     * @param attack the attack to set
     */
    public void setAttack(String attack) {
        this.attack = attack;
    }
}
