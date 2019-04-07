package server;

import com.google.gson.Gson;
import com.rethinkdb.*;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

public class User 
{
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table userTable = r.db("Cloud_Encryption").table("users");
	
	public static int insert(util.User u) 
	{
		if(userTable.g("username").contains(u.username).run(conn))
		{
			System.out.println("Duplicate User Entry");		
			return 1;
		}
		else
		{
			util.DBUser user = new util.DBUser(u.id,u.username,u.PubKey.toString());
			userTable.insert(r.hashMap("username", user.username).with("PubKey", r.binary(u.PubKey))).run(conn);
			return 0;
		}
	}
	
	public static util.User GetUser(String username)
	{
		if(userTable.g("username").contains(username).run(conn))
		{
			Cursor dbRes = userTable.getAll(username).optArg("index", "username").run(conn);
			for(Object doc : dbRes)
			{
				Gson gson = new Gson();
				String s = gson.toJson(doc);
				util.User u = gson.fromJson(s, util.User.class);
				return u;
			}
		}
		else System.out.println("User does not Exist");
		return null;
	}
}
