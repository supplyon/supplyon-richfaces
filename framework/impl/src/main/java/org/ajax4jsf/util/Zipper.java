package org.ajax4jsf.util;

public class Zipper {
	public static void zip(byte[] buf, int value, int offset) {
		buf[offset] = (byte)(value & 0x0ff);
		buf[offset+1] = (byte)((value & 0x0ff00)>>8);
		buf[offset+2] = (byte)((value & 0x0ff0000)>>16);
	}
	public static int unzip(byte[] buf, int offset) {
		int r0 = buf[offset]&0x0ff;
		int r1 = (buf[offset+1]<<8)&0x0ff00;
		int r2 = (buf[offset+2]<<16)&0x0ff0000;
		int ret = r0 | r1 | r2;
		return ret;
	}
}
