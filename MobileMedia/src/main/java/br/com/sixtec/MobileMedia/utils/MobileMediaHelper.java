/**
 * 
 */
package br.com.sixtec.MobileMedia.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author maicon
 *
 */
public class MobileMediaHelper {

	public static byte[] toByteArray(InputStream is) throws IOException {
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int lidos;
		while ((lidos = is.read(bytes)) > 0) {
			baos.write(bytes, 0, lidos);
		}
		return baos.toByteArray();
	}

}
