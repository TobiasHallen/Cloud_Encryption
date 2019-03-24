package handlers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class register 
{
    private final StringBuilder buf = new StringBuilder();

	public register(HttpRequest req)
	{
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        if (!params.isEmpty()) {
            for (Entry<String, List<String>> p: params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    buf.append("PARAM: ").append(key).append(" = ").append(val).append("\r\n");
                }
            }
            buf.append("\r\n");
        }
        System.out.println(buf);
		util.User user;
		if(req.equals(null)) {}
//		System.out.println("Register Works!");
		System.out.println(req.uri());
	}
}
