/**
 * 
 */
package br.com.sixtec.MobileMedia;

import br.com.sixtec.MobileMedia.persistencia.MMConfiguracao;
import br.com.sixtec.MobileMedia.persistencia.MobileMediaDAO;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author maicon
 *
 */
public class ConfigActivity extends Activity {
	
	private EditText txtIP;
	private EditText txtPorta;
	private EditText txtSSID;
	private EditText txtSenha;
	private EditText txtIdentificador;
	
	private MMConfiguracao c;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.config);
		
		txtIP = (EditText) findViewById(R.conf.txtIP);
		txtPorta = (EditText) findViewById(R.conf.txtPorta);
		txtSSID = (EditText) findViewById(R.conf.txtSSID);
		txtSenha = (EditText) findViewById(R.conf.txtSenha);
		txtIdentificador = (EditText) findViewById(R.conf.txtIdentificador);
		
		Button btnVoltar = (Button) findViewById(R.conf.btnVoltar);
		btnVoltar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Button btnSalvar = (Button) findViewById(R.conf.btnSalvar);
		btnSalvar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String ip = txtIP.getText().toString();
				String porta = txtPorta.getText().toString();
				String ssid = txtSSID.getText().toString();
				String senha = txtSenha.getText().toString();
				String identificador = txtIdentificador.getText().toString();
				
				c.setIp(ip);
				c.setPorta(Integer.valueOf(porta));
				c.setSsid(ssid);
				c.setPass(senha);
				c.setIdentificador(identificador);
				
				MobileMediaDAO.getInstance(ConfigActivity.this).alterarConfiguracao(c);
				
				setResult(RESULT_OK);
				finish();
			}
		});
		
		c = MobileMediaDAO.getInstance(this).buscaConfiguracao();
		txtIP.setText(c.getIp());
		txtPorta.setText(c.getPorta().toString());
		txtSSID.setText(c.getSsid());
		txtSenha.setText(c.getPass());
		txtIdentificador.setText(c.getIdentificador());
		
	}

}
