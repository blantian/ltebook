package com.lantian.base.utils;

import android.util.Log;

public class LogUtils {

    private static final boolean DEBUG = true;
    /**
     * 获取当前类名
     * @return
     */
    public static String getClassName(){
        StackTraceElement stackTraceElement = (new Exception()).getStackTrace()[2];
        String result = stackTraceElement.getClassName();
        int lastInsex = result.lastIndexOf(".");
        result =result.substring(lastInsex + 1,result.length());
        return result;
    }

    public static void  w(String logString){
        if (DEBUG){
            Log.w(getClassName(),logString);
        }
    }

    /**
     * debug log
     * @param tag
     * @param msg
     */
    public static void d(String tag,String msg){
        if (DEBUG){
            Log.d(tag, msg);
        }
    }


    /**
     * erro log
     * @param tag
     * @param msg
     */
    public static void e(String tag,String msg){
        if (DEBUG){
            Log.e(tag, msg);
        }
    }

    /**
     * debug log
     *
     * @param msg
     */
    public static void d(String msg){
        if (DEBUG){
            Log.d(getClassName(),msg);
        }
    }

    /**
     * debug log
     *
     * @param msg
     */
    public static void i(String msg){
        if (DEBUG){
            Log.i(getClassName(),msg);
        }
    }


    /**
     * erro log
     *
     * @param msg
     */
    public static void e(String msg){
        if (DEBUG){
            Log.e(getClassName(),msg);
        }
    }

    public static void i(String tag,String logString){
        if (DEBUG){
            Log.i(tag,logString);
        }
    }

    public static void w(String tag,String logString){
        if (DEBUG){
            Log.w(tag,logString);
        }
    }

}
