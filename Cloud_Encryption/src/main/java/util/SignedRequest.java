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
	public byte[] getMessage() {
		return message;
	}
	public void setMessage(byte[] message) {
		this.message = message;
	}
	public byte[] getSignature() {
		return Signature;
	}
	public void setSignature(byte[] signature) {
		Signature = signature;
	}
}
