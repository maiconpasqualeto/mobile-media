/**
 * 
 */
package br.com.sixtec.MobileMedia.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * @author maicon
 *
 */
public class MobileMediaHelper {
	
	public static final String TAG = "MobileMedia";
	
	public static final String DIRETORIO_MIDIAS = Environment.getExternalStorageDirectory().getPath() + "/";
	public static final String DIRETORIO_TEMPORARIO = Environment.getExternalStorageDirectory().getPath() + "/tmp/";
	public static final String EXTENSAO_ARQUIVO_MIDIA = ".mp4";
	public static final DateFormat SQLITE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	public static final DateFormat JSON_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
	
	public static byte[] toByteArray(InputStream is) throws IOException {
		byte[] bytes = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int lidos;
		while ((lidos = is.read(bytes)) > 0) {
			baos.write(bytes, 0, lidos);
		}
		return baos.toByteArray();
	}
	
	public static void moveFile(File arquivo, File diretorioDestino){
		Log.d(TAG, "Movendo arquivo: " + arquivo.getName() + " de " + arquivo.getPath() + "/" + " para ==> " +
				diretorioDestino.getPath());
		copyFile(arquivo, diretorioDestino);
		arquivo.delete();
	}
	
	public static void copyFile(File arquivo, File diretorioDestino){
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(arquivo);			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int len = fis.read(buf);
			while (len > -1){
				baos.write(buf);
				len = fis.read(buf);
			}
			baos.flush();
			byte[] bytes = baos.toByteArray();
			
			if (!diretorioDestino.exists())
				diretorioDestino.mkdir();
			
			File arqDestino = new File(diretorioDestino.getPath() + "/" + arquivo.getName());
			fos = new FileOutputStream(arqDestino);
			fos.write(bytes);
			fos.flush();
						
		} catch (IOException e) {
			Log.e(TAG, "Erro ao mover arquivo");
        } finally {
        	try {
        		if (fis != null)
        			fis.close();
        		if (fos!=null)
        			fos.close();
        	} catch (IOException e) {
        		Log.e(TAG, "Erro ao fechar arquivos");
        	}
        }
	}
	
	/**
     * If the user has specified a local url, then we download the
     * url stream to a temporary location and then call the setDataSource
     * for that local file
     *
     * @param path
     * @throws IOException
     */
    public void setDataSource(MediaPlayer mp, String path) throws IOException {
        if (!URLUtil.isNetworkUrl(path)) {
            mp.setDataSource(path);
        } else {
            URL url = new URL(path);
            URLConnection cn = url.openConnection();
            cn.connect();
            InputStream stream = cn.getInputStream();
            if (stream == null)
                throw new RuntimeException("stream is null");
            File temp = File.createTempFile(path + "/mediaplayertmp", "dat");
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                out.write(buf, 0, numread);
            } while (true);
            mp.setDataSource(tempPath);
            try {
                stream.close();
                out.close();
            }
            catch (IOException ex) {
                Log.e(TAG, "error: " + ex.getMessage(), ex);
            }
        }
    }

}
