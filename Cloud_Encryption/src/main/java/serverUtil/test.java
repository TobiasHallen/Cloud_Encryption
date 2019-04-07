package util;

import java.security.PublicKey;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.ast.ReqlAst;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

public class test 
{
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table filekeytable = r.db("Cloud_Encryption").table("filekeys");

	public static void main(String[] args) 
	{
		PublicKey pubkey = null;
//		Cursor cursor = r.db("Cloud_Encryption").table("users").run(conn);
//		for (Object doc : cursor) {
//		    System.out.println(doc+"         hello");
//		    
//		}
		byte[] x = "".getBytes();
		User u = new User("testerino", x);
		byte[] temp = DBHost.getBytes();
//		FileKey fk = new FileKey("15", "Tokmaru", "Tokmaru", "hellothere.jpg", temp);
//		FileKey fk1 = new FileKey("15", "Tokmarek", "Tokmarek", "hellothere.jpg", temp);
		
		server.User.insert(u);
//		u = server.User.GetUser("testerino", conn );
//		server.FileKey.insert(fk);
//		Object dbRes = filekeytable.getAll(fk.Name).optArg("index", "name").filter(r.hashMap("owner", fk.Owner).with("user", fk.User)).run(conn);
//		System.out.println(dbRes);
		System.exit(0);

	}
}
