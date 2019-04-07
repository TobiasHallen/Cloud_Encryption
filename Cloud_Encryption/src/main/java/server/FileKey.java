package server;
import com.google.gson.Gson;
import com.rethinkdb.*;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import java.util.HashMap;
import java.util.List;

public class FileKey 
{
	private static String DBHost = "127.0.0.1";
	private static final RethinkDB r = RethinkDB.r;
	private static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	private static Table filekeytable = r.db("Cloud_Encryption").table("filekeys");


	static int insert(serverUtil.FileKey fk)
	{
		if(filekeytable.g("name").contains(fk.name).run(conn))
		{
			if(filekeytable.g("owner").contains(fk.owner).run(conn))
			{
				if(filekeytable.g("user").contains(fk.user).run(conn))
				{
					System.out.println("Duplicate FileKey Entry");
					serverUtil.FileKey f = getFileKey(fk.owner, fk.name, fk.user);
					fk.id = f.id;
					filekeytable.insert(r.hashMap("name", fk.name).with("id", fk.id).with("user", fk.user).with("owner", fk.owner).with("key", r.binary(fk.key))).optArg("conflict", "replace").run(conn);
					return 1;
				}
			}
		}
		filekeytable.insert(r.hashMap("name", fk.name).with("user", fk.user).with("owner", fk.owner).with("key", r.binary(fk.key))).run(conn);
		return 0;
	}

	

	static int revoke(serverUtil.FileKey fk)
	{
		if(fk.user == fk.owner)
		{
			System.out.println("Cannot revoke own file access");
			return 2;
		}
		if(filekeytable.g("name").contains(fk.name).run(conn))
		{
			if(filekeytable.g("owner").contains(fk.owner).run(conn))
			{
				if(filekeytable.g("user").contains(fk.user).run(conn))
				{

					filekeytable.get(getFileKey(fk.owner, fk.name, fk.user).id).delete().run(conn);
					return 0;
				}
			}
		}
		return 1;
	}

	@SuppressWarnings("rawtypes")
	static serverUtil.FileKey getFileKey(String owner, String filename, String user)
	{
		
		Cursor dbRes;
		try {
			dbRes = filekeytable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", owner).with("user", user)).run(conn);
			HashMap m = (HashMap) dbRes.next();
			serverUtil.FileKey fk = new serverUtil.FileKey((String)m.get("id"), (String)m.get("user"), (String)m.get("owner"), (String)m.get("name"), (byte[])m.get("key"));
			return fk;
		} catch (java.util.NoSuchElementException e) {
			System.out.println("Invalid file access");
			System.out.println("User "+user+" does not have access to file "+filename+" owned by "+owner);
		}
		return new serverUtil.FileKey();
	}

@SuppressWarnings("rawtypes")
static List getFileUsers(String owner, String filename)
{
	
	Cursor dbRes = filekeytable.getAll(filename).optArg("index", "name").filter(r.hashMap("owner", owner)).pluck("user").run(conn);
//	System.out.println(dbRes.toList());
//	Gson gson = new Gson();
//	System.out.println(	gson.toJson(dbRes.toList()));
	return dbRes.toList();
}

}
