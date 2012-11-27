package com.ideaheap.io.test;

import com.ideaheap.io.VorbisFileOutputStream;
import com.ideaheap.io.VorbisInfo;

import android.test.AndroidTestCase;

public class VorbisFileOutputStreamTest extends AndroidTestCase {
	
	private String fname = "/sdcard/whiteNoise.ogg";

	public void testOpenClose() throws Exception {
		VorbisFileOutputStream s = new VorbisFileOutputStream("/sdcard/empty.ogg");
		s.close();
	}
	
	public void testSquareWave() throws Exception {
		short buf[] = new short[1000];
		String fname = "/sdcard/squareWave.ogg";
		
		VorbisFileOutputStream s = new VorbisFileOutputStream(fname);
		// Write a square wave
		for (int j=0; j < 1000; ) {
			for (int i=0; i < 100 && j < 1000; i++) {
				buf[j++] = (short) (5000);
			}
			for (int i=0; i < 100 && j< 1000; i++) {
				buf[j++] = (short) (-5000);
			}
		}
		
		s.write(buf);
		assertEquals(1, 1);
		for(int i=0; i < 500; i++) {
			s.write(buf);
		}
		s.close();
		
	}
	public void testWhiteNoise() throws Exception {
		short buf[] = new short[1000];
		
		VorbisFileOutputStream s = new VorbisFileOutputStream(fname);
		// Write a square wave
		for (int j=0; j < 1000; ) {
				buf[j++] = (short) (Math.random()-0.5 * 10000);
		}
		
		s.write(buf);
		assertEquals(1, 1);
		for(int i=0; i < 500; i++) {
			s.write(buf);
		}
		s.close();
		
	}
	
	public void testOpenAndCloseForFileLeak() throws Exception {
		VorbisInfo vi = new VorbisInfo();
		for (int i=0; i < 50; i++) {
			VorbisFileOutputStream s = new VorbisFileOutputStream(fname,vi);
			s.close();
		}
	}
}
