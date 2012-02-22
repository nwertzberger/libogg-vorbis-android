package org.ideaheap.io;

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
	
	public int write(final short [] buffer) throws IOException {
		return this.writeStreamIdx(this.oggStreamIdx, buffer);
	}
	
	private native int writeStreamIdx(int idx, short [] pcmdata) throws IOException;
	private native void closeStreamIdx(int idx) throws IOException;
	private native int create(String path, VorbisInfo s) throws IOException;
}
