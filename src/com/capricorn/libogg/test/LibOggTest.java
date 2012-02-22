package com.capricorn.libogg.test;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;

import org.ideaheap.io.VorbisFileOutputStream;

public class LibOggTest extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        short buf[] = new short[1000];
        String fname = "/sdcard/testFile.ogg";

        try {
            VorbisFileOutputStream s = new VorbisFileOutputStream(fname);
            // create a square wave
            for (int j=0; j < 1000; ) {
                for (int i=0; i < 100 && j < 1000; i++) {
                    buf[j++] = (short) (5000);
                }
                for (int i=0; i < 100 && j< 1000; i++) {
                    buf[j++] = (short) (-5000);
                }
            }

            s.write(buf);
            for(int i=0; i < 1000; i++) {
                s.write(buf);
            }

            // Save our 11 second ogg square wave.
            s.close();
        }
        catch (IOException e) {
            // well i guess that didn't go well...
        }
    }
}
