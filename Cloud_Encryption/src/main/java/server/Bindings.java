package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;

import client.Crypto;
import express.DynExpress;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;
import express.utils.Status;
import util.SignedRequest;

public class Bindings {
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table filetable = r.db("Cloud_Encryption").table("files");

	@DynExpress(context= "/register", method = RequestMethod.POST) // Default is context="/" and method=RequestMethod.GET
	public void register(Request req, Response res) throws IOException {    
		System.out.println("Received Register Request!");
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject)jsonParser.parse(
				new InputStreamReader(req.getBody(), "UTF-8"));
		Gson gson = new Gson();
		String json = gson.toJson(jsonObject);
		//    	System.out.println(json);
		util.User user = gson.fromJson(json,util.User.class);
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
		System.out.println(sig);

		util.File file = gson.fromJson(message, util.File.class);
		util.User u = User.GetUser(file.owner);
		if(!util.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
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

		util.FileKey fk = gson.fromJson(message,util.FileKey.class);
		util.User u = User.GetUser(fk.owner);
		if(!util.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
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

		util.FileKey f1 = gson.fromJson(message, util.FileKey.class);
		util.FileKey fk = FileKey.getFileKey(f1.owner, f1.name, f1.user);
		if(!fk.id.equals(""))
		{
			util.User u = User.GetUser(fk.owner);
			if(!util.Crypto.verify(KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(u.PubKey)), message.getBytes(), sig))
			{
				System.out.println("Could not Verify Signature!");
				res.setStatus(Status.valueOf(403));
				res.send("Could not Verify Signature!");
			}
			else if(FileKey.revoke(fk)==2)
			{
				res.send("Cannot revoke own file access!");
			}
			else if(FileKey.revoke(fk)==1) 
			{
				res.send("FileKey entry does not exist, cannot be revoked!");
			}
			else res.send("File access Revoked!");
		}
		else
		{
			res.send("FileKey entry does not exist, failed to revoke file access!");
		}
	}

	@DynExpress(context = "/users/:username", method = RequestMethod.GET) // Both defined
	public void getUser(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
		util.User u = User.GetUser(req.getParam("username"));
		Gson gson = new Gson();
		String json = gson.toJson(u);    	
		res.send(json);
	}

	@DynExpress(context = "/users/:username/:filename", method = RequestMethod.GET) // Both defined
	public void getFile(Request req, Response res) {
		util.File f = File.getFile(req.getParam("username"), req.getParam("filename"));
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

	@DynExpress(context = "/users/:username/:filename/users", method = RequestMethod.GET) // Both defined
	public void getFileUsers(Request req, Response res) {
		HashMap m = FileKey.getFileUsers(req.getParam("username"), req.getParam("filename"));
		Gson gson = new Gson();
		String json = gson.toJson(m);    	
		res.send(json);
	}

	@DynExpress(context = "/users/:username/:filename/key/:user", method = RequestMethod.GET) // Both defined
	public void getFileKey(Request req, Response res) {
		util.FileKey fk = FileKey.getFileKey(req.getParam("username"), req.getParam("filename"), req.getParam("user"));
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
