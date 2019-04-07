package client;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.net.ssl.HttpsURLConnection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;

public class ClientUserFunctions 
{
	public static int Register(User u) throws ClientProtocolException, IOException
	{
		
		Gson gson = new Gson();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://80.111.202.166:8000/register");		
		StringEntity postingString = new StringEntity(gson.toJson(u));
		post.setEntity(postingString);
		post.setHeader("Content-type", "application/json");
		HttpResponse response = httpClient.execute(post);
		if(response.getStatusLine().getStatusCode()!=200)
		{
			System.out.println("Response was not Positive: "+response.getStatusLine().getStatusCode());
		}
		else System.out.println("Response was OK");
		return 0;
	}
	
	public static User GetUser(String username) throws ClientProtocolException, IOException
	{
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet("http://80.111.202.166:8000"+"/users/"+username);
		get.setHeader("Content-type", "application/json");
		HttpResponse response = httpClient.execute(get);
		String json = EntityUtils.toString(response.getEntity());
		System.out.println(json);
		Gson g = new Gson();
		return g.fromJson(json, User.class);

	}

}
