package client;

import java.security.PublicKey;

public class User 
{
	public String id;
	public String username;
	public byte[] PubKey;
	
	public User(String username, byte[] pubKey) {
		super();
		this.username = username;
		PubKey = pubKey;
	}
}
