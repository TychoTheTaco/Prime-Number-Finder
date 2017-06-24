package com.tycho.app.simplegraphs.utils;

/**
 * Representation of a point in 2D space.
 *
 * @author Tycho Bellers
 *         Date Created: 2/15/2017
 */
public class Point{

    /**
     * X-coordinate of this point.
     */
    private float x;

    /**
     * Y-coordinate of this point.
     */
    private float y;

    /**
     * Create a point at (x, y).
     *
     * @param x The X-coordinate of this point.
     * @param y The Y-coordinate of this point.
     */
    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }

    //Override methods

    @Override
    public String toString(){
        return "(" + x + ", " + y + ")";
    }

    //Getters and setters

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }
}
