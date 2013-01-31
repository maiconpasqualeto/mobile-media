package br.com.sixtec.MobileMedia;

import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import br.com.sixtec.MobileMedia.facade.MobileFacade;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.service.ConexaoService;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "MobileMedia";
    
    private BroadcastReceiver receiver;
    
    private WifiManager wifi;
    private String ssidRede = null;
    private String passRede = null;
    //private static final String PASS_REDE = "a0b1c2d3e4";
    
    //private static final String SSID_REDE = "Nastek_Andar2A";
    //private static final String PASS_REDE = "abcdef2405";
        
    //private static final WifiType WIFI_TYPE = WifiType.WPA_PSK;
    
    boolean receiverRegistrado = false;
    
    private String serial = "sem serial";
    private String identificador = "tabra";
    
    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);
        
       /*Button btnConexao = (Button) findViewById(R.id.btnTeste);
        btnConexao.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				registrarBoard();
			}
		});
        
        Button btnCriarRede = (Button) findViewById(R.id.btnCriarRede);
        btnCriarRede.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				conectarWifi();
			}			
        });*/
        
        Button btnPlayer = (Button) findViewById(R.id.btnPlayer);
        btnPlayer.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(HelloAndroidActivity.this, PlayerActivity.class));
			}
        });
        
        
        
        Button btnDesconectar = (Button) findViewById(R.id.btnDesconectar);
        btnDesconectar.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				desconectarWifi();
			}			
        });
        
        Button btnDownloadMidia = (Button) findViewById(R.id.btnDownloadMidia);
        btnDownloadMidia.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				downloadDasMidias();
			}			
        });
        
        MobileMediaDAO dao = MobileMediaDAO.getInstance(this);
        MMConfiguracao conf = dao.buscaConfiguracao();
        ssidRede = conf.getSsid();
        passRede = conf.getPass();

        serial = Settings.System.getString(getContentResolver(),
            Settings.System.ANDROID_ID);
        
        Log.v(TAG, "Device Serial: " + serial);
        
        // Cria variável Wifi local
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        
        receivers();
        
    }
    
    @Override
	protected void onRestart() {
		super.onRestart();
				
		Log.d(TAG, "onRestart");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (!receiverRegistrado) {
			IntentFilter f = new IntentFilter();
			f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			f.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			f.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			
			registerReceiver(receiver, f);
			receiverRegistrado = true;
		}
		
		if (!wifi.isWifiEnabled())
			wifi.setWifiEnabled(true);
		
		//wifi.startScan();
		
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (receiverRegistrado) {
			unregisterReceiver(receiver);
			receiverRegistrado = false;
		}
		
		Log.d(TAG, "onPause");
	}
	
    @Override
    protected void onStop() {
    	super.onStop();		
		
		Log.d(TAG, "onStop");
    }
    
    @Override
    protected void onDestroy() {    	
    	super.onDestroy();
    	Log.d(TAG, "onDestroy");
    }
        
    private void downloadDasMidias(){
    	/*new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					jsonMidias = MobileFacade.getInstance().registraBoard(serial, identificador);
										
					for (int i=0; i<jsonMidias.length(); i++) {
						JSONObject o = jsonMidias.getJSONObject(i);
						final String idMidia = o.getString("id");
						final String nomeArquivo = o.getString("nomeArquivo");
						MobileFacade.getInstance().downloadMidia(idMidia, nomeArquivo);						
					}
					
					MobileFacade.getInstance().moveArquivosPlaylist();
					
					Toast.makeText(HelloAndroidActivity.this, "Dowload concluído.", Toast.LENGTH_LONG).show();
					
				} catch (JSONException e) {
					Log.e(TAG, "Erro no dowload de midias", e);
				}
			}
		}).start();*/
    	Intent it = new Intent(this, ConexaoService.class);
    	it.putExtra("serial", serial);
    	it.putExtra("identificador", identificador);
    	startService(it);
    }
    
    /**
	 * 
	 */
	private void desconectarWifi() {
		if (wifi != null) {
			wifi.disconnect();
			wifi.setWifiEnabled(false);
		}
	}

	private void conectarWifi() { 
		
		if (!wifi.isWifiEnabled()) {
			wifi.setWifiEnabled(true);
			return;
		}
		
		conectaRedeWifi();
    	
	}
		
	public void receivers(){
		if (receiver == null)
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
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
								wifi.setWifiEnabled(true);
								break;
							case WifiManager.WIFI_STATE_ENABLED:
								//wifi.startScan();
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
				        	downloadDasMidias();
				        } 
			        }
				}
		};
		
	}
	
	public void conectaRedeWifi(){
    	
		for (WifiConfiguration r : wifi.getConfiguredNetworks()) {
    		if (("\"" + ssidRede + "\"").equals(r.SSID)) {
    			int id = r.networkId;
    			wifi.enableNetwork(id, true);
    			//wifi.setWifiEnabled(true);
    			Log.d(TAG, "A rede já existe, habilitou o Wifi");
    			//wifi.reconnect();
    			//wifi.reassociate();
    			return;
    		}    		
    	}
		
		// se a rede não existe, cria uma nova rede
		List<ScanResult> results = wifi.getScanResults();
		
        for (ScanResult r : results){
        	if(ssidRede.equals(r.SSID)){
        		
        		WifiType wifiType = null;
        		
        		Log.d(TAG, "Capabilities: " + r.capabilities);
        		if (r.capabilities.contains(WifiType.WEP.name()))
        			wifiType = WifiType.WEP;
        		else if (r.capabilities.contains(WifiType.WPA_PSK.name()))
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
        		}		        		
        	}
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
		    //wc.wepKeys[0] = "\"" + pass + "\"";
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

