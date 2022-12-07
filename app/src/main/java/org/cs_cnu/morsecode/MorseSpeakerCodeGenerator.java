package org.cs_cnu.morsecode;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.math.BigInteger;

public class MorseSpeakerCodeGenerator implements MorseSpeakerThread.MorseSpeakerIterator {

    final String message;
    final Map<String, String> map;
    final String morse_code;

    public MorseSpeakerCodeGenerator(String message, Map<String, String> map) {
        this.message = message.trim();     // message.toUpperCase() -> message.trim()
        this.map = map;

// Need to edit below!
        Log.i("MorseSpeaker", String.format("User Input: %s", this.message));
        byte[] byteHex = new byte[0];
        try {
            byteHex = this.message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("MorseSpeaker",
                    String.format("Unicode Encoding Exception: %s", e.getMessage()), e);
        }
        String byteString = new BigInteger(1, byteHex).toString(16).toUpperCase();

        StringBuilder sb = new StringBuilder();
        for(int i=0;i<byteString.length();i++){
            sb.append(map.get(Character.toString(byteString.charAt(i))));
            sb.append(' ');
        }
// Need to edit above!
        String code = sb.toString();
        this.morse_code = code.substring(0, code.length()-1);
        Log.i("MorseCode", this.morse_code);
    }

    public String getMorseCode() {
        return this.morse_code;
    }

    @Override
    public int getSize() {
        int size = 0;
        for (int i = 0; i < this.morse_code.length(); i++) {
            char ch = this.morse_code.charAt(i);
            if (ch == '/') {
                size = size + 2;
            } else if (ch == ' ') {
                size = size + 2;
            } else if (ch == '.') {
                size = size + 1;
            } else if (ch == '-') {
                size = size + 3;
            }
        }
        size = size + this.morse_code.length();
        return size;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String> () {
            boolean start = false;
            boolean end = false;
            int i = 0;

            @Override
            public boolean hasNext() {
                if (!start || !end) {
                    return true;
                }
                return false;
            }

            @Override
            public String next() {
                if (!start) {
                    start = true;
                    i = 0;
                }
                if (morse_code.length() > i) {
                    String value = Character.toString(morse_code.charAt(i));
                    i = i + 1;
                    return value;
                } else {
                    end = true;
                }
                return "";
            }
        };
    }
}
