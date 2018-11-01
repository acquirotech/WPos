package com.acquiro.wpos.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppUtil {
    public AppUtil() {
    }


    public static boolean isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Protects PAN, Track2, CVC (suitable for logs).
     * <p>
     * <pre>
     * "40000101010001" is converted to "400001____0001"
     * "40000101010001=020128375" is converted to "400001____0001=0201_____"
     * "123" is converted to "___"
     * </pre>
     *
     * @param s string to be protected
     * @return 'protected' String
     */
    public static String cardProtect(String s) {
        StringBuffer sb = new StringBuffer();
        int len = s.length();
        int clear = len > 6 ? 6 : 0;
        int lastFourIndex = -1;
        if (clear > 0) {
            lastFourIndex = s.indexOf('=') - 4;
            if (lastFourIndex < 0) {
                lastFourIndex = s.indexOf('^') - 4;
                if (lastFourIndex < 0) {
                    lastFourIndex = len - 4;
                }
            }
        }
        for (int i = 0; i < len; i++) {
            if (s.charAt(i) == '=') {
                clear = 5;
            } else if (s.charAt(i) == '^') {
                lastFourIndex = 0;
                clear = len - i;
            } else if (i == lastFourIndex) {
                clear = 4;
            }
            sb.append(clear-- > 0 ? s.charAt(i) : '*');
        }
        return sb.toString();
    }

    public static String formatAmount(String amount, boolean separator) {
        if (amount == null) {
            amount = "";
        }
        if (amount.length() < 3) {
            amount = StringUtil.fillZero(amount, 3);
        }
        StringBuffer s = new StringBuffer();
        int strLen = amount.length();
        for (int i = 1; i <= strLen; i++) {
            s.insert(0, amount.charAt(strLen - i));
            if (i == 2) s.insert(0, '.');
            if (i > 3 && ((i % 3) == 0)) {
                if (separator) {
                    s.insert(1, ',');
                }
            }
        }
        return s.toString();
    }

    /**
     * prepare long value used as amount for display
     * (implicit 2 decimals)
     *
     * @param amount value
     * @return formated field
     * @throws RuntimeException
     */
    public static String formatAmount(long amount) {
        return formatAmount("" + amount, false);
    }

    public static int toAmount(byte[] byte6) {
        int amount = -1;

        try {
            amount = Integer.parseInt(StringUtil.toHexString(byte6, false));
        } catch (NumberFormatException e) {
            amount = -1;  // the number too big
        }

        return amount;
    }

    /**
     * F87608B3F4E629DA3776157A401AC216 -> TMK
       0EECBE37D972447C9AE04D0119597E94 ->DUKPT
       03000000009500000000 ->KSN
     //99999999
     * @param buffer
     * @return
     */

    public static byte[] removeTailF(byte[] buffer) {
        int length = buffer.length;
        for (; length > 0; length--) {
            if (buffer[length - 1] != 'F')
                break;
        }
        if (length == buffer.length) {
            return buffer;
        } else {
            byte[] destBuffer = new byte[length];
            System.arraycopy(buffer, 0, destBuffer, 0, length);
            return destBuffer;
        }
    }
}
