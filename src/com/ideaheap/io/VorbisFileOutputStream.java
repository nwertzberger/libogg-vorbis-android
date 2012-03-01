package com.ideaheap.io;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Converts incoming PCM Audio Data into OGG data into a file. This will be implemented
 * using the open source BSD-licensed stuff from Xiph.org.
 * 
 * NOTE: This implementation has a limitation of MAX_STREAMS concurrent output streams.
 * When i wrote this, it was set to 8.  Check in vorbis-fileoutputstream.c to see what
 * it is set to.
 * 
 * @author nwertzberger
 *
 */
public class VorbisFileOutputStream implements Closeable {
	
	// The index into native memory where the ogg stream info is stored.
	private final int oggStreamIdx;
	
	static {
		System.loadLibrary("ogg");
		System.loadLibrary("vorbis");
		System.loadLibrary("vorbis-stream");
	}
	
	public VorbisFileOutputStream (String fname, VorbisInfo s) throws IOException {
		oggStreamIdx = this.create(fname, s);
	}
	public VorbisFileOutputStream (String fname) throws IOException {
		oggStreamIdx = this.create(fname, new VorbisInfo());
	}
	
	@Override
	public void close() throws IOException {
		this.closeStreamIdx(this.oggStreamIdx);
	}
	
	/**
	 * Please god never use this.  It works, but this is very inefficient.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	public int write(final short buf) throws IOException {
		short [] bufArray = new short[1];
		bufArray[0] = buf;
		return this.writeStreamIdx(this.oggStreamIdx, bufArray, 0, 1);
	}
	
	/**
	 * Write PCM data to ogg.  This assumes that you pass your streams in interleaved.
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public int write(final short [] buffer, int offset, int length) throws IOException {
		return this.writeStreamIdx(this.oggStreamIdx, buffer, 0, length);
	}
	
	/**
	 * Write PCM data to ogg. This assumes you pass streams in interleaved.
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public int write(final short [] buffer) throws IOException {
		return this.writeStreamIdx(this.oggStreamIdx, buffer, 0, buffer.length);
	}
	
	private native int writeStreamIdx(int idx, short [] pcmdata, int offset, int size) throws IOException;
	private native void closeStreamIdx(int idx) throws IOException;
	private native int create(String path, VorbisInfo s) throws IOException;
}
