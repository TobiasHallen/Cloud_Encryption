package client;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;

import org.apache.http.client.ClientProtocolException;

public class test 
{
	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, ClientProtocolException, IOException
	{
//		KeyPairGenerator kbg = KeyPairGenerator.getInstance("DSA", "SUN");
//		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
//		kbg.initialize(1024, random);
//		KeyPair pair = kbg.generateKeyPair();
//		User u = new User("1","Tokmaru", pair.getPublic());
//		ClientUserFunctions.Register(u);
		
		ClientUserFunctions.GetUser("Tokmaru");
	}
}
