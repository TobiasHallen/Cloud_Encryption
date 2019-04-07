package client;

class ClientFileKey 
{
	public String id;
	public String user;
	public String owner;
	public String name;
	public byte[] key;
	
	ClientFileKey(String user, String owner, String name, byte[] key) {
		super();
		this.user = user;
		this.owner = owner;
		this.name = name;
		this.key = key;
	}
	ClientFileKey()
	{
		id = "";
		user = "";
		owner = "";
		name = "";
		key = "".getBytes();
	}

}
