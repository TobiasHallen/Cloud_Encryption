package server;

import com.google.gson.Gson;
import com.rethinkdb.*;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

public class User 
{
	private static String DBHost = "127.0.0.1";
	private static final RethinkDB r = RethinkDB.r;
	private static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	private static Table userTable = r.db("Cloud_Encryption").table("users");
	
	public static int insert(serverUtil.User u) 
	{
		if(userTable.g("username").contains(u.username).run(conn))
		{
			System.out.println("Duplicate User Entry");		
			return 1;
		}
		else
		{
			serverUtil.DBUser user = new serverUtil.DBUser(u.id,u.username,u.PubKey.toString());
			userTable.insert(r.hashMap("username", user.username).with("PubKey", r.binary(u.PubKey))).run(conn);
			return 0;
		}
	}
	
	@SuppressWarnings("rawtypes")
	static serverUtil.User GetUser(String username)
	{
		if(userTable.g("username").contains(username).run(conn))
		{
			Cursor dbRes = userTable.getAll(username).optArg("index", "username").run(conn);
			for(Object doc : dbRes)
			{
				Gson gson = new Gson();
				String s = gson.toJson(doc);
				serverUtil.User u = gson.fromJson(s, serverUtil.User.class);
				return u;
			}
		}
		else System.out.println("User does not Exist");
		return null;
	}
}
