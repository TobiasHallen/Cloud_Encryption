package client;

import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultCaret;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;

import util.TextAreaOutputStream;

public class Client 
{
	private static String usage = "  Usage:\r\n"+"  register\r\n" + 
			"  upload <filepath> <filename>\r\n" + 
			"  download <user> <filename> <outputpath>\r\n" + 
			"  share <filename> <users>\r\n" + 
			"  revoke <filename> <users>\r\n" ; 

	final JFileChooser fc = new JFileChooser();
	private static JTextArea txtAbout;
	private static JTextField iOField;

	private static JTextArea clientInfoArea;
	private static JTextArea userIOArea;

	private static JScrollPane connInfoScrollPane;
	private static JScrollPane userIOScrollPane;
	private static JFrame clientUI;

	public static PublicKey publicKey;
	public static PrivateKey privateKey;
	public static KeyPair kp;
	public static String ClientUser = "default";

	public static void main(String[] args) throws IOException, GeneralSecurityException, UnsupportedLookAndFeelException
	{
		initialize();	
	}

	public static void Register(String userName) throws ClientProtocolException, IOException, GeneralSecurityException
	{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		kp = Crypto.generateKeyPair(userName);
		privateKey=kp.getPrivate();
		publicKey=kp.getPublic();
		User u = new User(userName, publicKey.getEncoded());
		ClientUserFunctions.Register(u);
		System.out.println("Registered Successfully!");
	}

	public static void Upload(String filepath, String filename) throws IOException, GeneralSecurityException
	{
		java.io.File file = new java.io.File(filepath);
		SecretKey sk = Crypto.generateAESKey();
		byte[] b = Files.readAllBytes(file.toPath());
		byte[] encoded = Crypto.encryptAES(sk, b);
		File f = new File(ClientUser, filename, encoded);
		ClientFileFunctions.Upload(f, privateKey);
		byte[] encodedKey = Crypto.encrypt(sk.getEncoded(), publicKey);
		FileKey fk = new FileKey(ClientUser, ClientUser, filename, encodedKey);
		System.out.println(new String(fk.key));
		ClientFileKeyFunctions.Share(fk, privateKey);
		System.out.println("Successfully Uploaded File!");
	}

	public static void SwitchUsers(String newUser) throws IOException, GeneralSecurityException
	{
		java.io.File pem = new java.io.File("users/"+newUser+"/privateKey.pem");
		if(pem.exists())
		{
			ClientUser=newUser;
			kp = Crypto.generateKeyPair(newUser);
			privateKey=kp.getPrivate();
			publicKey=kp.getPublic();
			System.out.println("Logged in as: "+newUser);
		}
		else
			System.out.println("User does not Exist, please register user before switching to it.");
	}

	public static void Download(String owner, String filename, String outPath) throws ClientProtocolException, IOException, GeneralSecurityException
	{
		System.out.println(owner+"     "+filename+"        "+ClientUser);
		File f = ClientFileFunctions.GetFile(owner, filename, ClientUser);
		FileKey fk = ClientFileKeyFunctions.GetFileKey(owner, filename, ClientUser);

		byte[] decodedData = "".getBytes();
		try {
			byte[] decodedKey = Crypto.decrypt(fk.key, privateKey);
			decodedData = Crypto.decryptAES(new SecretKeySpec(decodedKey, 0, decodedKey.length,"DES"), f.data);
			FileUtils.writeByteArrayToFile(new java.io.File("C:\\Users\\artha\\Documents\\GitHub\\Cloud_Encryption\\Cloud_Encryption\\upANDdownloadFolder"+"\\noDecryption"+filename), f.data);
			FileUtils.writeByteArrayToFile(new java.io.File(outPath), decodedData);
			System.out.println("Successfully Downloaded File!");
		} catch (javax.crypto.BadPaddingException e) 
		{
			System.out.println("Error Decrypting: You do not have access to this File!");
		}

	}

	public static void Share(String filename, String[] Users) throws ClientProtocolException, IOException, GeneralSecurityException
	{
		FileKey fk = ClientFileKeyFunctions.GetFileKey(ClientUser, filename, ClientUser);
		byte[] decodedKey = Crypto.decrypt(fk.key, privateKey);
		for(String u : Users)
		{
			if(!u.equals(ClientUser))
			{
				User user = ClientUserFunctions.GetUser(u);
				fk.id="";
				fk.user=u;
				byte[] encodedKey = Crypto.encrypt(decodedKey, KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(user.PubKey)));
				fk.key=encodedKey;
				ClientFileKeyFunctions.Share(fk, privateKey);
			}
		}
		System.out.println("File Shared Successfully!");
	}

	public static void Revoke(String filename, String[] users) throws ClientProtocolException, IOException, GeneralSecurityException
	{
		System.out.println(Arrays.toString(users));
		File f = ClientFileFunctions.GetFile(ClientUser, filename, ClientUser);
		FileKey fk = ClientFileKeyFunctions.GetFileKey(ClientUser, filename, ClientUser);

		byte[] decodedKey = Crypto.decrypt(fk.key, privateKey);
		byte[] decodedData = Crypto.decryptAES(new SecretKeySpec(decodedKey, 0, decodedKey.length,"DES"), f.data);
		SecretKey sk = Crypto.generateAESKey();
		byte[] encodedData = Crypto.encryptAES(sk, decodedData);
		f.data=encodedData;

		ClientFileFunctions.Upload(f, privateKey);

		byte[] encodedKey=Crypto.encrypt(sk.getEncoded(), publicKey);
		fk.key=encodedKey;
		Gson gson = new Gson();

		ClientFileKeyFunctions.Share(fk, privateKey);

		for(String u:users)
		{
			FileKey fileKey = new FileKey(u, ClientUser, filename, null);
			ClientFileKeyFunctions.Revoke(fileKey, privateKey);
		}
		FileUsers fUsers = ClientFileFunctions.GetFileUsers(ClientUser, filename, ClientUser);
		if(fUsers.users!=null)Share(filename, fUsers.users);
		System.out.println("Successfully Revoked File!");
	}

	private static void initialize() 
	{
		final JFrame frame = new JFrame();
		frame.add( new JLabel("Client" ), BorderLayout.NORTH );
		JTextArea ta = new JTextArea(800, 400);
		TextAreaOutputStream taos = new TextAreaOutputStream(ta);
		PrintStream ps = new PrintStream(taos);
		System.setOut(ps);
		System.setErr(ps);  

		final JTextField input = new JTextField("Input", 50);
		input.requestFocus();
		ActionListener a = new ActionListener()
		{

			public void actionPerformed(ActionEvent e) 
			{
				String[] inputArray = input.getText().split("\\s+");
				try {
					if(inputArray[0].equals("register"))
					{
						input.setText("");
						ClientUser = inputArray[1];
						Register(ClientUser);
					}
					else if(inputArray[0].equals("login"))
					{
						input.setText("");
						SwitchUsers(inputArray[1]);
					}
					else if(inputArray[0].equals("upload"))
					{
						input.setText("");
						Upload(inputArray[1], inputArray[2]);
					}
					else if(inputArray[0].equals("download"))
					{
						input.setText("");
						Download(inputArray[1], inputArray[2], inputArray[3]);
					}
					else if(inputArray[0].equals("share"))
					{
						input.setText("");
						Share(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
					}
					else if(inputArray[0].equals("revoke"))
					{
						input.setText("");
						Revoke(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
					}
					else 
					{
						input.setText("");
						System.out.println("Bad Input");
					}
				}
				catch (IOException io)
				{
					System.out.println("IOException in Input");
				} catch (GeneralSecurityException e1) {
					System.out.println("GeneralSecurityException in Input");
				}

			}};
			input.addActionListener(a);	
			ta.setEditable(false);

			frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
			frame.getContentPane().add(input);
			frame.getContentPane().add(new JScrollPane(ta));  

			frame.pack();
			frame.setVisible(true);
			frame.setSize(800, 600);

			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent windowEvent) {
					if (JOptionPane.showConfirmDialog(frame, 
							"Are you sure you want to close the Client?", "Close Client?", 
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
						System.exit(0);
					}
				}
			});
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			System.out.println("Client Ready for input!");

	}

}
