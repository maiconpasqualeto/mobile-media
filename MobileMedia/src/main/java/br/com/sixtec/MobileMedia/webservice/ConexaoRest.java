/**
 * 
 */
package br.com.sixtec.MobileMedia.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 * 
 */
public class ConexaoRest {
	
	private static final String TAG = "MobileMedia";
	
	//private String host = "http://10.1.1.104:8080/WebMedia/restfull/";
	private String host = "http://192.168.20.215:8080/WebMedia/restfull/";

	/**
	 * 
	 * @param nomeRest nome do rest (path) ex: board/downloadmidia/1
	 * @return
	 */
	public byte[] getREST(String nomeRest) {
		byte[] retorno = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(host + nomeRest);
		InputStream is = null;
		try {
			HttpResponse response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				is = entity.getContent();
				retorno = MobileMediaHelper.toByteArray(is);
			}
		} catch (Exception e) {
			Log.e(TAG, "Falha ao acessar Web service", e);
		} finally { 
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Falha ao fechar stream", e);
			}
		}
		return retorno;
	}
		
	public byte[] postREST(String nomeRest, NameValuePair...props) {
		byte[] b = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(host + nomeRest);
		InputStream is = null;
		try {
			
			List<NameValuePair> propsList = new ArrayList<NameValuePair>();
			propsList.addAll(Arrays.asList(props));
			post.setEntity(new UrlEncodedFormEntity(propsList));
			
			HttpResponse response = httpclient.execute(post);
			
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				is = entity.getContent();
				b = MobileMediaHelper.toByteArray(is);
			}
		} catch (Exception e) {
			Log.e(TAG, "Falha ao acessar Web service", e);
		} finally { 
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Falha ao fechar stream", e);
			}
		}
		return b;
	}
}
