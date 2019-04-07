package client;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import javax.crypto.*;
import org.apache.commons.codec.binary.Base64;

class ClientCrypto 
{
	static KeyPair generateKeyPair(String clientName) throws IOException, GeneralSecurityException
	{
		
		new java.io.File("users/"+clientName).mkdirs();
		java.io.File pem = new java.io.File("users/"+clientName+"/privateKey.pem");
		if(!pem.exists())
		{

			KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
			gen.initialize(2048);
			KeyPair kp = gen.generateKeyPair();
			PrintWriter pw = new PrintWriter(pem);
			JcaPEMWriter writer = new JcaPEMWriter(pw);
			writer.writeObject(kp.getPrivate());
			writer.writeObject(kp.getPublic());
			writer.close();

			return kp;
		}
		else
		{
			java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			PEMParser pemParser = new PEMParser(new FileReader(pem));
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			Object object = pemParser.readObject();
			KeyPair kp = converter.getKeyPair((PEMKeyPair) object);
			pemParser.close();
			return kp;
		}
	}

	static String sign(PrivateKey privateKey, String message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
		Signature sign = Signature.getInstance("SHA1withRSA");
		sign.initSign(privateKey);
		sign.update(message.getBytes("UTF-8"));
		return new String(Base64.encodeBase64(sign.sign()), "UTF-8");
	}


	

	static byte[] encrypt(byte[] rawText, PublicKey publicKey) throws IOException, GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.ENCRYPT_MODE,  (RSAPublicKey)publicKey);
		return cipher.doFinal(rawText);
	}

	static byte[] decrypt(byte[] cipherText, PrivateKey privateKey) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
		cipher.init(Cipher.DECRYPT_MODE, (RSAPrivateKey)privateKey);
		return cipher.doFinal(cipherText);
	}

	static byte[] encryptAES(SecretKey key, byte[] value) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");   	
		c.init(Cipher.ENCRYPT_MODE, key);
		byte[] cipherbytes = c.doFinal(value);
		return cipherbytes;
	}

	static byte[] decryptAES(SecretKey key, byte[] encrypted) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5PADDING");
		c.init(Cipher.DECRYPT_MODE, key);
		byte[] decrypted = c.doFinal(encrypted);
		return decrypted;       	
	}

	static SecretKey generateAESKey() throws NoSuchAlgorithmException
	{
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); // for example
		SecretKey secretKey = keyGen.generateKey();
		return secretKey;
	}
}


