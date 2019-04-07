package server;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import express.Express;
import util.TextAreaOutputStream;

public class Server 
{
    public static void main(String[] args) throws IOException 
    {
        final JFrame frame = new JFrame();
        frame.add( new JLabel("Server" ), BorderLayout.NORTH );
        JTextArea ta = new JTextArea();
        TextAreaOutputStream taos = new TextAreaOutputStream(ta);
        PrintStream ps = new PrintStream(taos);
        System.setOut(ps);
        System.setErr(ps);  
        ta.setEditable(false);
        frame.add(new JScrollPane(ta));        
        frame.pack();
        frame.setVisible(true);
        frame.setSize(800, 600);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(frame, 
                    "Are you sure you want to close the Server?", "Close Server?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Express app = new Express();
        try {
        	System.out.println("Starting up Server...");
			app.bind(new Bindings()); // See class below
			app.listen(8000);
		} catch (java.lang.ExceptionInInitializerError e) {
			System.out.println("Server Refused Connection. Shutting Down.");
			System.exit(0);
		}
    }

}
