package util;

public class FileKey 
{

	public String id;
	public String user;
	public String owner;
	public String name;
	public byte[] key;
	
	public FileKey(String iD, String user, String owner, String name, byte[] key) {
		super();
		id = iD;
		this.user = user;
		this.owner = owner;
		this.name = name;
		this.key = key;
	}
	
	public void printFileKey(FileKey f)
	{
		System.out.println(f.id);
		System.out.println(f.user);
		System.out.println(f.owner);
		System.out.println(f.name);
		System.out.println(f.key);
		
	}

}
