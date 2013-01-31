/**
 * 
 */
package br.com.sixtec.MobileMedia.persistencia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author maicon
 *
 */
public class MobileMediaDAO {
	
	private static MobileMediaDAO dao;
	
	public static MobileMediaDAO getInstance(Context ctx){
		if (dao == null)
			dao = new MobileMediaDAO(ctx);
		return dao;
	}
	
	private static DatabaseManager dbman;

	/**
	 * 
	 */
	public MobileMediaDAO(Context ctx) {
		dbman = new DatabaseManager(ctx);
	}
	
	/**
	 * Altera os dados do carro passado pelo parâmetro
	 * 
	 * @param c
	 */
	public void alterarConfiguracao(MMConfiguracao c){
		SQLiteDatabase db = dbman.getWritableDatabase();
		
		ContentValues v = new ContentValues();
		v.put("ip", c.getIp());
		v.put("porta", c.getPorta());
		v.put("ssid", c.getSsid());
		v.put("pass", c.getPass());
		
		String clausulaWhere = "_id=?";
		String[] args = new String[]{ c.getId().toString() };
		
		db.update(DatabaseManager.TABLE_NAME, v, clausulaWhere, args);
	}
	
	/**
	 * Busca um carro pelo id
	 * 
	 * @return
	 */
	public MMConfiguracao buscaConfiguracao(){
		SQLiteDatabase db = dbman.getReadableDatabase();
		MMConfiguracao conf = null;
		Cursor c = db.query(DatabaseManager.TABLE_NAME, 
				new String[] {"_id", "ip", "porta", "ssid", "pass"}, 
				null, null, null, null, null);
		
		if (c.moveToFirst()) {
			
			Long id = c.getLong(0);
			String ip = c.getString(1);
			Integer porta = c.getInt(2);
			String ssid = c.getString(3);
			String pass = c.getString(4);
			
			conf = new MMConfiguracao();
			conf.setId(id);
			conf.setIp(ip);
			conf.setPorta(porta);
			conf.setSsid(ssid);
			conf.setPass(pass);
						
		}
		
		c.close();
		
		return conf;
	}
	
	public void close(){
		dbman.close();
		MobileMediaDAO.dao = null;
	}


}
