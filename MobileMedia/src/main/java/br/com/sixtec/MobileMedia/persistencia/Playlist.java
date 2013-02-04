/**
 * 
 */
package br.com.sixtec.MobileMedia.persistencia;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

import android.util.Log;

/**
 * @author maicon
 *
 */
public class Playlist {

	private Long id;
	
	private String descricao;
	
	private Date dataHoraCriacao;
	
	private List<Midia> midias = new ArrayList<Midia>();
	
	private boolean atualizado = false;
	
	public Playlist() {
		
	}
	
	public Playlist(JSONObject obj) {
		try {
			id = obj.getLong("id");
			descricao = obj.getString("descricao");
			dataHoraCriacao = MobileMediaHelper.JSON_DATE_FORMAT.parse(obj.getString("dataHoraCriacao"));
			
		} catch (JSONException e){
			Log.e(MobileMediaHelper.TAG, "Erro no parse do Json - Playlist", e);
		} catch (ParseException e) {
			Log.e(MobileMediaHelper.TAG, "Erro no parse da dataHoraCriacao - Playlist", e);
		}
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Date getDataHoraCriacao() {
		return dataHoraCriacao;
	}

	public void setDataHoraCriacao(Date dataHoraCriacao) {
		this.dataHoraCriacao = dataHoraCriacao;
	}

	public List<Midia> getMidias() {
		return midias;
	}

	public void setMidias(List<Midia> midias) {
		this.midias = midias;
	}

	public boolean isAtualizado() {
		return atualizado;
	}

	public void setAtualizado(boolean atualizado) {
		this.atualizado = atualizado;
	}	
	
	

}
