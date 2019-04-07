package serverUtil;

public class DBUser 
{
	public DBUser(String iD, String username, String pubKey) {
		super();
		ID = iD;
		this.username = username;
		PubKey = pubKey;
	}
	public String ID = "gorethink:\"id,omitempty\"";
	public String username = "gorethink:\"username\"";
	public String PubKey = "gorethink:\"pubkey\"";
}
