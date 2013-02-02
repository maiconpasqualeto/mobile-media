/**
 * 
 */
package br.com.sixtec.MobileMedia.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import br.com.sixtec.MobileMedia.facade.MobileFacade;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class ConexaoService extends Service {
	
	private DownloadTask task;
	private Messenger messenger;
		
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		String serial = intent.getExtras().getString("serial");
		String identifidor = intent.getExtras().getString("identificador");
		
		this.task = new DownloadTask();
		this.task.execute(serial, identifidor);
		
		this.messenger = (Messenger) intent.getParcelableExtra("messenger");
		
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
				MobileFacade facade = MobileFacade.getInstance(getApplicationContext());
				// pega nova lista de midias do servidor
				String boardSerial = params[0];
				String identificador = params[1];
				JSONArray arr = facade.registraBoard(boardSerial, identificador);
				
				for (int i=0; i<arr.length(); i++) {
					JSONObject o = arr.getJSONObject(i);
					final String idMidia = o.getString("id");
					final String nomeArquivo = o.getString("nomeArquivo");
					facade.downloadMidia(idMidia, nomeArquivo);
				}
				
				if (arr.length() > 0)
					sucesso = true;
				//	facade.moveArquivosPlaylist();
				
			} catch (JSONException e) {
				Log.e(MobileMediaHelper.TAG, "Erro no dowload de midias", e);
			}
			return sucesso;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.d(MobileMediaHelper.TAG, "onPostExecute");
			Message m = new Message();
			m.obj = result;
			try {
				messenger.send(m);
			} catch (RemoteException e) {
				Log.e(MobileMediaHelper.TAG, 
						"Erro ao enviar mensagem do service para activity", e);
			}
		}
		
	}

}
