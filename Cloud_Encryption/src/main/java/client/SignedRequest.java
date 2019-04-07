package client;

class SignedRequest 
{
	public String message;
	public String signature;
	SignedRequest(String message, String signature) {
		super();
		this.message = message;
		this.signature = signature;
	}
}
