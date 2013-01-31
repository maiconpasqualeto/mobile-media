/**
 * 
 */
package br.com.sixtec.MobileMedia.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.sixtec.MobileMedia.facade.MobileFacade;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

/**
 * @author maicon
 *
 */
public class ConexaoService extends Service{
	
	private DownloadTask task;
		
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String serial = intent.getExtras().getString("serial");
		String identifidor = intent.getExtras().getString("identifidor");
		
		task = new DownloadTask();
		task.execute(serial, identifidor);
		
		return Service.START_FLAG_REDELIVERY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private class DownloadTask extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... params) {
			boolean sucesso = false;
			try {
				// pega nova lista de midias do servidor
				JSONArray arr = MobileFacade.getInstance().registraBoard(params[0], params[1]);
				
				for (int i=0; i<arr.length(); i++) {
					JSONObject o = arr.getJSONObject(i);
					final String idMidia = o.getString("id");
					final String nomeArquivo = o.getString("nomeArquivo");
					MobileFacade.getInstance().downloadMidia(idMidia, nomeArquivo);
				}
				
				if (arr.length() > 0)
					MobileFacade.getInstance().moveArquivosPlaylist();
				
				sucesso = true;
				
			} catch (JSONException e) {
				Log.e(MobileMediaHelper.TAG, "Erro no dowload de midias", e);
			}
			return sucesso;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			super.onPostExecute(result);
		}
		
	}

}
