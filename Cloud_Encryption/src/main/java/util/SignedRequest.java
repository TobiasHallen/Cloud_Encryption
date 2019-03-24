package util;

public class SignedRequest 
{
	byte[] message;
	byte[] Signature;
	public SignedRequest(byte[] message, byte[] signature) {
		super();
		this.message = message;
		Signature = signature;
	}
}
