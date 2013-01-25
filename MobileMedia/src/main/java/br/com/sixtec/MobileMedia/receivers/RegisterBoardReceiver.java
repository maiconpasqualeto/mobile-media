/**
 * 
 */
package br.com.sixtec.MobileMedia.receivers;

import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author maicon
 *
 */
public class RegisterBoardReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent it) {
		Log.d(MobileMediaHelper.TAG, "On receive - RegisterBoardReceiver");
		
	}

}
