/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import java.util.List;

import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * @author maicon
 *
 */
public class WifiReceiver extends BroadcastReceiver {
	
	private static String TAG = "MobileMedia";
	
	private WifiManager wifi;
	
	private String ssidRede;
	private String passRede;
	
	public WifiReceiver(WifiManager wifi, String ssidRede, String passRede) {
		this.ssidRede = ssidRede;
		this.passRede = passRede;
		this.wifi = wifi;
		
		// habilita o rádio wifi
		//if (!wifi.isWifiEnabled())
		//	wifi.setWifiEnabled(true);
	}
	
	public String getSsidRede() {
		return ssidRede;
	}

	public void setSsidRede(String ssidRede) {
		this.ssidRede = ssidRede;
	}

	public String getPassRede() {
		return passRede;
	}

	public void setPassRede(String passRede) {
		this.passRede = passRede;
	}

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d(TAG, "Receiver: " + intent.getAction());
		if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
			Log.d(TAG, "Scan results avalilable");
			
			conectaRedeWifi();
        }
        else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {			        	
        	int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        	Log.d(TAG, "Extra Wifi State: " + returnWifiState(wifiState));
        	
        	switch(wifiState){
				case WifiManager.WIFI_STATE_DISABLED:
					Log.d(TAG, "Wifi desligado, ligando Wifi...");
					wifi.setWifiEnabled(true);
					//wifi.startScan();
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					conectaRedeWifi();
					break;
        	}
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())){
        	Log.d(TAG, "Suplicate State changed. State: " + wifi.getConnectionInfo().getSupplicantState());
			switch (wifi.getConnectionInfo().getSupplicantState()){
			case INACTIVE:
				conectaRedeWifi();
				break;
			case DORMANT:
				Log.d(TAG, "Reconectando");
				conectaRedeWifi();
				break;
			case SCANNING:
				
				break;
			case COMPLETED:
				
				break;
			default:
				break;
			}
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
	        
			DetailedState ds = WifiInfo.getDetailedStateOf(wifi.getConnectionInfo().getSupplicantState());
			Log.d(MobileMediaHelper.TAG, "Detailed Supplicant state: " + ds);
			
			Log.d(MobileMediaHelper.TAG, "Network info: IP: " + 
	        		Formatter.formatIpAddress(wifi.getConnectionInfo().getIpAddress()));
	        
	        if (wifi.getConnectionInfo().getIpAddress() > 0){
	        	//downloadDasMidias();
	        } 
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())){
        	Log.d(MobileMediaHelper.TAG, "Results available");
        }

	}
	
	public void conectaRedeWifi(){
    	
		for (WifiConfiguration r : wifi.getConfiguredNetworks()) {
    		if (("\"" + ssidRede + "\"").equals(r.SSID)) {
    			Log.d(TAG, "A rede já existe.");
    			if (WifiConfiguration.Status.ENABLED == r.status) {
    				Log.d(TAG, "Não faz nada.");
    				return;
    			}
    			int id = r.networkId;    			
    			wifi.enableNetwork(id, true);
    			Log.d(TAG, "Habilitou o SSID.");
    			return;
    		}    		
    	}
		
		// se a rede não existe, cria uma nova rede
		List<ScanResult> results = wifi.getScanResults();
		
		if (results == null)
			return;
		
        for (ScanResult r : results){
        	if(ssidRede.equals(r.SSID)){
        		
        		WifiType wifiType = null;
        		
        		Log.d(TAG, "Capabilities: " + r.capabilities);
        		if (r.capabilities.contains(WifiType.WEP.name()))
        			wifiType = WifiType.WEP;
        		else if (r.capabilities.contains("WPA-PSK"))
        			wifiType = WifiType.WPA_PSK;
        		
        		WifiConfiguration wc = new WifiConfiguration();
        		
        		wc.SSID = "\"" + r.SSID + "\"";
        		wc.BSSID = r.BSSID;
        		wc.priority = 100;
        		
        		configuraRedeWifi(wc, passRede, wifiType);
        		
        		int id = wifi.addNetwork(wc);
        		
        		Log.d(TAG, "Wifi result ID:" + id);
        		
        		// a rede foi criada com sucesso
        		if (id > -1) {
        			wifi.saveConfiguration();
        			wifi.enableNetwork(id, true);
	        		//wifi.setWifiEnabled(true);
        			break;
        		}		        		
        	}
        }
	}

	private void desconectarWifi() {
		if (wifi != null) {
			wifi.disconnect();
			wifi.setWifiEnabled(false);
		}
	}
	
	private WifiConfiguration configuraRedeWifi(WifiConfiguration wc, String pass, WifiType wifiType) {
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
			Log.e(TAG, "Tipo de rede WIFI não encontrado");
			break;
		}
		
		wc.status = WifiConfiguration.Status.ENABLED;
		
		return wc;
    }
	
	private String returnWifiState(int state) {
    	String retorno = "Desconhecido";
    	switch (state) {
    	case WifiManager.WIFI_STATE_DISABLED:
    		retorno = "Desligado";
    	break;
    	case WifiManager.WIFI_STATE_DISABLING:
    		retorno = "Desligando";
    	break;
    	case WifiManager.WIFI_STATE_ENABLED:
    		retorno = "Ligado";
    	break;
    	case WifiManager.WIFI_STATE_ENABLING:
    		retorno = "Ligando";
    	break;
    	}
    	return retorno;
    }
    
    
    private enum WifiType {
    	WEP, WPA_PSK;
    }

}
