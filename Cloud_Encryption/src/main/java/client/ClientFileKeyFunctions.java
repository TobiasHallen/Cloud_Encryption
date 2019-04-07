package client;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.*;

class ClientFileKeyFunctions 
{
	static void Share(ClientFileKey fk, PrivateKey pk) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		Gson gson = new Gson();
		String message = gson.toJson(fk);
		String signature = ClientCrypto.sign(pk, message);
		SignedRequest sr = new SignedRequest(message, signature);
		String signedJSON = gson.toJson(sr);
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://80.111.202.166:8000/sharefile");
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

	static void Revoke(ClientFileKey fk, PrivateKey pk) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		Gson gson = new Gson();
		String message = gson.toJson(fk);
		String signature = ClientCrypto.sign(pk, message);
		SignedRequest sr = new SignedRequest(message, signature);
		String signedJSON = gson.toJson(sr);
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost("http://80.111.202.166:8000/revokefile");
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

	static ClientFileKey GetFileKey(String owner, String filename, String clientUser) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, ClientProtocolException, IOException
	{
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet("http://80.111.202.166:8000/"+"users/"+owner+"/"+filename+"/key/"+clientUser);
		HttpResponse response = httpClient.execute(get);
		if(response.getStatusLine().getStatusCode()!=200)
		{
			System.out.println("Response was not Positive: "+response.getStatusLine().getStatusCode());
			return new ClientFileKey();
		}
		else 
		{
			System.out.println("Response was OK");
			String responseString = new BasicResponseHandler().handleResponse(response);
			Gson g = new Gson();
			ClientFileKey fk= g.fromJson(responseString, ClientFileKey.class);
			return fk;
		}

	}
}
