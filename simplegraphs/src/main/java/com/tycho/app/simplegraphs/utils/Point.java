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
    private double x;

    /**
     * Y-coordinate of this point.
     */
    private double y;

    /**
     * Create a point at (x, y).
     *
     * @param x The X-coordinate of this point.
     * @param y The Y-coordinate of this point.
     */
    public Point(double x, double y){
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
        return (float) x;
    }

    public float getY(){
        return (float) y;
    }
}
