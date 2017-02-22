package panels;

import geometry.Circle;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

import operations.ImageOperations;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MyPanel extends JPanel implements MouseMotionListener, MouseInputListener
{
	BufferedImage image;
	ImageOperations io;
	JPanel miniPanel;
	Rectangle tempRect;
	int [] tab;
	Image background;
	private String imageName;
	private Graphics2D graphics2D;

	int counter = 0;
	
	public void setCircle (int [] tab)
	{
		this.tab = tab;
	}

	JLabel x,y;
	private Circle currentCircle;
	private Circle currentPupil;
	public ArrayList <geometry.Point> linePoints;

	public ImageOperations getIo() 
	{
		return io;
	}

	public void setIo(ImageOperations io)
	{
		this.io = io;
	}
	
	public void setImageName (String name)
	{
		imageName = name;
	}

	public MyPanel ()
	{
		addMouseMotionListener(this);
		
	}
	
	@Override
	public void paintComponent (Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		graphics2D = g2d;
		
		if (image != null)
		{
			double scaleFactor = Math.min(1d, getScaleFactorToFit(new Dimension
					  (image.getWidth(), image.getHeight()), getSize()));

	          int width = getWidth() - 1;
	          int height = getHeight() - 1;

	          
	           g2d.drawImage(image,0,0, image.getWidth(), image.getHeight(), this);
       	   	g2d.setColor(new Color (255,0,0));

	           if (getCurrentCircle() != null)
	        	   {
		        	   	//g2d.drawOval(40, 100 - 70, 120, 120);
	        	   		g2d.setColor(new Color (255,255,0));
	        	   		
	        	   		int x0 = (int) getCurrentCircle().getX(), y0 = (int) getCurrentCircle().getY(), r0 = getCurrentCircle().getR();
	        	   		g2d.drawOval(x0-r0, y0-r0, 2*r0, 2*r0) ;
	        	   		
	        	   		if (linePoints != null)
	        	   		for (geometry.Point p : linePoints)
	        	   		{
	        	   			g2d.drawRect((int) p.x, (int) p.y, 1, 1);
	        	   		}
	        	      /*double m = (getCurrentCircle().getR() * Math.sqrt (2)) / 3;
	        	      
	        	      
	        	      Line2D.Double lineVertical = new Line2D.Double (getCurrentCircle().getX(),
								 (getCurrentCircle().getY() - 2*getCurrentCircle().getR()/3),
								 getCurrentCircle().getX(),
								 getCurrentCircle().getY() + 2*getCurrentCircle().getR()/3),
				
					lineHorizontal = new Line2D.Double (getCurrentCircle().getX() - getCurrentCircle().getR()+10,
									getCurrentCircle().getY(),
									getCurrentCircle().getX() + getCurrentCircle().getR() - 10,
									getCurrentCircle().getY()),
	        		   		
	        		 thirdLine = new Line2D.Double (getCurrentCircle().getX() - m,
	        										getCurrentCircle().getY() + m,
	        										getCurrentCircle().getX() + m,
	        										getCurrentCircle().getY() - m);
	        	      
	        		  g2d.drawLine((int) lineVertical.x1, (int) lineVertical.y1,
	        		   				(int)  lineVertical.x2,(int)  lineVertical.y2);
	        		  
	        		  g2d.drawLine((int) lineHorizontal.x1, (int) lineHorizontal.y1,
      		   				(int)  lineHorizontal.x2,(int)  lineHorizontal.y2);
	        		  
	        		  g2d.drawLine((int) thirdLine.x1, (int) thirdLine.y1,
      		   				(int)  thirdLine.x2,(int)  thirdLine.y2);*/
	        	   	
	        	   }
	           
	           if (getCurrentPupil() != null)
        	   {
        	   		g2d.setColor(new Color (255,0,0));
        	   		
        	   		int x0 = (int) getCurrentPupil().getX(), y0 = (int) getCurrentPupil().getY(),
        	   				r0 = getCurrentPupil().getR();
        	   		g2d.drawOval(x0-r0, y0-r0, 2*r0, 2*r0) ;
        	   	
        	   }
		}
	}
	
	public void displayAndSaveImage (double [][] wynik)
	{
	     	BufferedImage outImage = new BufferedImage(64, 16, BufferedImage.TYPE_BYTE_BINARY);
	     	Graphics2D graphics = outImage.createGraphics();     	   
	     	graphics.setColor(Color.WHITE);
	
     	   for (int a = 0; a < wynik.length; a++)
     	   {
     		   for (int b = 0; b < wynik [a].length; b++)
     		   {
     			   if (wynik [a][b] == 1)
     			   graphics.fillRect(b, a, 1, 1);
     		   }
     	   }
     	   try 
     	   {
     		   ImageIO.write(outImage, "JPG", new File (imageName));
     	   } 
     	   catch (IOException e) 
			{
				e.printStackTrace();
			}
     	   
     	   repaint ();
	}
	
	public void setLabelText (String s)
	{
		x.setText(s);
	}
	
	public Rectangle getTempRect()
	{
		return tempRect;
	}

	public void setTempRect(Rectangle tempRect)
	{
		this.tempRect = tempRect;
	}

	public double getScaleFactorToFit(Dimension original, Dimension toFit) 
	{

	    double dScale = 1d;

	    if (original != null && toFit != null)
	    {

	        double dScaleWidth = getScaleFactor(original.width, toFit.width);
	        double dScaleHeight = getScaleFactor(original.height, toFit.height);

	        dScale = Math.min(dScaleHeight, dScaleWidth);

	    }

	    return dScale;

	}
	
	public double getScaleFactor(int iMasterSize, int iTargetSize) 
	{

	    double dScale = 1;
	    if (iMasterSize > iTargetSize)
	    {

	        dScale = (double) iTargetSize / (double) iMasterSize;

	    } 
	    else 
	    {

	        dScale = (double) iTargetSize / (double) iMasterSize;

	    }

	    return dScale;

	}
	
	public void removeImage ()
	{
		image = null;
	}
	
	public void setImage (File file)
	{
		try
		{
			image = ImageIO.read(file);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		if (io == null) io = new ImageOperations(image, this);
		//else io.handleThis (image, file.getName());
		else io.loadImageToMat(image);
		tab = null;
		setCurrentCircle(null);
		setCurrentPupil(null);
		revalidate();
		getParent().repaint();
		repaint();
	}
	
	public void setImage (BufferedImage image)
	{
		this.image = image;
		
		getParent().repaint();
		repaint();
	}
	
	public boolean save (String name)
	{
		try 
		{
			File file = new File (System.getProperty("user.dir") + "/" + name + ".jpg");
			ImageIO.write(image, "jpg", file);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	public boolean saveImage (String name)
	{
          if (getCurrentCircle() != null)
          {
	       	   Graphics graphics = image.getGraphics();
	       	   graphics.setColor(new Color (255,255,0));
	       	   graphics.drawOval((int) getCurrentCircle().getX()-getCurrentCircle().getR(),
	       			   		(int) getCurrentCircle().getY() - getCurrentCircle().getR(),
	       			   				getCurrentCircle().getR() * 2, 
	       			   					getCurrentCircle().getR() * 2);
	       	   
	       	   graphics.drawOval((int) getCurrentPupil().getX()-getCurrentPupil().getR(),
   			   		(int) getCurrentPupil().getY() - getCurrentPupil().getR(),
			   			   	getCurrentPupil().getR() * 2, 
			   			   		getCurrentPupil().getR() * 2);	
          }
          
		try 
		{
			File file = new File (System.getProperty("user.dir") + "/" + name);
			ImageIO.write(image, "jpg", file);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		
	}
	
	Point mouseClicked, mouseReleased;
	
	@Override
	public void mouseReleased (MouseEvent e)
	{

	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public Circle getCurrentCircle() {
		return currentCircle;
	}

	public void setCurrentCircle(Circle currentCircle) {
		this.currentCircle = currentCircle;
	}

	public Circle getCurrentPupil() {
		return currentPupil;
	}

	public void setCurrentPupil(Circle currentPupil) {
		this.currentPupil = currentPupil;
	}

	

	
}
