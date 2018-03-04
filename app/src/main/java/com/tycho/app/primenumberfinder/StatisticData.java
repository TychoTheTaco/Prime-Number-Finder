package com.tycho.app.primenumberfinder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is a custom implementation of {@link JSONObject}. This object is used for storing
 * statistics for each task.
 *
 * @author Tycho Bellers
 *         Date Created: 5/19/2017
 */
public class StatisticData extends JSONObject{

    public StatisticData(){
        super();
    }

    public StatisticData(String json) throws JSONException{
        super(json);
    }

    public void put(Statistic statistic, boolean value){
        try {
            super.put(statistic.getKey(), value);
        }catch (JSONException e){}
    }

    public void put(Statistic statistic, double value){
        try {
            super.put(statistic.getKey(), value);
        }catch (JSONException e){}
    }

    public void put(Statistic statistic, int value){
        try {
            super.put(statistic.getKey(), value);
        }catch (JSONException e){}
    }

    public void put(Statistic statistic, long value){
        try {
            super.put(statistic.getKey(), value);
        }catch (JSONException e){}
    }

    public void put(Statistic statistic, Object value){
        try {
            super.put(statistic.getKey(), value);
        }catch (JSONException e){}
    }

    public boolean optBoolean(Statistic statistic){
        return super.optBoolean(statistic.getKey());
    }

    public double optDouble(Statistic statistic){
        return super.optDouble(statistic.getKey());
    }

    public int optInt(Statistic statistic){
        return super.optInt(statistic.getKey());
    }

    public long optLong(Statistic statistic){
        return super.optLong(statistic.getKey());
    }

    public Object opt(Statistic statistic){
        return super.opt(statistic.getKey());
    }

    public boolean getBoolean(Statistic statistic) throws JSONException{
        return super.getBoolean(statistic.getKey());
    }

    public double getDouble(Statistic statistic) throws JSONException{
        return super.getDouble(statistic.getKey());
    }

    public int getInt(Statistic statistic) throws JSONException{
        return super.getInt(statistic.getKey());
    }

    public long getLong(Statistic statistic) throws JSONException{
        return super.getLong(statistic.getKey());
    }

    public Object get(Statistic statistic) throws JSONException{
        return super.get(statistic.getKey());
    }
}
