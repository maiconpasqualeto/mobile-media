/**
 * 
 */
package br.com.sixtec.MobileMedia;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.receivers.WifiReceiver;
import br.com.sixtec.MobileMedia.service.ConexaoService;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
/**
 * @author maicon
 *
 */
public class PlayerActivity extends Activity implements OnErrorListener,
        OnBufferingUpdateListener, OnCompletionListener,
        MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
	
    private static final String TAG = "MobileMedia";
    
    private static final int ID_INTENT_CONFIG = 100;
    
    private MediaPlayer mp;
    private SurfaceView mPreview;
    private SurfaceHolder holder;
    
    private List<String> arquivos;
    private int indexArquivo; 
    
    // wifi
    private BroadcastReceiver receiver;
    private String ssidRede = null;
    private String passRede = null;
    private boolean receiverRegistrado = false;
    private String serial = "sem serial";
    private String identificador = "";

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.player_full);

        // Set the transparency
        getWindow().setFormat(PixelFormat.TRANSPARENT);

        mPreview = (SurfaceView) findViewById(R.id.newSurface);
        // Set a size for the video screen
        holder = mPreview.getHolder();
        holder.addCallback(this);
        // Não tirar essa linha, resolve o problema do erro (1, -38)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        //holder.setFixedSize(65, 50);
        
        // Create a new media player and set the listeners
        mp = new MediaPlayer();
        mp.setOnErrorListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnPreparedListener(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);  
        // Set the surface for the video output
        //mp.setDisplay(holder);
        
        // Preparar o arquivo
        FilenameFilter fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(MobileMediaHelper.EXTENSAO_ARQUIVO_MIDIA);
			}
		};
		File midiasDir = new File(MobileMediaHelper.DIRETORIO_MIDIAS);
		arquivos = Arrays.asList(midiasDir.list(fileFilter));
		if (arquivos.isEmpty()){
			Log.e(TAG, "Não existem midias para execução");
			// TODO [Maicon] - criar um playlist padrao.
			Toast.makeText(this, "Não existem midias para execução", Toast.LENGTH_SHORT).show();
			finish();
		}
		
    	for (String nomeArq : arquivos)
    		Log.v(TAG, "file: " + nomeArq);
    	
    	indexArquivo = -1;
    	
    	defineArquivoParaExecucao(mp);
    	
    	
    	serial = Settings.System.getString(getContentResolver(),
                Settings.System.ANDROID_ID);
            
        Log.v(TAG, "Device Serial: " + serial);
        
        atualizaConfigRede();
        
        receivers();
    }
    
    private void defineArquivoParaExecucao(MediaPlayer mediaPlayer) {
    	try {
    		if( (++indexArquivo) == arquivos.size() )
    			indexArquivo = 0;
    		
        	String arqMidia = MobileMediaHelper.DIRETORIO_MIDIAS + arquivos.get(indexArquivo);
        	
        	//setDataSource(path);
        	mediaPlayer.setDataSource(arqMidia);
        } catch (IOException ex) {
        	Log.e(TAG, "error: " + ex.getMessage(), ex);
            if (mediaPlayer != null) {
            	mediaPlayer.release();
            }
        }
    }

    private void prepareToPlay() {
        try {

            Runnable r = new Runnable() {
                public void run() {
                    try {
                       
                        mp.prepare();
                        //Log.v(TAG, "Duration:  ===>" + mp.getDuration());
                        
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
            if (mp != null) {
                mp.release();
            }
        }
    }

    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.e(TAG, "onError--->   what:" + what + "    extra:" + extra);
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        return false;
    }

    public void onBufferingUpdate(MediaPlayer arg0, int percent) {
        Log.d(TAG, "onBufferingUpdate called --->   percent:" + percent);
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion called");
        mediaPlayer.stop();
    	mediaPlayer.reset();
        defineArquivoParaExecucao(mediaPlayer);
        prepareToPlay();
    }

    public void onPrepared(MediaPlayer mediaplayer) {
        Log.d(TAG, "onPrepared called");
        // play
        mPreview.requestFocus();
        mediaplayer.start();
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceCreated called");
        mp.setDisplay(surfaceholder);
        prepareToPlay();
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
        mp.release();
        mp = null;
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	
    	if (!receiverRegistrado) {
			IntentFilter f = new IntentFilter();
			f.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			f.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			f.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			f.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);			
			
			registerReceiver(receiver, f);
			receiverRegistrado = true;
		}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mp.pause();
    	
    	if (receiverRegistrado) {
			unregisterReceiver(receiver);
			receiverRegistrado = false;
		}
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	if (mp != null)
    		mp.start();
    	
    	atualizaConfigRede();
    }
    
    
    // métodos do Wifi
    public void receivers(){
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (receiver == null)
			receiver = new WifiReceiver(wifi, ssidRede, passRede);
		
	}
    
    private void atualizaConfigRede(){
        MMConfiguracao conf = MobileMediaDAO.getInstance(this).buscaConfiguracao();
        ssidRede = conf.getSsid();
        passRede = conf.getPass();
        identificador = conf.getIdentificador();
    }
    
    private void downloadDasMidias(){
    	Intent it = new Intent(this, ConexaoService.class);
    	it.putExtra("serial", serial);
    	it.putExtra("identificador", identificador);
    	startService(it);
    }
    
    
    // métodos da Activity
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 0, Menu.NONE, "Configurar");
    	    	
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getItemId() == 0) {
    		startActivityForResult(new Intent(this, ConfigActivity.class), ID_INTENT_CONFIG);
    	}
    	return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == ID_INTENT_CONFIG &&
    			resultCode == RESULT_OK){
    		
    		// Fazer sincronia com o WS e startar o download Midias.
    		
    	}
    }
    
}
