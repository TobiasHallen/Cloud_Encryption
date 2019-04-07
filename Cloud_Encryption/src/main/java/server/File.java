package server;

import java.util.HashMap;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

public class File 
{
	private static String DBHost = "127.0.0.1";
	private static final RethinkDB r = RethinkDB.r;
	private static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	private static Table filetable = r.db("Cloud_Encryption").table("files");
	
	
	static int insert(serverUtil.File f)
	{
		if(filetable.g("name").contains(f.name).run(conn))
		{
			if(filetable.g("owner").contains(f.owner).run(conn))
			{
					System.out.println("Duplicate File Entry");
					serverUtil.File file = getFile(f.owner, f.name);
					f.id = file.id;
					filetable.insert(r.hashMap("name", f.name).with("owner", f.owner).with("id", f.id).with("data", r.binary(f.data))).optArg("conflict", "replace").run(conn);
					return 1;
				
			}
		}
		filetable.insert(r.hashMap("name", f.name).with("owner", f.owner).with("data", r.binary(f.data))).run(conn);
		return 0;
	}
	
	

	@SuppressWarnings("rawtypes")
	static serverUtil.File getFile(String Owner, String filename)
	{
		try {
			Cursor dbRes = filetable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", Owner)).run(conn);
			HashMap m = (HashMap) dbRes.next();
			serverUtil.File f = new serverUtil.File((String)m.get("id"), (String)m.get("owner"), (String)m.get("name"), (byte[])m.get("data"));
			return f;
		} catch (java.util.NoSuchElementException e) {
			System.out.println("Invalid file access");
			System.out.println("File "+filename+" owned by "+Owner+" does not exist, perhaps unauthorized access!");
		}
		return new serverUtil.File();
	}

}
