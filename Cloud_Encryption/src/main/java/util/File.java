package util;

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
	
	public void printFileKey(File f)
	{
		System.out.println(f.id);
		System.out.println(f.owner);
		System.out.println(f.name);
		System.out.println(f.data);
		
	}

}
