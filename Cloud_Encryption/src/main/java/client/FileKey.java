package client;

public class FileKey 
{
	public String id;
	public String user;
	public String owner;
	public String name;
	public byte[] key;
	
	public FileKey(String user, String owner, String name, byte[] key) {
		super();
		this.user = user;
		this.owner = owner;
		this.name = name;
		this.key = key;
	}
}
