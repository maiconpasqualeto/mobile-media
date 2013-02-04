/**
 * 
 */
package br.com.sixtec.MobileMedia.persistencia;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;


/**
 * @author maicon
 *
 */
public class Midia {

	public Midia() {
		
	}
	
	public Midia(JSONObject obj) {
		try {
			id = obj.getLong("id");
			nomeArquivo = obj.getString("nomeArquivo");
			tempoReproducao = obj.getInt("tempoReproducao");
		} catch (JSONException e){
			Log.e(MobileMediaHelper.TAG, "Erro no parse do Json - Midia", e);
		}
	}
	
	private Long id;
	
	private String nomeArquivo;
	
	private Integer tempoReproducao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public Integer getTempoReproducao() {
		return tempoReproducao;
	}

	public void setTempoReproducao(Integer tempoReproducao) {
		this.tempoReproducao = tempoReproducao;
	}

		
}
