/**
 * 
 */
package br.com.sixtec.MobileMedia.facade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
import br.com.sixtec.MobileMedia.webservice.ConexaoRest;

/**
 * @author maicon
 *
 */
public class MobileFacade {
	
	private static final String TAG = "MobileMedia";
	
	private ConexaoRest conn;

	private static MobileFacade facade;
	private Context ctx;
	
	public static MobileFacade getInstance(Context ctx){
		if (facade == null)
			facade = new MobileFacade(ctx);
		
		return facade;
	}
	
	public MobileFacade(Context ctx) {
		conn = new ConexaoRest();
		this.ctx = ctx;
	}
	
	private String retornaHost(){
		MMConfiguracao c = MobileMediaDAO.getInstance(ctx).buscaConfiguracao();
		return c.getIp() + ":" + c.getPorta();
	}
		
	public void downloadMidia(String idMidia, String nomeArquivo) {
		String nomeRest = "board/downloadmidia/" + idMidia;
		
		
		byte[] b = conn.getREST(retornaHost(), nomeRest);
		
		if (b == null) {
			Log.e(TAG, "Falha ao acessar WS");
			return;
		}
		FileOutputStream fos = null;
		try {
			
			String tempPath = MobileMediaHelper.DIRETORIO_TEMPORARIO;
			File dir = new File(tempPath);
			if (!dir.exists())
				dir.mkdir();
			
			File arquivo = new File(tempPath + nomeArquivo);
			fos = new FileOutputStream(arquivo);
			fos.write(b);
			fos.flush();
			
			Log.d(TAG, "Feito Download da Midia: " + arquivo.getPath() + "/" + arquivo.getName());
			
		} catch (IOException e) {
			Log.e(TAG, "Erro ao gravar arquivo de midia");
		} finally {
			try { 
				if (fos != null)
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
		
		byte[] b = conn.postREST(retornaHost(), nomeRest, p1, p2);
		if (b == null) {
			arr = new JSONArray();
			return arr;
		}
		
		String result = new String(b);
				
		try {
			arr = new JSONArray(result);
			
		} catch (JSONException e) {
			Log.e(TAG, "Erro ao fazer parsing do JSON", e);
		}
		return arr;
	}
	
	public void moveArquivosPlaylist(){
		FilenameFilter fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(MobileMediaHelper.EXTENSAO_ARQUIVO_MIDIA);
			}
		};
		// apaga as midias atuais
		File dirMidias =  new File(MobileMediaHelper.DIRETORIO_MIDIAS);
		File[] arsMidia = dirMidias.listFiles(fnf);
		for (int i=0; i<arsMidia.length; i++){
			File arq = arsMidia[i];
			Log.d(TAG, "Arquivo deletado: " + arq.getPath() + "/" + arq.getName());
			arq.delete();
		}
		
		// copia as midias baixadas
		File dirTemp = new File(MobileMediaHelper.DIRETORIO_TEMPORARIO);
		File[] arqsTemp = dirTemp.listFiles(fnf);
		for (int i=0; i<arqsTemp.length; i++){
			File arq = arqsTemp[i];			
			MobileMediaHelper.moveFile(arq, dirMidias);
		}
		
	}
	
}
