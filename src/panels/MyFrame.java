package panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.security.Key;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class MyFrame extends JFrame implements KeyListener
{
	private MyPanel panel;
	private JPanel options;
	private JMenuBar menuBar;
	private JMenu file;
	private JMenuItem open;
	private String defaultLocation = "E:/PRACA INZYNIERSKA/Obrazy/Iris Database/SGGSIE&T Iris Image Database all in 1";
	 

	LayoutManager manager;

	ActionsPanel actionsPanel;
	
	public MyFrame (String name)
	{
		super (name);
		setVisible(true);
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
		setPreferredSize(new Dimension (500,500));
		setSize(getPreferredSize());
		setLocation(430,200);
		addKeyListener(this);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		file = new JMenu ("File");	
		open = new JMenuItem ("Open");
		open.addActionListener(new OpenListener());
		file.add(open);
		menuBar.add(file);
		setJMenuBar(menuBar);
		
		manager = new BorderLayout ();
		setLayout(manager);
		
		panel = new MyPanel ();
		add (panel, BorderLayout.CENTER);
		
		actionsPanel = new ActionsPanel (panel);
		options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		actionsPanel.initButtons(options);
		add (options, BorderLayout.EAST);

		setFocusable(true);
		
		chooseFile ();
		//readAll();
		repaint();
	}
	
	
	
	
	private void chooseFile() 
	{
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog (null);
		fileChooser.setVisible(true);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			panel.setImage(file);
			panel.setImageName (file.getName());
		}
	}

	private void readAll ()
	{
		File folder = new File(defaultLocation);
		File[] listOfFiles = folder.listFiles();

		    for (int i = 8; i < 13; i++)
		    {
			      if (listOfFiles[i].isFile())
			      {
			    	  if (!listOfFiles [i].getName().contains(".db"))
				    	{
			    		  System.out.println("File " + listOfFiles[i].getName());
					        panel.setImage(listOfFiles [i]);
				    	}
			      
			      } 
			      else if (listOfFiles[i].isDirectory())
			      {
			    	  File [] listOfSubfolderFiles = listOfFiles [i].listFiles();
			    	  for (int j = 0; j < listOfSubfolderFiles.length; j++)
					    {
						      if (listOfSubfolderFiles[j].isFile())
						      {
						    	if (!listOfSubfolderFiles [j].getName().contains(".db"))
						    	{
						    		System.out.println("File " + listOfSubfolderFiles[j].getName());
							        panel.setImage(listOfSubfolderFiles [j]);
						    	}
						        
						      } 
						      else if (listOfSubfolderFiles[j].isDirectory())
						      {
						        System.out.println("Directory " + listOfSubfolderFiles[j].getName());
						      }
					    }
			      }
		    }
	}

	private class OpenListener implements ActionListener
	{
		public void actionPerformed(ActionEvent e) 
		{
			chooseFile ();
		}
		
	}
	
	public static void main (String [] args)
	{
		new MyFrame("Iris segmentation");
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (e.getKeyCode() == KeyEvent.VK_1)
		{
			panel.getIo().undo();
			revalidate();
		}
		
		else if (e.getKeyCode() == KeyEvent.VK_2)
		{
			panel.getIo().setGaborResult (2);
			revalidate();
		}
		
		else if (e.getKeyCode() == KeyEvent.VK_3)
		{
			panel.getIo().setGaborResult (3);
			revalidate();
		}
		
		else if (e.getKeyCode() == KeyEvent.VK_4)
		{
			panel.getIo().showEncoded ();
			revalidate();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
