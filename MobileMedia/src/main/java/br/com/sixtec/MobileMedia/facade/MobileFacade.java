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
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.Midia;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.persistencia.Playlist;
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
	
	public void apagaTodosArquivosDaPastaTemp(){
		String tempPath = MobileMediaHelper.DIRETORIO_TEMPORARIO;
		File dir = new File(tempPath);
		
		if (!dir.exists())
			return;
		
		// apaga todos os arquivos de midia que existirem na pasta temp
		FilenameFilter fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(MobileMediaHelper.EXTENSAO_ARQUIVO_MIDIA);
			}
		};
		File[] arsMidia = dir.listFiles(fnf);
		for (int i=0; i<arsMidia.length; i++){
			File arq = arsMidia[i];
			Log.d(TAG, "Arquivo deletado: " + arq.getPath() + "/" + arq.getName());
			arq.delete();
		}
	}
	
	/**
	 * Método utilizado dentro de uma thread
	 * 
	 * @param boardSerial
	 * @param identificador
	 * @param strDataHoraPlaylist
	 * @return
	 */
	public Playlist registraBoard(String boardSerial, String identificador) {
		String nomeRest = "board/registraboard";
		BasicNameValuePair p1 = new BasicNameValuePair("boardSerial", boardSerial);
		BasicNameValuePair p2 = new BasicNameValuePair("identificador", identificador);
		
		Playlist p = null;
		
		byte[] b = conn.postREST(retornaHost(), nomeRest, p1, p2);
		if (b == null) {
			p = new Playlist();
			return p;
		}
		
		String result = new String(b);
				
		try {
			JSONObject obj = new JSONObject(result);
			JSONObject jPlaylist = obj.getJSONObject("playlist");
			JSONArray jMidias = obj.getJSONArray("midias");
			p = new Playlist(jPlaylist);
			for (int i=0; i<jMidias.length(); i++) {
				JSONObject jMidia = jMidias.getJSONObject(i);
				Midia m = new Midia(jMidia);
				p.getMidias().add(m);
			}
			
			MobileMediaDAO dao = MobileMediaDAO.getInstance(ctx);
			MMConfiguracao c = dao.buscaConfiguracao();
			String strPlaylist = p.getDataHoraCriacao() != null ?
					MobileMediaHelper.JSON_DATE_FORMAT.format(p.getDataHoraCriacao()) : "";
			String strDataHoraPlaylist = c.getDataHoraPlaylist() != null ?
					MobileMediaHelper.JSON_DATE_FORMAT.format(c.getDataHoraPlaylist()) : "";
			
			// se a lista de mídias não estiver vazio e se a data do playlist for diferente da que 
			// recebeu do servidor, então o playlist deve ser atualizado.
			if ( (!p.getMidias().isEmpty()) &&
					( (c.getIdPlaylist() == null) || 
						(!p.getId().equals(c.getIdPlaylist())) || 
						(!strDataHoraPlaylist.equals(strPlaylist)) ) ) {
				
				// atualiza o banco com a configuração
				c.setIdPlaylist(p.getId());
				c.setDataHoraPlaylist(p.getDataHoraCriacao());
				dao.alterarConfiguracao(c);
				p.setAtualizado(true);
			}
			
		} catch (JSONException e) {
			Log.e(TAG, "Erro ao fazer parsing do JSON", e);
		}
		return p;
	}
	
	public void moveArquivosPlaylist(){
		FilenameFilter fnf = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(MobileMediaHelper.EXTENSAO_ARQUIVO_MIDIA);
			}
		};
		// se não tiver arquivos temporários não faz nada
		File dirTemp = new File(MobileMediaHelper.DIRETORIO_TEMPORARIO);
		File[] arqsTemp = dirTemp.listFiles(fnf);
		if (arqsTemp.length == 0)
			return;
		
		// apaga as midias atuais
		File dirMidias =  new File(MobileMediaHelper.DIRETORIO_MIDIAS);
		File[] arsMidia = dirMidias.listFiles(fnf);
		for (int i=0; i<arsMidia.length; i++){
			File arq = arsMidia[i];
			Log.d(TAG, "Arquivo deletado: " + arq.getPath() + "/" + arq.getName());
			arq.delete();
		}
		
		// copia as midias baixadas
		for (int i=0; i<arqsTemp.length; i++){
			File arq = arqsTemp[i];			
			MobileMediaHelper.moveFile(arq, dirMidias);
		}
		
	}
	
}
