package org.cs_cnu.morsecode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;
public class MorseMicrophoneThread extends Thread {
    public interface MorseMicrophoneCallback {
        void onProgress(String value);
        void onDone(String value);
    }


    final short MORSE_THRESHOLD = Short.MAX_VALUE / 4;
    final float UNSEEN_THRESHOLD = 3.0f;

    final int sample_rate;
    final float frequency;
    final float unit;
    final int unit_size;
    final int buffer_size;

    final MorseMicrophoneThread.MorseMicrophoneCallback callback;

    public MorseMicrophoneThread(MorseMicrophoneThread.MorseMicrophoneCallback callback,
                                 int sample_rate, float frequency, float unit) {
        this.callback = callback;
        this.sample_rate = sample_rate;
        this.frequency = frequency;
        this.unit = unit;
        this.unit_size = (int) Math.ceil(this.sample_rate * this.unit);
        this.buffer_size = (int) AudioRecord.getMinBufferSize(sample_rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        setPriority(Thread.MAX_PRIORITY);
    }
    public double stdev(Short[] array) {
        double sum = 0.0, standardDev = 0.0;
        int leng = array.length;
        for (short num: array) {
            sum += num;
        }
        double mean = sum/leng;

        for (short num: array) {
            standardDev += Math.pow(num-mean, 2);
        }
        return Math.sqrt(standardDev/leng);
    }
    public double stdev(short[] array) {
        double sum = 0.0, standardDev = 0.0;
        int leng = array.length;
        for (short num: array) {
            sum += num;
        }
        double mean = sum/leng;

        for (short num: array) {
            standardDev += Math.pow(num-mean, 2);
        }
        return Math.sqrt(standardDev/leng);
    }
    @Override
    public void run() {
        @SuppressLint("MissingPermission")
        final AudioRecord record = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                this.sample_rate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                2 * sample_rate);

        final short[] samples = new short[unit_size];
        StringBuilder sb = new StringBuilder();

        record.startRecording();
// Need to edit below!
        boolean flag = false;
        int blank = 0;
        ArrayList<Short> audioarr = new ArrayList<>();
        while (true) {
            int result = record.read(samples, 0, unit_size);
            if (result < 0) {
                break;
            }
            double std = stdev(samples);
            for (int i=0;i<unit_size;i++) {
                if (!flag && samples[i] > MORSE_THRESHOLD) {
                    Log.i("Recording", "start");
                    flag = true;
                }
                if (flag) {
                    if (samples[i] > MORSE_THRESHOLD)
                        blank = 0;
                    audioarr.add(samples[i]);
                    blank++;
                }
            }
            if (flag) {
                if (std > MORSE_THRESHOLD) {
                    sb.append('.');
                } else {
                    sb.append(' ');
                }
            }
            callback.onProgress(sb.toString());
            if (blank >= 48000)
                break;
        }
        sb = new StringBuilder();
        record.stop();
        record.release();

        int counter = 0;
        blank = 0;
        int sizeperunit = (int)Math.ceil((double)audioarr.size()/unit_size)+1;
        Short[] arr = new Short[0];
        arr = audioarr.toArray(arr);
        for (int i=1;i<sizeperunit;i++) {
            double standardDev = 0.0;
            int from = (i-1)*unit_size;
            int to = i*unit_size > audioarr.size() ? audioarr.size() : i*unit_size;
            try {
                standardDev = stdev(Arrays.copyOfRange(arr, from, to));
            } catch (NullPointerException e) {
                Log.i("Exception",
                        String.format("NullPointException: i=%d, sizeperunit=%d, audioarr.size=%d",
                        i, sizeperunit, audioarr.size())
                );
                throw e;
            }
            if (standardDev > MORSE_THRESHOLD) {
                switch(blank){
                    case 1:
                        blank = 0; counter = 1;
                        break;
                    case 0:
                        counter++;
                        break;
                    case 3:
                        sb.append(' ');
                        blank = 0; counter = 1;
                        break;
                    case 7:
                        sb.append(" / ");
                        blank = 0; counter = 1;
                        break;
                }
            } else {
                if (counter != 0) {
                    if (counter == 1) {
                        sb.append('.');
                    } else {
                        sb.append('-');
                    }
                    counter = 0; blank = 1;
                } else {
                    blank++;
                }
            }
        }   // end of for
        if (counter == 1) {
            sb.append('.');
        } else if(counter == 3) {
            sb.append('-');
        }
        String morse = sb.toString();
        Log.i("Morse", morse);
        callback.onDone(morse);
// Need to edit above!
    }
}
