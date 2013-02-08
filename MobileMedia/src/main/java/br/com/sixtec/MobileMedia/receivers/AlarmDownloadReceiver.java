/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.util.Log;
import br.com.sixtec.MobileMedia.service.ConexaoService;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class AlarmDownloadReceiver extends BroadcastReceiver {
	
	public static final int ALARM_RECEIVER_REQUEST_CODE = 200;
	

	@Override
	public void onReceive(Context ctx, Intent it) {
		Log.d(MobileMediaHelper.TAG, "[AlarmDownloadReceiver] Chama serviço Download.");
		
        Intent serviceIntent = new Intent(ctx, ConexaoService.class);
        serviceIntent.putExtra("serial", it.getStringExtra("serial"));
        serviceIntent.putExtra("identificador", it.getStringExtra("identificador"));
        serviceIntent.putExtra("messenger", (Messenger) it.getParcelableExtra("messenger"));
    	ctx.startService(serviceIntent);
	}

}
