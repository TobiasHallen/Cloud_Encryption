package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import express.DynExpress;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;

class ServerBindings {

	@DynExpress(context= "/register", method = RequestMethod.POST) // Default is context="/" and method=RequestMethod.GET
	public void register(Request req, Response res) throws IOException {    
		System.out.println("Received Register Request!");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(
				new InputStreamReader(req.getBody(), "UTF-8"));
		Gson gson = new Gson();
		String json = gson.toJson(jsonObject);
		serverUtil.User user = gson.fromJson(json,serverUtil.User.class);
		if(User.insert(user)!=0)res.send("Duplicate User Entry!");
		else res.send("User Registered!");

	}

	@DynExpress(context= "/uploadfile", method = RequestMethod.POST) // Only context is defined, method=RequestMethod.GET is used as method
	public void uploadFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("Received Upload Request!");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(
				new InputStreamReader(req.getBody(), "UTF-8"));
		Gson gson = new Gson();
		String message = jsonObject.get("message").getAsString();
		String sig = jsonObject.get("signature").getAsString();

		serverUtil.File file = gson.fromJson(message, serverUtil.File.class);
		serverUtil.User u = User.GetUser(file.owner);
		if(!serverUtil.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
		{
			System.out.println("Could not Verify Signature!");
			res.setStatus(Status.valueOf(403));
			res.send("Could not Verify Signature!");
		}

		if(File.insert(file)!=0) res.send("Updated Existing File!");
		else res.send("File Uploaded!");
	}

	@DynExpress(context = "/sharefile", method = RequestMethod.POST) // Both defined
	public void shareFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("Received Share Request!");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(
				new InputStreamReader(req.getBody(), "UTF-8"));
		Gson gson = new Gson();
		String message = jsonObject.get("message").getAsString();
		String sig = jsonObject.get("signature").getAsString();

		serverUtil.FileKey fk = gson.fromJson(message,serverUtil.FileKey.class);
		serverUtil.User u = User.GetUser(fk.owner);
		if(!serverUtil.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
		{
			System.out.println("Could not Verify Signature!");
			res.setStatus(Status.valueOf(403));
			res.send("Could not Verify Signature!");
		}

		if(FileKey.insert(fk)!=0) res.send("Updated Existing FileKey!");
		else res.send("File Shared!");
	}

	@DynExpress(context= "/revokefile", method = RequestMethod.POST) // Only the method is defined, "/" is used as context
	public void revokeFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException, InvalidKeyException, SignatureException, NoSuchAlgorithmException, InvalidKeySpecException {
		System.out.println("Received Revoke Request!");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(
				new InputStreamReader(req.getBody(), "UTF-8"));
		Gson gson = new Gson();
		String message = jsonObject.get("message").getAsString();
		String sig = jsonObject.get("signature").getAsString();

		serverUtil.FileKey f1 = gson.fromJson(message, serverUtil.FileKey.class);
		serverUtil.FileKey fk = FileKey.getFileKey(f1.owner, f1.name, f1.user);
		if(!fk.id.equals(""))
		{
			serverUtil.User u = User.GetUser(fk.owner);
			if(!serverUtil.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
			{
				System.out.println("Could not Verify Signature!");
				res.setStatus(Status.valueOf(403));
				res.send("Could not Verify Signature!");
			}
			else
			{
				int i = FileKey.revoke(fk);
				if(i==2)
				{
					res.send("Cannot revoke own file access!");
				}
				else if(i==1) 
				{
					res.send("FileKey entry does not exist, cannot be revoked!");
				}
				else res.send("File access Revoked!");
			}
		}
		else
		{
			res.send("FileKey entry does not exist, failed to revoke file access!");
		}
	}

	@DynExpress(context = "/users/:username", method = RequestMethod.GET) // Both defined
	public void getUser(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
		serverUtil.User u = User.GetUser(req.getParam("username"));
		Gson gson = new Gson();
		String json = gson.toJson(u);    	
		res.send(json);
	}

	@DynExpress(context = "/users/:username/:filename", method = RequestMethod.GET) // Both defined
	public void getFile(Request req, Response res) {
		serverUtil.File f = File.getFile(req.getParam("username"), req.getParam("filename"));
		if(f.name.equals(""))
		{
			res.setStatus(Status.valueOf(403));
			res.send("Failed Getting File, does not Exist.");
		}
		else 
		{
			Gson gson = new Gson();
			String json = gson.toJson(f);    	
			res.send(json);
		}
	}

	@SuppressWarnings("rawtypes")
	@DynExpress(context = "/users/:username/:filename/users", method = RequestMethod.GET) // Both defined
	public void getFileUsers(Request req, Response res) {
		List l = FileKey.getFileUsers(req.getParam("username"), req.getParam("filename"));
		Gson gson = new Gson();
		String json = gson.toJson(l); 
		res.send(json);
	}

	@DynExpress(context = "/users/:username/:filename/key/:user", method = RequestMethod.GET) // Both defined
	public void getFileKey(Request req, Response res) {
		serverUtil.FileKey fk = FileKey.getFileKey(req.getParam("username"), req.getParam("filename"), req.getParam("user"));
		if (fk.name.equals(""))
		{
			res.setStatus(Status.valueOf(403));
			res.send("Failed Getting FileKey, unauthorized access.");
		}
		else 
		{
			Gson gson = new Gson();
			String json = gson.toJson(fk);    	
			res.send(json);
		}
	}

}
