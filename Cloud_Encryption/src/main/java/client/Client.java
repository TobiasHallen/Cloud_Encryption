package client;

import java.awt.event.KeyEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

	private static JTextArea connectionInfoArea;
	private static JTextArea userIOArea;

	private static JScrollPane connInfoScrollPane;
	private static JScrollPane userIOScrollPane;
	private static JFrame clientUI;

	public static PublicKey publicKey;
	public static PrivateKey privateKey;
	public static KeyPair kp;
	public static String ClientUser = "Tokmaru";
	
	public static void main(String[] args) throws IOException, GeneralSecurityException, UnsupportedLookAndFeelException
	{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		kp = Crypto.generateKeyPair();
		privateKey=kp.getPrivate();
		publicKey=kp.getPublic();

		UIManager.setLookAndFeel(UIManager.getLookAndFeel());
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					initialize();
					clientUI.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});		

//		Scanner sc = new Scanner(System.in);
//		String input = sc.nextLine();
//		String[] inputArray = input.split("\\s+");
//		if(inputArray[0].equals("register"))
//			Register();
//		else if(inputArray[0].equals("upload"))
//		{
//			Upload(inputArray[1], inputArray[2]);
//		}
//		else if(inputArray[0].equals("download"))
//		{
//			Download(inputArray[1], inputArray[2], inputArray[3]);
//		}
//		else if(inputArray[0].equals("share"))
//		{
//			Share(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
//		}
//		else if(inputArray[0].equals("revoke"))
//		{
//			Revoke(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
//		}
//		else System.out.println("Bad Input");

	}

	public static void Register() throws ClientProtocolException, IOException
	{
		User u = new User(ClientUser, publicKey.getEncoded());
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

	public static void Download(String owner, String filename, String outPath) throws ClientProtocolException, IOException, GeneralSecurityException
	{
		System.out.println(owner+"     "+filename+"        "+ClientUser);
		File f = ClientFileFunctions.GetFile(owner, filename, ClientUser);
		FileKey fk = ClientFileKeyFunctions.GetFileKey(owner, filename, ClientUser);
		byte[] decodedData = "".getBytes();
		try {
			byte[] decodedKey = Crypto.decrypt(fk.key, privateKey);
			decodedData = Crypto.decryptAES(new SecretKeySpec(decodedKey, 0, decodedKey.length,"DES"), f.data);
		} catch (javax.crypto.BadPaddingException e) 
		{
			System.out.println("Error Decrypting: You do not have access to this File!");
			System.exit(0);
		}
		FileUtils.writeByteArrayToFile(new java.io.File("C:\\Users\\artha\\Desktop\\testServer"+"\\noDecryption"+filename), f.data);
		FileUtils.writeByteArrayToFile(new java.io.File(outPath), decodedData);
		System.out.println("Successfully Downloaded File!");
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
		System.out.println(Arrays.equals(decodedData,encodedData));
		System.out.println(Arrays.equals(decodedData,encodedData));
		System.out.println(Arrays.equals(decodedData,encodedData));
		System.out.println(Arrays.equals(decodedData,encodedData));

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
		clientUI = new JFrame();
		clientUI.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e) 
			{
				clientUI.dispose();
				System.exit(0);
			}
		});
		clientUI.setTitle("Client");
		clientUI.setBounds(100, 100, 913, 400);
		clientUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		clientUI.setResizable(false);

		JMenuBar menuBar = new JMenuBar();
		clientUI.setJMenuBar(menuBar);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("Usage");
		mntmAbout.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				System.out.println(usage);
			}
		});
		mnHelp.add(mntmAbout);
		clientUI.getContentPane().setLayout(null);

		JButton sendCommand = new JButton("Send Command");
		sendCommand.setMnemonic(KeyEvent.VK_ENTER);
		sendCommand.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				String input = iOField.getText();
				iOField.setText("");
				String[] inputArray = input.split("\\s+");
				try {
					if(inputArray[0].equals("register"))
						Register();
					else if(inputArray[0].equals("upload"))
					{
						Upload(inputArray[1], inputArray[2]);
					}
					else if(inputArray[0].equals("download"))
					{
						Download(inputArray[1], inputArray[2], inputArray[3]);
					}
					else if(inputArray[0].equals("share"))
					{
						Share(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
					}
					else if(inputArray[0].equals("revoke"))
					{
						Revoke(inputArray[1], java.util.Arrays.copyOfRange(inputArray, 2, inputArray.length));
					}
					else System.out.println("Bad Input");
				}
				catch (IOException io)
				{
					System.out.println("IOException in Input");
				} catch (GeneralSecurityException e1) {
					System.out.println("GeneralSecurityException in Input");
				}

			}
		});
		sendCommand.setBounds(913-12-119, 350-32-12, 119, 32);
		clientUI.getContentPane().add(sendCommand);

		connectionInfoArea = new JTextArea();
       
		TextAreaOutputStream taos = new TextAreaOutputStream(connectionInfoArea);
        PrintStream ps = new PrintStream(taos);
        System.setOut(ps);
        System.setErr(ps);  

		connectionInfoArea.setLineWrap(false);

		connInfoScrollPane = new JScrollPane(connectionInfoArea);

		connInfoScrollPane.setBounds(12, 12, 913-24, 350-18-12-32);

		connInfoScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		clientUI.getContentPane().add(connInfoScrollPane);

		DefaultCaret connCaret = (DefaultCaret) connectionInfoArea.getCaret();
		connCaret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		iOField = new JTextField();

		iOField.setBounds(12, 350-32-12, 913-24-12-119, 32);

		clientUI.getContentPane().add(iOField);

		iOField.setColumns(10);
	}

	public static void addToConnArea(String s) 
	{
		s += "\n";
		try 
		{
			connectionInfoArea.append(s);
		} 
		catch (NullPointerException e) {}	
	}

	public static void addToInfoArea(String s) { 
		s += "\n";
		userIOArea.append(s);	
	}

}
