package com.ideaheap.io.test;

import com.ideaheap.io.VorbisFileInputStream;
import com.ideaheap.io.VorbisInfo;

import android.test.AndroidTestCase;

public class VorbisFileInputStreamTest extends AndroidTestCase {
	public void testOpenClose() throws Exception {
		VorbisFileInputStream s = new VorbisFileInputStream("/sdcard/empty.ogg");
		s.close();
		VorbisInfo info = s.getInfo();
		assertEquals(info.channels , 1);
		assertEquals(info.sampleRate , 44100);
		assertEquals(info.length , 0);
	}
	
	public void testReadEmpty() throws Exception {
		short pcmData[] = new short[2048];
		int samplesRead;
		VorbisFileInputStream s = new VorbisFileInputStream("/sdcard/empty.ogg");
		samplesRead = s.read(pcmData);
		assertEquals(samplesRead, -1);
		s.close();
	}
	public void testEntireReadSquare() throws Exception {
		short pcmData[] = new short[2048];
		int samplesRead = 0;
		VorbisFileInputStream s = new VorbisFileInputStream("/sdcard/squareWave.ogg");
		samplesRead = s.read(pcmData);
		assertNotSame(samplesRead, -1);
		while (samplesRead != -1) {
			samplesRead = s.read(pcmData);
			assertNotSame(samplesRead, 0);
		}
		s.close();
	}
}
