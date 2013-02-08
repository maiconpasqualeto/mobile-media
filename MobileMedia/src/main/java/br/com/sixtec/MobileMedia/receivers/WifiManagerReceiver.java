/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.AndroidException;
import android.util.Log;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class WifiManagerReceiver extends BroadcastReceiver {

	private static final String TAG = MobileMediaHelper.TAG;
	
	private String ssid = null;
	private String pass = null;
	
	private WifiConfiguration wc = null;
	
	
	public WifiManagerReceiver(WifiManager wm, String ssid, String pass) throws InterruptedException, AndroidException {
		
		this.ssid = ssid;
		this.pass = pass;
		
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
		
		// Se a rede já estiver configurada só ativa a rede.
		verificaRedeJaConfigurada(wm);
		
	}

	@Override
	public void onReceive(Context ctx, Intent intent) {
		//Log.d(TAG, "[onReceive] Receiver: " + intent.getAction());
		
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		
		if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
			Log.d(TAG, "[onReceive] SCAN_RESULTS_AVAILABLE_ACTION");
			
			verificaResultadosScan(wifiManager);
			
        } else {
        	        	
        	SupplicantState ss = intent.getParcelableExtra("newState");
        	if (ss != null) {
        		//Log.d(TAG, "[onReceive] supplicant state: " + ss.toString());
        		if (ss.equals(SupplicantState.DISCONNECTED)) {
        			Log.d(TAG, "[onReceive] DESCONECTADO ");
        		}
        	}
        	int error = intent.getIntExtra("supplicantError", -1);
        	
        	if (error == WifiManager.ERROR_AUTHENTICATING) {
        		Log.d(TAG, "[onReceive] Erro de autenticação");
        	}
        	
        	NetworkInfo ni = intent.getParcelableExtra("networkInfo");        	
        	if (ni != null) {
        		//Log.d(TAG, "[onReceive] NI: " + ni.getExtraInfo());
        		if (ni.isConnected())
        			Log.d(TAG, "[onReceive] CONECTADO ");
        	}
        }
		
	}

	/**
	 * verifica se a rede com o SSID passado já está configurada, se estiver só ativa a rede
	 */
	private synchronized boolean verificaRedeJaConfigurada(WifiManager wifiManager) {
		boolean redeJaExiste = false;
		for (WifiConfiguration r : wifiManager.getConfiguredNetworks()) {
    		if (("\"" + ssid + "\"").equals(r.SSID)) {
    			Log.d(TAG, "[verificaRedeJaConfigurada] A rede já existe.");
    			if (WifiConfiguration.Status.CURRENT == r.status ||
    					WifiConfiguration.Status.ENABLED == r.status) {
    				// 0 - CURRENT  1 - DISABLED  2 - ENABLED
    				Log.d(TAG, "[verificaRedeJaConfigurada] WifiConfiguration Status: " + r.status);
    				//int id = r.networkId;    			
    				//wifiManager.enableNetwork(id, true);
        			Log.d(TAG, "[verificaRedeJaConfigurada] O SSID: " + ssid + " já está na lista de AP's gravadas.");
    				
    			} else { // se for DISABLED habilita rede
    				int id = r.networkId;    			
    				wifiManager.enableNetwork(id, true);
    			}
    			redeJaExiste = true;
    			return redeJaExiste;
    		}    		
    	}
		return redeJaExiste;
		
	}
	
	private void verificaResultadosScan(WifiManager wifiManager){
		if (verificaRedeJaConfigurada(wifiManager)) 
			return;
		
		// se a rede não existe, cria uma nova rede
		List<ScanResult> results = wifiManager.getScanResults();
		
		if (results == null)
			return;
		
        for (ScanResult r : results){
        	if(ssid.equals(r.SSID)){
        		
        		WifiType wifiType = null;
        		
        		//Log.d(TAG, "Capabilities: " + r.capabilities);
        		if (r.capabilities.contains(WifiType.WEP.name()))
        			wifiType = WifiType.WEP;
        		else if (r.capabilities.contains("WPA"))
        			wifiType = WifiType.WPA_PSK;
        		
        		wc = new WifiConfiguration();
        		
        		configuraRedeWifi(wc, r.SSID, r.BSSID, pass, wifiType);
        		
        		int id = wifiManager.addNetwork(wc);
        		
        		Log.d(TAG, "[verificaResultadosScan] Rede criada com o ID:" + id);
        		
        		// a rede foi criada com sucesso
        		if (id > -1) {
        			Log.d(TAG, "[verificaResultadosScan] Conecta com SSID: " + ssid);
        			//wifi.setWifiEnabled(true);
        			wifiManager.enableNetwork(id, true);
	        		wifiManager.saveConfiguration();
        			return;
        		}		        		
        	}
        }
        Log.d(TAG, "[verificaResultadosScan] Rede " + ssid + " não encontrada");
	}
	
	
	private WifiConfiguration configuraRedeWifi(WifiConfiguration wc, String ssid, String bSsid, String pass, WifiType wifiType) {
		Log.d(TAG, "[configuraRedeWifi] Configurando rede : " + wifiType.name());
    	switch (wifiType) {
		case WEP:
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
		    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		    			
			wc.wepKeys[0] = pass;
			wc.wepTxKeyIndex = 0;
			
			break;
		case WPA_PSK:
	        wc.preSharedKey = "\"" + pass + "\"";
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	        
			break;
		default: 
			Log.e(TAG, "[configuraRedeWifi] Tipo de rede WIFI não encontrado");
			break;
		}
    	wc.SSID = "\"" + ssid + "\"";
		wc.BSSID = bSsid;
		wc.priority = 1;
		wc.status = WifiConfiguration.Status.ENABLED;
		
		return wc;
    }
		
	private enum WifiType {
    	WEP, WPA_PSK;
    }
	
	public void atualizaConfiguracaoRede(Context ctx, String ssidNovo, String senhaNova){
		WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		// procura a rede antiga
		int idRede = -1;
		for (WifiConfiguration r : wifiManager.getConfiguredNetworks()) {
    		if (("\"" + ssid + "\"").equals(r.SSID)) {
    			idRede = r.networkId;
    			break;
    		}
    	}
		
		wifiManager.removeNetwork(idRede);
		wifiManager.startScan();
		ssid = ssidNovo;
		pass = senhaNova;
	}

}
