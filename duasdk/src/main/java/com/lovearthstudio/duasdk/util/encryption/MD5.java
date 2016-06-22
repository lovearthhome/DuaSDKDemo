package com.lovearthstudio.duasdk.util.encryption;

 import android.util.Log;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static final String TAG = "MD5";
    public static String md5(String str) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();  //md5 32bit
            // result = buf.toString().substring(8, 24))); //md5 16bit
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "加密失败, ", e);
        }
        return result;
    }

}
