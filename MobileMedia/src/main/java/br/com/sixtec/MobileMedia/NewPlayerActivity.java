/**
 * 
 */
package br.com.sixtec.MobileMedia;

import java.io.File;

import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * @author maicon
 *
 */
public class NewPlayerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_player);
		final VideoView vv = (VideoView) findViewById(R.newPlayer.videoView);
				
		File arq = new File(MobileMediaHelper.DIRETORIO_MIDIAS + "JAC.mp4");
		MediaController mc = new MediaController(NewPlayerActivity.this);
		vv.setMediaController(mc);
		vv.setVideoPath(arq.getPath());
		vv.requestFocus();
	}

}
