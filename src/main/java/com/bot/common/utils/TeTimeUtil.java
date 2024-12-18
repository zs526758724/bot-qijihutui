package com.bot.common.utils;

import cn.hutool.core.date.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TeTimeUtil {
    //获取当前时间
    public static String getNowTime() {
        return DateUtil.format(DateUtil.date(), "yyyy-MM-dd");
    }

    /**
     * yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String getNowTimeDetail() {
        return DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * <p>获取从当天开始往前的days天日期字符串集合
     * <br>e.g days=5,
     * <br>return [2024-05-27, 2024-05-26, 2024-05-25, 2024-05-24, 2024-05-23]
     * </p>
     *
     * @param days
     * @return
     */
    public static List<String> getBeforeDays(int days) {
        List<String> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new java.util.Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateList.add(sdf.format(date));
        for (int i = 0; i < days - 1; i++) {
            calendar.add(Calendar.DATE, -1);
            date = calendar.getTime();
            dateList.add(sdf.format(date));
        }
        return dateList;
    }


}
