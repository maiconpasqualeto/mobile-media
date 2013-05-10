/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Messenger;
import android.util.AndroidException;
import android.util.Log;
import br.com.sixtec.MobileMedia.PlayerActivity;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class SimpleWifiReceiver extends BroadcastReceiver {
	
	private static final String TAG = MobileMediaHelper.TAG;
	
	private final int INTERVALO_SERVICO_DOWNLOAD = 60000; // 1 minuto
	
	private PendingIntent piDownload;
	
	private final Messenger messenger = new Messenger(new PlayerActivity.ServiceReturnHandle());
	
	private String serial;
	private String identificador;

	/**
	 * @throws InterruptedException 
	 * @throws AndroidException 
	 * 
	 */
	public SimpleWifiReceiver(Context ctx, WifiManager wm, String serial, String identificador) throws InterruptedException, AndroidException {
		this.serial = serial;
		this.identificador = identificador;
		
		// conecta rede WIFI
		if (!wm.isWifiEnabled())
			wm.setWifiEnabled(true);
		
		int tentativas = 0; // 5 tentativas, 1 por segundo
		while ( (tentativas++ < 5) && 
				(!wm.isWifiEnabled()) ) {
						
			Thread.sleep(1000);
		}
		
		if (tentativas == 10) {
			throw new AndroidException("Não foi possível startar o wifi");
		}
		
		
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context ctx, Intent intent) {
		SupplicantState ss = intent.getParcelableExtra("newState");
    	if (ss != null) {
    		Log.d(TAG, "[onReceive] supplicant state: " + ss.toString());
    		switch(ss){
    		case DISCONNECTED:
    			Log.d(TAG, "[onReceive] DESCONECTADO ");
    			cancelServicoDownload(ctx);
    			break;
    		case DORMANT: // desconexão chamada pelo wifiManager.disconnect()
    			cancelServicoDownload(ctx);
    			break;
			default:
				break;
    		}
    	}
    	int error = intent.getIntExtra("supplicantError", -1);
    	
    	if (error == WifiManager.ERROR_AUTHENTICATING) {
    		Log.d(TAG, "[onReceive] Erro de autenticação");
    	}
    	
    	NetworkInfo ni = intent.getParcelableExtra("networkInfo");        	
    	if (ni != null) {
    		Log.d(TAG, "[onReceive] NI: " + ni.getExtraInfo());
    		if (ni.isConnected()) {
    			// se estiver conectado em outro ssid, remove a rede
    			Log.d(TAG, "[onReceive] CONECTADO ");
    			registraServicoDonwload(ctx);
    		}
    	}
		
	}
	
	private void registraServicoDonwload(Context ctx){
		Intent it = new Intent(ctx, AlarmDownloadReceiver.class);
		it.putExtra("messenger", messenger);
		it.putExtra("serial", serial);
		it.putExtra("identificador", identificador);

		piDownload = PendingIntent.getBroadcast(
				ctx, AlarmDownloadReceiver.ALARM_RECEIVER_REQUEST_CODE, 
				it, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
		// Começa contar o tempo a partir de 15 segundos
		alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis()+15000, INTERVALO_SERVICO_DOWNLOAD, piDownload);
	}
	
	public void cancelServicoDownload(Context ctx){
		if (piDownload != null) {
			AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			alarm.cancel(piDownload);
			piDownload.cancel();
		}
	}
	
	

}
