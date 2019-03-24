package util;
import java.security.*;
import java.util.Base64;


public class Crypto 
{
	public boolean verify(PublicKey pk, byte[] message, String signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException
	{
		Signature publicSignature = Signature.getInstance("SHA256withRSA");
		publicSignature.initVerify(pk);
		publicSignature.update(message);
		
		byte[] signatureBytes = Base64.getDecoder().decode(signature);
		
		return publicSignature.verify(signatureBytes);
	}
}
