package initDB;

import org.apache.log4j.BasicConfigurator;

import com.rethinkdb.*;
import com.rethinkdb.net.Connection;


class InitDB 
{
	public static final RethinkDB r = RethinkDB.r;
	
	public static void main(String[] args) 
	{
		BasicConfigurator.configure();

		String DBHost = "127.0.0.1";
		Connection conn = r.connection().hostname(DBHost).port(28015).connect();

		r.dbCreate("Cloud_Encryption").run(conn);
		r.db("Cloud_Encryption").tableCreate("users").run(conn);
		r.db("Cloud_Encryption").table("users").indexCreate("username").run(conn);
		
		r.db("Cloud_Encryption").tableCreate("files").run(conn);
		r.db("Cloud_Encryption").table("files").indexCreate("name").run(conn);
		r.db("Cloud_Encryption").table("files").indexCreate("owner").run(conn);

		r.db("Cloud_Encryption").tableCreate("filekeys").run(conn);
		r.db("Cloud_Encryption").table("filekeys").indexCreate("name").run(conn);
		r.db("Cloud_Encryption").table("filekeys").indexCreate("owner").run(conn);
		r.db("Cloud_Encryption").table("filekeys").indexCreate("user").run(conn);		System.out.println("hello");

		System.exit(0);
	}
}

