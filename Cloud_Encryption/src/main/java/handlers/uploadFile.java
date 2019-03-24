package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class uploadFile 
{
	public uploadFile(HttpRequest req)
	{
		System.out.println("uploadFile Works!");
		System.out.println(req.uri());

	}
}
