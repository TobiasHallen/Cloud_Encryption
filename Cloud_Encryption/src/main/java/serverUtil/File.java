package serverUtil;

public class File 
{
	public String id;
	public String owner;
	public String name;
	public byte[] data;	
	public File(String iD, String owner, String name, byte[] data) {
		super();
		id = iD;
		this.owner = owner;
		this.name = name;
		this.data = data;
	}	
	public File()
	{
		id = "";
		owner = "";
		name = "";
		data = "".getBytes();
	}


}
