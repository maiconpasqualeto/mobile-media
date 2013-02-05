/**
 * 
 */
package br.com.sixtec.MobileMedia.persistencia;

import java.text.ParseException;
import java.util.Date;

import android.util.Log;
import br.com.sixtec.MobileMedia.utils.MobileMediaHelper;

/**
 * @author maicon
 *
 */
public class MMConfiguracao {

	private Long id;
	
	private String ip;
	
	private Integer porta;
	
	private String ssid;
	
	private String pass;
	
	private String identificador;
	
	private Long idPlaylist;
	
	private Date dataHoraPlaylist;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPorta() {
		return porta;
	}

	public void setPorta(Integer porta) {
		this.porta = porta;
	}

	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getIdentificador() {
		return identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	public Date getDataHoraPlaylist() {
		return dataHoraPlaylist;
	}

	public void setDataHoraPlaylist(Date dataHoraPlaylist) {
		this.dataHoraPlaylist = dataHoraPlaylist;
	}
	
	public Long getIdPlaylist() {
		return idPlaylist;
	}

	public void setIdPlaylist(Long idPlaylist) {
		this.idPlaylist = idPlaylist;
	}

	public String getDataHoraPlaylistStringSQLite(){
		if (getDataHoraPlaylist() == null)
			return "";
		return MobileMediaHelper.SQLITE_DATE_FORMAT.format(getDataHoraPlaylist());
	}
	
	public void setDataHoraPlaylistStringSQLite(String strDataHoraPlaylist) {
		if (strDataHoraPlaylist == null || "".equals(strDataHoraPlaylist))
			return;
		
		try {
			setDataHoraPlaylist(MobileMediaHelper.SQLITE_DATE_FORMAT.parse(strDataHoraPlaylist));
			
		} catch (ParseException e) {
			Log.e(MobileMediaHelper.TAG, "Erro ao converter data");
		}
		
	}

}
