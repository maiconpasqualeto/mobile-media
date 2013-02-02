/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.provider.Settings;
import android.util.Log;
import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import br.com.sixtec.MobileMedia.service.ConexaoService;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	public static final int ALARM_RECEIVER_REQUEST_CODE = 200;
	

	@Override
	public void onReceive(Context ctx, Intent it) {
		Log.d(MobileMediaHelper.TAG, "Receiver Alarm.");
		
		String serial = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.System.ANDROID_ID);
        
        Log.v(MobileMediaHelper.TAG, "Device Serial: " + serial);
		
		MMConfiguracao conf = MobileMediaDAO.getInstance(ctx).buscaConfiguracao();
		
        String identificador = conf.getIdentificador();
        Intent serviceIntent = new Intent(ctx, ConexaoService.class);
        serviceIntent.putExtra("serial", serial);
        serviceIntent.putExtra("identificador", identificador);
        serviceIntent.putExtra("messenger", (Messenger) it.getParcelableExtra("messenger"));
    	ctx.startService(serviceIntent);
	}

}
