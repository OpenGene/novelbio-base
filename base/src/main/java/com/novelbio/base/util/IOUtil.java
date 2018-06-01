package com.novelbio.base.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * IO流工具类
 * @author novelbio
 */
public final class IOUtil {

	private IOUtil() {
	}

	/**
	 * 关闭一个或多个流对象
	 * @param closeables 可关闭的流对象列表
	 * @throws IOException
	 */
	public static void close(Closeable... closeables)  {
		if (closeables != null) {
			for (Closeable closeable : closeables) {
				if (closeable != null) {
					try {
						closeable.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}


}
