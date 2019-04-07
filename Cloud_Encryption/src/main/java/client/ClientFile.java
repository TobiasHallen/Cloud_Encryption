package client;

class ClientFile 
{
	public String id;
	public String owner;
	public String name;
	public byte[] data;
	ClientFile(String owner, String name, byte[] data) {
		super();
		this.owner = owner;
		this.name = name;
		this.data = data;
	}

}
