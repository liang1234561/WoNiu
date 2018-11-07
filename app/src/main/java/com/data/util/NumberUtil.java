package com.data.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName: NumberUtil
 * @Description: 数字相关工具类
 */
public class NumberUtil {

    /**
     * @param @param  x
     * @param @return
     * @return byte 返回类型
     * @throws
     * @Title: intToByte
     * @Description: int转成byte
     */
    public static byte intToByte(int x) {
        return (byte) x;
    }

    /**
     * @param @param  b
     * @param @return
     * @return int 返回类型
     * @throws
     * @Title: byteToInt
     * @Description: byte转成int
     */
    public static int byteToInt(byte b) {
        // Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    public static void main(String[] args) {
//		int int0 = 234;
//		byte byte0 = intToByte(int0);
//		System.out.println("byte0=" + byte0);// byte0=-22
        long s = 3;
        byte[] b = getBytes(s);
        System.err.println(getLong(b));
    }

    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) ((data >> 56) & 0xff);
        bytes[1] = (byte) ((data >> 48) & 0xff);
        bytes[2] = (byte) ((data >> 40) & 0xff);
        bytes[3] = (byte) ((data >> 32) & 0xff);
        bytes[4] = (byte) ((data >> 24) & 0xff);
        bytes[5] = (byte) ((data >> 16) & 0xff);
        bytes[6] = (byte) ((data >> 8) & 0xff);
        bytes[7] = (byte) (data & 0xff);
        return bytes;
    }

    public static long getLong(byte[] bytes) {
        return (0xffL & (long) bytes[7]) | (0xff00L & ((long) bytes[6] << 8))
                | (0xff0000L & ((long) bytes[5] << 16))
                | (0xff000000L & ((long) bytes[4] << 24))
                | (0xff00000000L & ((long) bytes[3] << 32))
                | (0xff0000000000L & ((long) bytes[2] << 40))
                | (0xff000000000000L & ((long) bytes[1] << 48))
                | (0xff00000000000000L & ((long) bytes[0] << 56));
    }

    /**
     * @param @param  i
     * @param @return
     * @return byte[] 返回类型
     * @throws
     * @Title: intToByteArray
     * @Description: int转byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        // 由高位到低位
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    /**
     * @param @param  bytes
     * @param @return
     * @return int 返回类型
     * @throws
     * @Title: byteArrayToInt
     * @Description: byte数组转int
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        // 由高位到低位
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (bytes[i] & 0x000000FF) << shift;// 往高位游
        }
        return value;
    }

    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[1] & 0xff);
        short s1 = (short) (b[0] & 0xff);// 最低位
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * 把16进制描述的String换成byte数组转换
     *
     * @param str
     * @return
     */
    public static byte[] hexString2ByteArray(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }

        if (0 != str.length() % 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    /**
     * 利用MD5进行加密
     *
     * @param str 待加密的字符串
     * @return 加密后的字符串
     * @throws NoSuchAlgorithmException     没有这种产生消息摘要的算法
     * @throws UnsupportedEncodingException
     */
    public String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        //加密后的字符串
        String newstr = byteArray2HEXString(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    /**
     * 把byte数组转换成16进制描述的String
     *
     * @param data
     * @return
     */
    public static String byteArray2HEXString(byte[] data) {
        if (null == data || 0 == data.length)
            return "";

        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(convertDigit(b >> 4));
            sb.append(convertDigit(b & 0x0f));
        }
        return sb.toString();
    }

    private static char convertDigit(int value) {
        value &= 0x0f;
        if (value >= 10)
            return ((char) (value - 10 + 'a'));
        else
            return ((char) (value + '0'));
    }
}
