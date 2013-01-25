/**
 * 
 */
package br.com.sixtec.MobileMedia.facade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Environment;
import android.util.Log;
import br.com.sixtec.MobileMedia.webservice.ConexaoRest;

/**
 * @author maicon
 *
 */
public class MobileFacade {
	
	private static final String TAG = "MobileMedia";
	
	private ConexaoRest conn;

	private static MobileFacade facade;
	
	public static MobileFacade getInstance(){
		if (facade == null)
			facade = new MobileFacade();
		
		return facade;
	}
	
	public MobileFacade() {
		conn = new ConexaoRest();
	}
		
	public void downloadMidia(String idMidia, String nomeArquivo) {
		String nomeRest = "board/downloadmidia/" + idMidia;
		
		byte[] b = conn.getREST(nomeRest);
		
		if (b == null) {
			Log.e(TAG, "Falha ao acessar WS");
			return;
		}
		FileOutputStream fos = null;
		try {
		
			String path = Environment.getExternalStorageDirectory() + "/tmp/";
			File dir = new File(path);
			if (!dir.exists())
				dir.mkdir();
			
			File arquivo = new File(path + nomeArquivo);
			fos = new FileOutputStream(arquivo);
			fos.write(b);
			fos.flush();
			
		} catch (IOException e) {
			Log.e(TAG, "Erro ao gravar arquivo de midia");
		} finally {
			try { 
				fos.close();
			} catch (IOException e) {
				Log.e(TAG, "Erro ao gravar arquivo de midia");
			}
		}
		
	}
	
	public JSONArray registraBoard(String boardSerial, String identificador) {
		String nomeRest = "board/registraboard";
		BasicNameValuePair p1 = new BasicNameValuePair("boardSerial", boardSerial);
		BasicNameValuePair p2 = new BasicNameValuePair("identificador", identificador);
		
		JSONArray arr = null;
		
		byte[] b = conn.postREST(nomeRest, p1, p2);
		String result = new String(b);
				
		try {
			arr = new JSONArray(result);
			
		} catch (JSONException e) {
			Log.e(TAG, "Erro ao fazer parsing do JSON", e);
		}
		return arr;
	}
	
	public void moveArquivosPlaylist(){
		
	}
	
}
