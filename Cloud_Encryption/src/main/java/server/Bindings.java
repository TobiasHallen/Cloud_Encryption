package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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

import express.DynExpress;
import express.http.RequestMethod;
import express.http.request.Request;
import express.http.response.Response;
public class Bindings {
	static String DBHost = "127.0.0.1";
	public static final RethinkDB r = RethinkDB.r;
	static Connection conn = r.connection().hostname(DBHost).port(28015).connect();
	static Table filetable = r.db("Cloud_Encryption").table("files");

    @DynExpress(context= "/register", method = RequestMethod.POST) // Default is context="/" and method=RequestMethod.GET
    public void register(Request req, Response res) throws IOException {     
    	JsonParser jsonParser = new JsonParser();
    	JsonObject jsonObject = (JsonObject)jsonParser.parse(
    		      new InputStreamReader(req.getBody(), "UTF-8"));
    	Gson gson = new Gson();
    	String json = gson.toJson(jsonObject);
    	System.out.println(json);
    	util.User user = gson.fromJson(json,util.User.class);
    	User.insert(user);
    	System.out.println(user.id);
    	System.out.println(user.username);
    	System.out.println(user.PubKey);

        res.send("User Registered!");

    }

    @DynExpress(context= "/uploadfile", method = RequestMethod.POST) // Only context is defined, method=RequestMethod.GET is used as method
    public void uploadFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
    	System.out.println("Received Upload Request!");
    	JsonParser jsonParser = new JsonParser();
    	JsonObject jsonObject = (JsonObject)jsonParser.parse(
    		      new InputStreamReader(req.getBody(), "UTF-8"));
    	Gson gson = new Gson();
    	String json = jsonObject.get("message").getAsString();
    	System.out.println(json);
    	util.File file = gson.fromJson(json, util.File.class);
//    	System.out.println(new String(file.data));
    	File.insert(file);
        res.send("File Uploaded!");
    }

    @DynExpress(context = "/sharefile", method = RequestMethod.POST) // Both defined
    public void shareFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
    	System.out.println("Received Share Request!");
    	JsonParser jsonParser = new JsonParser();
    	JsonObject jsonObject = (JsonObject)jsonParser.parse(
    		      new InputStreamReader(req.getBody(), "UTF-8"));
    	Gson gson = new Gson();
    	String json = jsonObject.get("message").getAsString();
    	System.out.println(json);
    	util.FileKey fk = gson.fromJson(json,util.FileKey.class);
    	FileKey.insert(fk);
        res.send("File Shared!");
    }

    @DynExpress(context= "/revokefile", method = RequestMethod.POST) // Only the method is defined, "/" is used as context
    public void revokeFile(Request req, Response res) throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException {
    	JsonParser jsonParser = new JsonParser();
    	JsonObject jsonObject = (JsonObject)jsonParser.parse(
    		      new InputStreamReader(req.getBody(), "UTF-8"));
    	Gson gson = new Gson();
    	String json = jsonObject.get("message").getAsString();
    	System.out.println(json);
    	util.FileKey fk = gson.fromJson(json,util.FileKey.class);
    	FileKey.revoke(fk);
    	System.out.println(fk.id);
    	System.out.println(fk.name);
    	System.out.println(fk.owner);
    	System.out.println(fk.user);

        res.send("File Shared!");
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
    	Gson gson = new Gson();
    	String json = gson.toJson(f);    	
        res.send(json);
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
    	Gson gson = new Gson();
    	String json = gson.toJson(fk);    	
        res.send(json);
    }

}
