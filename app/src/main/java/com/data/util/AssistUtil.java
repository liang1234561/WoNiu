package com.data.util;

import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssistUtil {
    private static final int MAX = 99999999;
    private static final int MIN = 10000000;
    public static final String PHONE_EXP = "^1[0-9]{10}$";
    public static final String MATHER_USER_NAME = "[A-Za-z0-9_-]+";
    public static final String MATHER_CHINA = "^[\u4E00-\u9FA5]+$";//^[a-zA-Z0-9\u4E00-\u9FA5]+$
    public static final String MATHER_NUM_CHINA_ENGLISH = "^[a-zA-Z0-9\u4e00-\u9fa5]+$";


    /**
     * 获取一个随机数
     *
     * @return
     */
    public static int getRandom() {
        Random random = new Random();
        return random.nextInt(MAX) % (MAX - MIN + 1) + MIN;
    }

    /**
     * 获取当前天月一年的唯一数
     * @return
     */
    public static String getNowDay() {
        Calendar cal = Calendar.getInstance();
        int a = cal.get(Calendar.MONTH);
        int b = cal.get(Calendar.DATE);
        return a + (b < 10 ? String.valueOf(0) + b : String.valueOf(b));
    }

    /**
     * 半角转换为全角
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

    /**
     * 去除特殊字符或将所有中文标号替换为英文标号
     *
     * @param str
     * @return
     */
    public static String stringFilter(String str) {
        str = str.replaceAll("【", "[").replaceAll("】", "]")
                .replaceAll("！", "!").replaceAll("：", ":");// 替换中文标号  
        String regEx = "[『』]"; // 清除掉特殊字符  
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 获取url判断的正则表达式
     * @param str
     * @return
     */
    public static boolean isUrl(String str) {
        String regex = "(?:(https?)://)?((?:(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|net|edu|biz|gov|org|in(?:t|fo)|(?-i:[a-z][a-z]))|(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])))(?::(\\d{1,5}))?(/.*)?";
//    	String regex = "[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
        return match(regex, str);
    }

    /**
     * @param regex 正则表达式字符串
     * @param str   要匹配的字符串
     * @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
     */
    public static boolean match(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 手机号码的合法性
     * @param phone
     * @return
     */
    public static boolean isPhoneByString(String phone) {
        // "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。  
        String regex = "[1][34578]\\d{9}";
        return phone.matches(regex);
    }

    /**
     * 版本转化为数字
     *
     * @param version
     * @return
     */
    public static int versionToNum(String version) {
        try {
            return Integer.parseInt(version.replace(".", ""));
        } catch (NumberFormatException e) {
        }
        return 0;
    }
}
