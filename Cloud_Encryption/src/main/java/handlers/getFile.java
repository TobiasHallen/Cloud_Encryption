package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class getFile 
{
	public getFile(HttpRequest req)
	{
		System.out.println("getFile Works!");
		System.out.println(req.uri());

	}
}
