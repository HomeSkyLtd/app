package com.homesky.homesky.utils;

import android.content.Context;
import com.homesky.homesky.R;

import com.homesky.homecloud_lib.model.enums.CommandCategoryEnum;
import com.homesky.homecloud_lib.model.enums.DataCategoryEnum;
import com.homesky.homecloud_lib.model.enums.OperatorEnum;

public class AppEnumUtils {

    public static String operatorToString(Context c, OperatorEnum op){
        switch (op){
            case EQ:
                return c.getString(R.string.EQ);
            case NE:
                return c.getString(R.string.NE);
            case GT:
                return c.getString(R.string.GT);
            case GE:
                return c.getString(R.string.GE);
            case LT:
                return c.getString(R.string.LT);
            case LE:
                return c.getString(R.string.LE);
        }
        return "Not found";
    }

    public static OperatorEnum stringToOperator(Context c, String s){

        if(s.equals(c.getString(R.string.EQ)))
            return OperatorEnum.EQ;
        else if(s.equals(c.getString(R.string.NE)))
            return OperatorEnum.NE;
        else if(s.equals(c.getString(R.string.GT)))
            return OperatorEnum.GT;
        else if(s.equals(c.getString(R.string.GE)))
            return OperatorEnum.GE;
        else if(s.equals(c.getString(R.string.LT)))
            return OperatorEnum.LT;
        else if(s.equals(c.getString(R.string.LE)))
            return OperatorEnum.LE;
        else
            return null;
    }

    public static String dataCategoryToString(Context c, DataCategoryEnum dc){
        switch (dc){
            case HUMIDITY:
                return c.getString(R.string.HUMIDITY);
            case LUMINANCE:
                return c.getString(R.string.LUMINANCE);
            case PRESSURE:
                return c.getString(R.string.PRESSURE);
            case PRESENCE:
                return c.getString(R.string.PRESENCE);
            case SMOKE:
                return c.getString(R.string.SMOKE);
            case TEMPERATURE:
                return c.getString(R.string.TEMPERATURE);
            case WIND_SPEED:
                return c.getString(R.string.WIND_SPEED);
        }
        return "Not found";
    }

    public static String commandCategoryToString(Context c, CommandCategoryEnum cc) {
        switch (cc) {
            case AC_MODE:
                return c.getString(R.string.AC_MODE);
            case FAN:
                return c.getString(R.string.FAN);
            case LIGHT_COLOR:
                return c.getString(R.string.LIGHT_COLOR);
            case LIGHT_INTENSITY:
                return c.getString(R.string.LIGHT_INTENSITY);
            case LIGHT_SWITCH:
                return c.getString(R.string.LIGHT_SWITCH);
            case TEMPERATURE:
                return c.getString(R.string.TEMPERATURE);
            case TOGGLE:
                return c.getString(R.string.TOGGLE);
        }
        return "Not found";
    }
}
