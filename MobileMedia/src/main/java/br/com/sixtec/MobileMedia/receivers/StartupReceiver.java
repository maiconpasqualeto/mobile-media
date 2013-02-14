/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import br.com.sixtec.MobileMedia.PlayerActivity;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author maicon
 *
 */
public class StartupReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.d(MobileMediaHelper.TAG, "[StartupReceiver] onReceive");
		Intent it = new Intent(ctx, PlayerActivity.class);
		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(it);
	}

}
