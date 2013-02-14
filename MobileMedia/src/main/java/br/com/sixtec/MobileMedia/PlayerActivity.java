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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AndroidException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import br.com.sixtec.MobileMedia.facade.MobileFacade;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.receivers.WifiManagerReceiver;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
/**
 * @author maicon
 *
 */
public class PlayerActivity extends Activity implements OnErrorListener,
        OnBufferingUpdateListener, OnCompletionListener,
        MediaPlayer.OnPreparedListener, SurfaceHolder.Callback {
	
    private static final String TAG = MobileMediaHelper.TAG;
    
    private static final int ID_INTENT_CONFIG = 100;
    
    private MediaPlayer mp;
    private SurfaceView sPreview;
    private SurfaceHolder holder;
    
    private List<String> arquivos;
    private int indexArquivo = -1; 
    
    // wifi
    private WifiManagerReceiver receiver;
    
    private boolean receiverRegistrado = false;
        
    private static boolean novosArquivos = false;
    
    private static String serial = null;
    
    private MMConfiguracao conf = null;
    

    /**
     * 
     */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.player_full);
        
        // guarda o serial do device
        // linha para Android 2.x
        //serial = Settings.Secure.getString(getApplicationContext().getContentResolver(),
        //        Settings.System.ANDROID_ID);
        serial = Settings.Secure.getString(getApplicationContext().getContentResolver(),
        		Settings.Secure.ANDROID_ID);
        
        Log.v(MobileMediaHelper.TAG, "Device Serial: " + serial);
                
        sPreview = (SurfaceView) findViewById(R.id.newSurface);
        criaSurfaceEMediaPlayer(sPreview);
        sPreview.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				//Log.d(TAG, "[onTouchEvent] Long event pressed");
				openOptionsMenu();
				return false;
			}
		});
        
        // android 4
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | 
        		View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        
        atualizarListaArquivos();
        
        atualizaConfigRede();
    	
        receivers();
        
    }
    	
	/**
	 * @param sPreview
	 */
	private void criaSurfaceEMediaPlayer(SurfaceView sPreview) {
		// Set the transparency
        getWindow().setFormat(PixelFormat.TRANSPARENT);
		
		holder = sPreview.getHolder();
        holder.addCallback(this);
        // Não tirar essa linha, resolve o problema do erro (1, -38)
        // no android 2.x
        //holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        // Create a new media player and set the listeners
        mp = new MediaPlayer();
        mp.setOnErrorListener(this);
        mp.setOnBufferingUpdateListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnPreparedListener(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);  
	}

	private void defineArquivoParaExecucao(MediaPlayer mediaPlayer) {
    	try {
    		if (novosArquivos) {
    			atualizarListaArquivos();
    			novosArquivos = false;
    		}
    		
    		if (arquivos.size() == 0) {
    			Log.d(MobileMediaHelper.TAG, "Não existem arquivos para execução");
    			return;
    		}
    		
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
        mediaplayer.start();
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceCreated called");
        mp.setDisplay(surfaceholder);
        if (!arquivos.isEmpty()) {
        	defineArquivoParaExecucao(mp);
        	prepareToPlay();
        }
        sPreview.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        sPreview.requestFocus();
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        Log.d(TAG, "surfaceChanged called");
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed called");
        
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	Log.e(TAG, "On Start called");
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
    protected void onResume() {
    	super.onResume();
    	Log.e(TAG, "On Resume called");
    	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.e(TAG, "On Pause called");

    	mp.pause();
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.e(TAG, "On Stop called");
    	
    	if (receiverRegistrado) {
			unregisterReceiver(receiver);
			receiverRegistrado = false;
		}
    	//if (mp.isPlaying()) {
    		mp.stop();
    		mp.reset();    		
    	//}
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	Log.e(TAG, "On Restart called");
    	
    	//atualizaConfigRede();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.e(TAG, "On Destroy called");
    	
    	mp.release();
        mp = null;
        receiver.cancelServicoDownload(this);
    }
    
    
    // métodos do Wifi
    public void receivers(){
    	try {
			WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			//receiver = new WifiReceiver(wifi, conf.getSsid(), conf.getPass());
			receiver = new WifiManagerReceiver(this, wifi, conf.getSsid(), conf.getPass(), serial, conf.getIdentificador());
    	} catch (InterruptedException e) {
    		Log.e(MobileMediaHelper.TAG, "Erro de interrupção ao ligar o wifi", e);
    		Toast.makeText(this, "Erro de interrupção ao ligar o wifi", Toast.LENGTH_LONG)
    		.show();
    	} catch (AndroidException e) {
    		Log.e(MobileMediaHelper.TAG, e.getLocalizedMessage(), e);
    		Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG)
    		.show();
		}
	}
    
    private void atualizaConfigRede(){
        conf = MobileMediaDAO.getInstance(this).buscaConfiguracao();
    }
    
    // métodos da Activity
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | 
    			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, 0, Menu.NONE, "Configurar");
    	menu.add(Menu.NONE, 1, Menu.NONE, "Android Settings");
    	menu.add(Menu.NONE, 2, Menu.NONE, "Sair");
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if (item.getItemId() == 0) { // configurar
    		startActivityForResult(new Intent(this, ConfigActivity.class), ID_INTENT_CONFIG);
    	} else 
    		if (item.getItemId() == 1) { // Android Settings
    		Intent it = new Intent(Settings.ACTION_SETTINGS);
    		startActivity(it);
    	} else
    		if (item.getItemId() == 2) { // Sair
    			// confirmação de saída
    			AlertDialog.Builder alert = new AlertDialog.Builder(this);
    			alert.setTitle("Confirmação");
    			alert.setMessage("Deseja realmente sair do aplicativo?");
    			alert.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					finish();
    				}
    			});
    			alert.setNegativeButton("Não", new DialogInterface.OnClickListener() {
    				@Override
    				public void onClick(DialogInterface dialog, int which) {
    					// não faz nada
    				}
    			});
    			alert.setCancelable(false);
    			alert.show();
        	}
    	return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == ID_INTENT_CONFIG &&
    			resultCode == RESULT_OK){
    		
    		// Fazer sincronia com o WS e startar o download Midias.
    		String ssid = data.getStringExtra("ssid");
    		String senha = data.getStringExtra("senha");
    		
    		String ssidOld = conf.getSsid();
    		String senhaOld = conf.getPass();
    		
    		atualizaConfigRede();
    		//receivers();
    		
    		if (!ssidOld.equals(ssid) ||
    				!senhaOld.equals(senha)) {
    			
    			receiver.atualizaConfiguracaoRede(this, ssid, senha);
    		}    		
    	}
    	getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | 
    			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    
    private void atualizarListaArquivos() {
    	MobileFacade.getInstance(this).moveArquivosPlaylist();
    	
    	// Preparar o arquivo
        FilenameFilter fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(MobileMediaHelper.EXTENSAO_ARQUIVO_MIDIA);
			}
		};
		File midiasDir = new File(MobileMediaHelper.DIRETORIO_MIDIAS);
		String[] fileNames = midiasDir.list(fileFilter);		
		if (fileNames == null){
			Log.e(TAG, "O cartão SD não está montado");
			return;
		}
		arquivos = Arrays.asList(fileNames);
		if (arquivos.isEmpty()){
			Log.e(TAG, "Não existem midias para execução");
			// TODO [Maicon] - criar um playlist padrao.
			return;
		}
		
    	for (String nomeArq : arquivos)
    		Log.d(TAG, "file: " + nomeArq);
    	
    	indexArquivo = -1;
	}
    
    
    /**
     * Classe privada para implementar o Handler
     * @author maicon
     *
     */
    public static class ServiceReturnHandle extends Handler {
    	
    	@Override
    	public void handleMessage(Message msg) {
    		Boolean sucessoDownload = (Boolean) msg.obj;
    		Log.d(MobileMediaHelper.TAG, 
    				"retornou resultado SERVICE para Activity: " + msg.obj);
    		if (sucessoDownload) {
    			novosArquivos = true;
    		}
    			
    	}
    }
}
