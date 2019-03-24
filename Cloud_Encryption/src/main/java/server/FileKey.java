package server;
import com.rethinkdb.*;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;
import java.util.HashMap;

public class FileKey 
{
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table filekeytable = r.db("Cloud_Encryption").table("filekeys");

	
	public static int insert(util.FileKey fk)
	{
		if(filekeytable.g("name").contains(fk.name).run(conn))
		{
			if(filekeytable.g("owner").contains(fk.owner).run(conn))
			{
				if(filekeytable.g("user").contains(fk.user).run(conn))
				{
					System.out.println("Duplicate File Entry");
					filekeytable.insert(r.hashMap("name", fk.name).
							with("user", fk.user).with("owner", fk.owner).with("key", fk.key)).optArg("conflict", "replace").run(conn);
					return 1;
				}
			}
		}

		
		 filekeytable.insert(r.hashMap("name", fk.name).with("user", fk.user).with("owner", fk.owner).with("key", r.binary(fk.key))).run(conn);
		 return 0;
	}
	
	public static int revoke(util.FileKey fk)
	{
		if(fk.user == fk.owner)
		{
			System.out.println("Cannot revoke own file access");
			return 1;
		}
		Cursor dbRes = filekeytable.getAll(fk.name).optArg("index", "name").filter(r.hashMap("owner", fk.owner).with("user", fk.user)).run(conn);
		if(dbRes!=null)
		{
			byte[] b = "".getBytes();
			util.FileKey f = new util.FileKey("", "", "", "", b);
			HashMap m = (HashMap) dbRes.next();
			f.id=(String)m.get("id");
			f.key=(byte[])m.get("key");
			f.name=(String)m.get("name");
			f.owner=(String)m.get("owner");
			f.user=(String)m.get("user");
			filekeytable.get(f.id).delete().run(conn);
		}
		return 0;
	}
	
	public static util.FileKey getFileKey(String owner, String filename, String user)
	{
		Cursor dbRes = filekeytable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", owner).with("user", user)).run(conn);
		if(dbRes==null)
		{
			return null;
		}
		HashMap m = (HashMap) dbRes.next();
		util.FileKey fk = new util.FileKey((String)m.get("id"),(String)m.get("user"), (String)m.get("owner"), (String)m.get("name"), (byte[])m.get("key"));

		return fk;
	}
	
	public static HashMap getFileUsers(String owner, String filename)
	{
		Cursor dbRes = filekeytable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", owner)).pluck("user").run(conn);
		return (HashMap) dbRes.next();
	}

}
