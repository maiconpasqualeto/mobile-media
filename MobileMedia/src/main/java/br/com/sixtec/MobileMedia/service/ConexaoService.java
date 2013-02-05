/**
 * 
 */
package br.com.sixtec.MobileMedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import br.com.sixtec.MobileMedia.facade.MobileFacade;
import br.com.sixtec.MobileMedia.persistencia.Midia;
import br.com.sixtec.MobileMedia.persistencia.Playlist;
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
		
		String serial = intent.getStringExtra("serial");
		String identifidor = intent.getStringExtra("identificador");
		
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
			
			MobileFacade facade = MobileFacade.getInstance(getApplicationContext());
			// pega nova lista de midias do servidor
			String boardSerial = params[0];
			String identificador = params[1];
			//String strDataHoraPlaylist = params[2];
			Playlist p = facade.registraBoard(boardSerial, identificador);
			
			// se o playlist foi atualizado então faz o download das midias e 
			// informa a o player que houve mudanças (sucesso = true)
			if (p.isAtualizado()){
				
				facade.apagaTodosArquivosDaPastaTemp();
				
				for (Midia m : p.getMidias()) {
					facade.downloadMidia(m.getId().toString(), m.getNomeArquivo());
				}
				
				sucesso = true;
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
			
			ConexaoService.this.stopSelf();
			task.cancel(true);
		}
		
	}

}
