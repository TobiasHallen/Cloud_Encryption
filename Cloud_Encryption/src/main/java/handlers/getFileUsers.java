package handlers;

import io.netty.handler.codec.http.HttpRequest;

public class getFileUsers 
{
	public getFileUsers(HttpRequest req)
	{
		System.out.println("getFileUsers Works!");
		System.out.println(req.uri());

	}
}
