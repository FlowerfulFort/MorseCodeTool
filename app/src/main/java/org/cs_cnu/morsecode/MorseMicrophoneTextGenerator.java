package org.cs_cnu.morsecode;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;

public class MorseMicrophoneTextGenerator {

    final String morse_code;
    final Map<String, String> map;
    final String text;

    public MorseMicrophoneTextGenerator(String morse_code, Map<String, String> map) {
        this.morse_code = morse_code;
        this.map = map;

// Need to edit below!
        HashMap<String, String> reverseMap = new HashMap<>();

        for (Map.Entry<String, String> entry: map.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }
        StringBuilder sb = new StringBuilder();
        String[] token = morse_code.split(" ");
        for (String tok: token) {
            sb.append(reverseMap.get(tok));
        }
// Need to edit above!
        String byteString = sb.toString();
        byte[] clientByteHex = new byte[byteString.length()/2];
        for (int i=0;i<clientByteHex.length;i++) {
            int index = i*2;
            clientByteHex[i] = (byte) Integer.parseInt(
                    byteString.substring(index, index+2), 16);
        }
        String clientByteString = new BigInteger(1, clientByteHex)
                .toString().toUpperCase();
        Log.i("MorseMicrophone", "clientByteString: " + clientByteString);

        String clientOutput = "";
        try {
            clientOutput = new String(clientByteHex, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("MorseMicrophone", "Unicode decode Error: "+e.getMessage(), e);
        }
        this.text = clientOutput;
        Log.i("Sound input", text);
    }

    public String getText() {
        return this.text;
    }
}
