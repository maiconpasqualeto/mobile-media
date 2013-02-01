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
import br.com.sixtec.MobileMedia.receivers.WifiReceiver;
import br.com.sixtec.MobileMedia.service.ConexaoService;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "MobileMedia";
    
    //private static final String PASS_REDE = "a0b1c2d3e4";
    
    //private static final String SSID_REDE = "Nastek_Andar2A";
    //private static final String PASS_REDE = "abcdef2405";
        
    //private static final WifiType WIFI_TYPE = WifiType.WPA_PSK;
    
    
    
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
				//desconectarWifi();
			}			
        });
        
        Button btnDownloadMidia = (Button) findViewById(R.id.btnDownloadMidia);
        btnDownloadMidia.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//downloadDasMidias();
			}			
        });
        
        
    }
    
    @Override
	protected void onRestart() {
		super.onRestart();
		
		
		
		Log.d(TAG, "onRestart");
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		
		
		Log.d(TAG, "onStart");
	}
	
	
	
	@Override
	protected void onPause() {
		super.onPause();
		
		
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
        
    
        
	
		
	
	

}

