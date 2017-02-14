package util;

import java.nio.ByteBuffer;

public class NetworkUtil {
	
	/**
	 * Converts int with little endian to big endian and vice versa
	 * @param i Int to swap
	 * @return Swapped int
	 */
	public static int swapIntEndian(int i) {
	    return (i&0xff)<<24 | (i&0xff00)<<8 | (i&0xff0000)>>8 | (i>>24)&0xff;
	}
	
	
	/**
	 * Converts short with little endian to big endian and vice versa
	 * @param s Short to swap
	 * @return Swapped short
	 */
	public static int swapShortEndian(int s) {
	    return ((s&0xff)<<8 | (s&0xff00)>>8) & 0x0000ffff;
	}
	
	
	public static int bytesToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return buffer.getInt();
	}
	
	
	public static byte[] intToBytes(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(value);
		return buffer.array();
	}
}
