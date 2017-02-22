package panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ActionsPanel extends JPanel
{
	private MyPanel panel;
	
	private ActionListener toGrayListener, setThresholdListener, setLocalMinimumListener, imcomplementListener,
	findIrisListener, erodeListener, dilateListener;
	
	private ActionListener equalizeListener;
	private ActionListener removeRelfectionsListener;
	private ActionListener normalizeIrisListener;

	JTextField thresholdField = new JTextField("Threshold value", 2);
	private JButton setLocalMinimumButton, cvtToGrayButton, setThresholdButton, erodeButton, dilateButton, findIrisButton;
	private JButton imcomplementButton;
	private JButton removeReflectionsButton;
	private JButton normalizeIrisButton;
	private AbstractButton equalizeButton;

	private JButton findPupilDaugmannButton;
	private JButton findPupilButton;

	private ActionListener findPupilListener;

	private ActionListener findPupilDaugmannListener;

	private JButton findPupilCombinedButton;

	private ActionListener findPupilCombinedListener;
	
	public ActionsPanel (MyPanel panel)
	{
		this.panel = panel;
	}
	
	public void initButtons (JPanel options)
	{
		initBindings();
		
		imcomplementButton = new JButton ("Imcomplement");
		cvtToGrayButton = new JButton ("Convert to gray");
		removeReflectionsButton = new JButton ("Remove reflections");
		equalizeButton = new JButton("Equalize Histogram");
		setThresholdButton = new JButton ("Thresholding");
		setLocalMinimumButton = new JButton( "Set local minimum");
		erodeButton = new JButton ("Erosion");
		dilateButton = new JButton ("Dilation");
		findIrisButton = new JButton("Find iris");
		findPupilDaugmannButton = new JButton("Find pupil (Daugmann)");
		findPupilButton = new JButton ("Find pupil");
		findPupilCombinedButton = new JButton ("Combined pupil");
		normalizeIrisButton = new JButton ("Encode and save iris");
		
		imcomplementListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().imcomplement();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		toGrayListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().convertToGray();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		erodeListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().erode ();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		dilateListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().dilate ();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		setLocalMinimumListener = new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo().setLocalMinimum();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		setThresholdListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().setThreshold();
				panel.getTopLevelAncestor().requestFocus(); 

			}
		};
		
		removeRelfectionsListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().findAndRemoveReflections();
				panel.getTopLevelAncestor().requestFocus(); 
			}
		};
		
		equalizeListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().equalize();
				panel.getTopLevelAncestor().requestFocus(); 
				
			}
		};
		
		
		findIrisListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().findIris (55, 75);
				panel.getTopLevelAncestor().requestFocus(); 
				

			}
		};
		
		findPupilDaugmannListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().findPupilDaugmann (20, 35);
				panel.getTopLevelAncestor().requestFocus(); 
			}
		};
		
		findPupilListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().findPupilLines ();
				panel.getTopLevelAncestor().requestFocus(); 
			}
		};
		
		normalizeIrisListener = new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo().getPolarImage();
			}
		};
		
		findPupilCombinedListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				panel.getIo ().findPupilCombined (20, 35);
				panel.getTopLevelAncestor().requestFocus(); 
			}
		};
		
		
		imcomplementButton.addActionListener(imcomplementListener);
		cvtToGrayButton.addActionListener(toGrayListener);
		removeReflectionsButton.addActionListener(removeRelfectionsListener);
		equalizeButton.addActionListener (equalizeListener);
		erodeButton.addActionListener(erodeListener);
		dilateButton.addActionListener(dilateListener);
		setLocalMinimumButton.addActionListener(setLocalMinimumListener);
		setThresholdButton.addActionListener(setThresholdListener);
		findIrisButton.addActionListener (findIrisListener);
		findPupilDaugmannButton.addActionListener(findPupilDaugmannListener);
		findPupilButton.addActionListener(findPupilListener);
		normalizeIrisButton.addActionListener(normalizeIrisListener);
		findPupilCombinedButton.addActionListener(findPupilCombinedListener);

		
		options.add (cvtToGrayButton);
		options.add(equalizeButton);
		options.add(removeReflectionsButton);
		options.add(imcomplementButton);
		options.add(dilateButton);
		options.add(erodeButton);
		options.add(setThresholdButton);
		options.add (setLocalMinimumButton);
		options.add(findIrisButton);
		options.add(findPupilDaugmannButton);
		options.add(findPupilButton);
		options.add(findPupilCombinedButton);
		options.add(normalizeIrisButton);
	}
	
	public void initBindings ()
	{
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("W"), "Undo");
		getActionMap().put("Undo", new AbstractAction ()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				System.out.println("ACKA");
				panel.getIo().undo();
			}
		});

	}


	
	
}
