package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class revokeFile 
{
	public revokeFile(HttpRequest req)
	{
		System.out.println("revokeFile Works!");
		System.out.println(req.uri());

	}
}
