/**
 * 
 */
package br.com.sixtec.MobileMedia.persistencia;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author maicon
 *
 */
public class DatabaseManager extends SQLiteOpenHelper {
	
	public static final String TABLE_NAME = "config";
	
	private static final String SCRIPT_CREATE_DB = 
			"CREATE TABLE " + TABLE_NAME + " (" +
			"_id INTEGER PRIMARY KEY, " +
			"ip TEXT, " +
			"porta INTEGER, " +
			"ssid TEXT, " +
			"pass TEXT);";
	
	private static final String NOME_BANCO = "mobile_media_db";
	
	private static final int VERSAO_BANCO = 2;

	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DatabaseManager(Context context) {
		super(context, NOME_BANCO, null, VERSAO_BANCO);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SCRIPT_CREATE_DB);
		// setando a configuração padrão
		db.execSQL("INSERT INTO " + TABLE_NAME + 
				" ('ip', 'porta', 'ssid', 'pass') values " +
				" ('10.1.1.104', 8080, 'Ma & Ma', 'a0b1c2d3e4')");
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			String sql = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN identificador TEXT";
			db.execSQL(sql);
		}

	}

}
