package server;

import java.util.HashMap;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

public class File 
{
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table filetable = r.db("Cloud_Encryption").table("files");

	
	public static int insert(util.File f)
	{
		System.out.println(f.name);
		System.out.println(f.owner);
		

//		if(filetable.g("name").contains(f.name).run(conn))
//		{
////			HashMap m = (HashMap) dbRes.next();
////			System.out.println(m);
//			System.out.println("Duplicate File Entry");
////			filetable.insert(r.hashMap("name", f.name).with("id", f.id).with("owner", f.owner)).optArg("conflict", "replace").run(conn);
//			return 1;
//		}
		
//		System.out.println(new String(f.data));	
		 filetable.insert(
				 r.hashMap("name", f.name)
				 .with("data", r.binary(f.data))
				 .with("owner", f.owner)
				 ).run(conn);
		 return 0;
	}
	
	public static util.File getFile(String Owner, String filename)
	{
		Cursor dbRes = filetable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", Owner)).run(conn);
		HashMap m = (HashMap) dbRes.next();
		util.File f = new util.File((String)m.get("id"), (String)m.get("owner"), (String)m.get("name"), (byte[])m.get("data"));
		return f;
	}

}
