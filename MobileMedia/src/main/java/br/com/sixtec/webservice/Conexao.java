/**
 * 
 */
package br.com.sixtec.webservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author maicon
 * 
 */
public class Conexao {

	private String toString(InputStream is) throws IOException {

		byte[] bytes = new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int lidos;
		while ((lidos = is.read(bytes)) > 0) {
			baos.write(bytes, 0, lidos);
		}
		return new String(baos.toByteArray());
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	private String getREST(String url) {
		String retorno = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);

		try {
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				InputStream instream = entity.getContent();
				String result = toString(instream);

				instream.close();

				retorno = result;
			}
		} catch (Exception e) {
			Log.e("MM", "Falha ao acessar Web service", e);
		}
		return retorno;
	}

	public void lerUmCarro() {
		String result = getREST("http://192.168.20.215:8080/WebMedia/restfull/sample/do-something");
		//String result = getREST("http://10.1.1.100:8080/WebMedia/rest");
		if (result == null) {
			Log.e("NGVL", "Falha ao acessar WS");
			return;
		}

		try {
			JSONObject carro = new JSONObject(result);

			Log.i("NGVL", "id=" + carro.getInt("id"));
			Log.i("NGVL", "nome=" + carro.getString("nome"));
			
		} catch (JSONException e) {
			Log.e("NGVL", "Erro ao fazer parsing do JSON", e);
		}
	}
}
