package client;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

class ClientFileFunctions 
{
	static void Upload(ClientFile f, PrivateKey pk) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		Gson gson = new Gson();
		String message = gson.toJson(f);
		String signature = ClientCrypto.sign(pk, message);
		SignedRequest sr = new SignedRequest(message, signature);
//		System.out.println(signature);
		
		String signedJSON = gson.toJson(sr);
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://80.111.202.166:8000/uploadfile");
		StringEntity postingString = new StringEntity(signedJSON);
		post.setEntity(postingString);
		post.setHeader("Content-type", "application/json");
		HttpResponse response = httpClient.execute(post);
		if(response.getStatusLine().getStatusCode()!=200)
		{
			System.out.println("Response was not Positive: "+response.getStatusLine().getStatusCode());
		}
		else System.out.println("Response was OK");
	}
	
	static ClientFile GetFile(String owner, String filename, String clientUser) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet("http://80.111.202.166:8000/"+"users/"+owner+"/"+filename);
		HttpResponse response = httpClient.execute(get);
		if(response.getStatusLine().getStatusCode()!=200)
		{
			System.out.println("Response was not Positive: "+response.getStatusLine().getStatusCode());
		}
		else System.out.println("Response was OK");
		String responseString = new BasicResponseHandler().handleResponse(response);
		Gson g = new Gson();
		ClientFile f = g.fromJson(responseString, ClientFile.class);
		return f;
	}

	static List GetFileUsers(String owner, String filename, String clientUser) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet("http://80.111.202.166:8000/"+"users/"+owner+"/"+filename+"/users");
		HttpResponse response = httpClient.execute(get);
		if(response.getStatusLine().getStatusCode()!=200)
		{
			System.out.println("Response was not Positive: "+response.getStatusLine().getStatusCode());
		}
		else System.out.println("Response was OK");
		String responseString = new BasicResponseHandler().handleResponse(response);
		Gson g = new Gson();

		return g.fromJson(responseString, List.class);

	}

}
