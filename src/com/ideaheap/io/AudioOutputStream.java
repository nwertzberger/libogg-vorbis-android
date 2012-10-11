package com.ideaheap.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AudioOutputStream extends OutputStream implements Closeable {
	/**
	 * In my world, passing arrays is not expensive.
	 * @param buf
	 * @return
	 * @throws IOException
	 */
	@Override
	public void write(final int buf) throws IOException {
		short[] buffer = new short[1];
		buffer[0] = (short)buf;
		this.write(buffer, 0, buffer.length);
	}
	
	public int write(final short [] buffer) throws IOException {
		return this.write(buffer, 0, buffer.length);
	}
	
	public abstract int write(final short [] buffer, int offset, int length)
		throws IOException;
	public abstract int getSampleRate();
	public abstract void close() throws IOException;
}
