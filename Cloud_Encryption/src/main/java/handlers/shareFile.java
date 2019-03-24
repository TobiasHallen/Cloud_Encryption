package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class shareFile 
{
	public shareFile(HttpRequest req)
	{
		System.out.println("shareFile Works!");
		System.out.println(req.uri());

	}
}
