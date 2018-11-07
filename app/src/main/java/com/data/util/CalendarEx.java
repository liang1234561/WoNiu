package com.data.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarEx extends GregorianCalendar {
    private static final long serialVersionUID = -5456051407740208351L;
    private static long servertimespan = 0;
    public static final long HOURS_MILLISECONDS = 60 * 60 * 1000l;
    public static final long DAY_MILLISECONDS = 24 * HOURS_MILLISECONDS;
    public static final SimpleDateFormat FMT_YYMDHMSS = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:sss");// db中存放Date类型数据格式
    public static final SimpleDateFormat FMT_YYMDHMS = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");// db中存放Date类型数据格式
    public static final SimpleDateFormat FMT_YYMDHM = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");// db中存放Date类型数据格式
    public static final SimpleDateFormat FMT_YMD = new SimpleDateFormat(
            "yy/MM/dd");
    public static final SimpleDateFormat FMT_YMD_ = new SimpleDateFormat(
            "yy-MM-dd");
    public static final SimpleDateFormat FMT_YMDHM = new SimpleDateFormat(
            "yy-MM-dd HH:mm");// db中存放Date类型数据格式

    public static final SimpleDateFormat SERVER_TIME = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");// db中存放Date类型数据格式

    public CalendarEx() {
        super();
    }

    public CalendarEx(Calendar other) {
        super(other.getTimeZone());

    }

    public static CalendarEx getInstance(long millis) {
        CalendarEx calendarEx = new CalendarEx();
        calendarEx.setTimeInMillis(millis);
        return calendarEx;
    }

    public static CalendarEx getInstance() {
        return new CalendarEx();
    }

    /**
     * 根据时间差，算出当前的服务器时间 毫秒表示
     *
     * @return
     */
    public static long getServerTimeMillis() {
        return System.currentTimeMillis() + servertimespan;
    }

    /**
     * 根据时间差，算出当前的服务器时间
     *
     * @return
     */
    public static CalendarEx getServerTimeInstance() {
        CalendarEx ins = getInstance();
        ins.setTimeInMillis(getServerTimeMillis());
        return ins;
    }

    public static void updateServerTime(long miliis) {
        servertimespan = miliis - System.currentTimeMillis();
//		LogUtil.info("System","日期更新: 秒差:"+servertimespan/1000);
    }

    public static long getServertimespan() {
        return servertimespan;
    }

    public static int Nano2MillisTime(long nano) {
        return (int) (nano / 1000000);
    }

    @Override
    public String toString() {
        return FMT_YYMDHMSS.format(getTimeInMillis());
    }

    /*
     * 目前主要用于将db中获取出来的date类型字段转换成秒值
     */
    public static long parse(String tasktime) {
        try {
            return FMT_YYMDHMSS.parse(tasktime).getTime();
        } catch (ParseException e) {
        }
        return 0;
    }

    public static String formatDate4Network(CalendarEx date) {
        return FMT_YYMDHMSS.format(date.getTime());
    }

    public static String format(long millis) {
        return FMT_YYMDHMSS.format(millis);
    }

    public static String setFormatMonitor(int hourPos, String minPos) {
        return hourPos + ":" + minPos;
    }
}
