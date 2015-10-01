package testingserver;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Special purpose InputStream that uses zero byte to separate content.
 */
public final class ZeroByteSeparatedXMLInputStream extends FilterInputStream {

	private boolean started = false;
	private boolean ended = false;

	/**
	 * Creates a <code>FilterInputStream</code> by assigning the  argument <code>in</code> to the field
	 * <code>this.in</code> so as to remember it for later use.
	 *
	 * @param in the underlying input stream, or <code>null</code> if this instance is to be created without an
	 *           underlying stream.
	 */
	public ZeroByteSeparatedXMLInputStream(InputStream in) throws IOException {
		super(in);
		int read = in.read();
		while (read != '<') {
			if (read == -1) {
				ended = true;
				break;
			}
			read = in.read();
		}
	}

	/**
	 * Reads the next byte of data from this input stream. The value byte is returned as an <code>int</code> in
	 * the range <code>0</code> to <code>255</code>. If no byte is available because the end of the stream has
	 * been reached, the value <code>-1</code> is returned. This method blocks until input data is available, the
	 * end of the stream is detected, or an exception is thrown.
	 * <p/>
	 * This method simply performs <code>in.read()</code> and returns the result.
	 *
	 * @return the next byte of data, or <code>-1</code> if the end of the stream is reached.
	 *
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public int read() throws IOException {
		if (ended)
			return -1;
		if (!started) {
			started = true;
			return '<';
		}
		int read = in.read();
		if (read == 0 || read == -1) {
			ended = true;
			return -1;
		}
		return read;
	}

	/**
	 * Reads up to <code>byte.length</code> bytes of data from this input stream into an array of bytes. This
	 * method blocks until some input is available.
	 * <p/>
	 * This method simply performs the call <code>read(b, 0, b.length)</code> and returns the  result. It is
	 * important that it does <i>not</i> do <code>in.read(b)</code> instead; certain subclasses of
	 * <code>FilterInputStream</code> depend on the implementation strategy actually used.
	 *
	 * @param b the buffer into which the data is read.
	 *
	 * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data
	 *         because the end of the stream has been reached.
	 *
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * Reads up to <code>len</code> bytes of data from this input stream into an array of bytes. If
	 * <code>len</code> is not zero, the method blocks until some input is available; otherwise, no bytes are
	 * read and <code>0</code> is returned.
	 * <p/>
	 * This method simply performs <code>in.read(b, off, len)</code> and returns the result.
	 *
	 * @param b   the buffer into which the data is read.
	 * @param off the start offset in the destination array <code>b</code>
	 * @param len the maximum number of bytes read.
	 *
	 * @return the total number of bytes read into the buffer, or <code>-1</code> if there is no more data
	 *         because the end of the stream has been reached.
	 *
	 * @throws NullPointerException	  If <code>b</code> is <code>null</code>.
	 * @throws IndexOutOfBoundsException If <code>off</code> is negative, <code>len</code> is negative, or
	 *                                   <code>len</code> is greater than <code>b.length - off</code>
	 * @throws java.io.IOException	   if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (ended)
			return -1;
		if (len == 0)
			return 0;
		int total = 0;
		if (!started) {
			started = true;
			b[off] = '<';
			total = 1;
		}
		while (total < len) {
			int read = in.read();
			if (read == -1) {
				ended = true;
				return total;
			}
			if (read == 0) {
				ended = true;
				return total;
			}
			b[off + total] = (byte) read;
			total++;
		}
		return total;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * This method simply performs <code>in.skip(n)</code>.
	 */
	@Override
	public long skip(long n) throws IOException {
		// @todo implement
		return super.skip(n);
	}

	/**
	 * Closes this input stream and releases any system resources associated with the stream. This method simply
	 * performs <code>in.close()</code>.
	 *
	 * @throws java.io.IOException if an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	@Override
	public void close() throws IOException {
	}

	@Override
	public String toString() {
		//noinspection ObjectToString
		return "XML Filter [ " + in + " ]";
	}
}
