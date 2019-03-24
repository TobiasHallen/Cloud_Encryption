package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class getUser 
{
	public getUser(HttpRequest req)
	{
		System.out.println("getUser Works!");
		System.out.println(req.uri());

	}
}
