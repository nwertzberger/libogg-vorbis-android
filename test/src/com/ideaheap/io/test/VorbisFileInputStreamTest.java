package com.ideaheap.io.test;

import com.ideaheap.io.VorbisFileInputStream;
import com.ideaheap.io.VorbisInfo;

import android.test.AndroidTestCase;

public class VorbisFileInputStreamTest extends AndroidTestCase {
	
	
	private static final String EMPTY = "/sdcard/empty.ogg";
	private static final String SQUARE_WAVE = "/sdcard/squareWave.ogg";

	public void testOpenClose() throws Exception {
		VorbisFileInputStream s = new VorbisFileInputStream(EMPTY);
		s.close();
		VorbisInfo info = s.getInfo();
		assertEquals(info.channels , 1);
		assertEquals(info.sampleRate , 44100);
		assertEquals(info.length , 0);
	}
	
	public void testReadEmpty() throws Exception {
		short pcmData[] = new short[2048];
		int samplesRead;
		VorbisFileInputStream s = new VorbisFileInputStream(EMPTY);
		samplesRead = s.read(pcmData);
		assertEquals(samplesRead, -1);
		s.close();
	}
	public void testEntireReadSquare() throws Exception {
		VorbisFileInputStream s = new VorbisFileInputStream(SQUARE_WAVE);
		short pcmData[] = new short[2048];
		int samplesRead = 0;
		samplesRead = s.read(pcmData);
		assertNotSame(samplesRead, -1);
		while (samplesRead != -1) {
			samplesRead = s.read(pcmData);
			assertNotSame(samplesRead, 0);
		}
		s.close();
	}
	
	public void testOpenAndCloseForFileLeak() throws Exception {
		for (int i=0; i < 50; i++) {
			VorbisFileInputStream s = new VorbisFileInputStream(SQUARE_WAVE);
			s.close();
		}
	}
}
