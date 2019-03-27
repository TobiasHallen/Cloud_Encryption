package server;

import java.util.Arrays;
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
		if(filetable.g("name").contains(f.name).run(conn))
		{
			if(filetable.g("owner").contains(f.owner).run(conn))
			{
					System.out.println("Duplicate File Entry");
					util.File file = getFile(f.owner, f.name);
					f.id = file.id;
					System.out.println(Arrays.equals(f.data,file.data));
					System.out.println(Arrays.equals(f.data,file.data));
					System.out.println(Arrays.equals(f.data,file.data));
					System.out.println(Arrays.equals(f.data,file.data));
					filetable.insert(r.hashMap("name", f.name).with("owner", f.owner).with("id", f.id).with("data", r.binary(f.data))).optArg("conflict", "replace").run(conn);
//					update(f);
					return 0;
				
			}
		}
		filetable.insert(r.hashMap("name", f.name).with("owner", f.owner).with("data", r.binary(f.data))).run(conn);
		return 0;
	}
	
	public static void update(util.File f) 
	{
		filetable.get(f.id).update(f).run(conn);
	}

	public static util.File getFile(String Owner, String filename)
	{
		Cursor dbRes = filetable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", Owner)).run(conn);
		HashMap m = (HashMap) dbRes.next();
		util.File f = new util.File((String)m.get("id"), (String)m.get("owner"), (String)m.get("name"), (byte[])m.get("data"));
		return f;
	}

}
