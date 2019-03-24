package client;

public class File 
{
	public String id;
	public String owner;
	public String name;
	public byte[] data;
	public File(String owner, String name, byte[] data) {
		super();
		this.owner = owner;
		this.name = name;
		this.data = data;
	}

}
