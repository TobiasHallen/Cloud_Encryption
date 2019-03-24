package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class getFileKey 
{
	public getFileKey(HttpRequest req)
	{
		System.out.println("getFileKey Works!");
		System.out.println(req.uri());

	}
}
