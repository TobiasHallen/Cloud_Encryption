package client;

public class SignedRequest 
{
	public String message;
	public String signature;
	public SignedRequest(String message, String signature) {
		super();
		this.message = message;
		this.signature = signature;
	}
}
