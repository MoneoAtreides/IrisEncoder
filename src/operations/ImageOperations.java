package operations;

import geometry.Circle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.Box.Filler;
import javax.swing.text.MaskFormatter;

import org.opencv.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import panels.MyPanel;

public class ImageOperations 
{
	private BufferedImage image;
	private MyPanel panel;
	private Mat originalMat, grayMat, sourceMat, thresholdMat, minimumMat, erodedMat, reflectionsMat, copy,
					beforeBlurMat;
	

	private double thresholdValue;
	private double [][] thresholdArray, grayArray, sourceArray;
	
	private HashMap <Point, Double> thresholdMap, minimumMap, workMap;
	private Mat dilatedMat;
	
	private LinkedList <Mat> savedActions;
	private Mat lastMat;
	
	private Rectangle irisRect;
	private Circle irisContour;
	
	private Calculations calculations;
	
	private double radialRes = 64,
					angularRes = 512;
	private Circle pupil;
	private Mat gaborReal;
	private Mat gaborImaginary;
	private Mat polarMat;
	private Mat encodedMat;

	public ImageOperations (BufferedImage image, MyPanel panel)
	{
		calculations = new Calculations(this);
		this.panel = panel;
		this.image = image;
		
		   try
		   {
		         System.loadLibrary( Core.NATIVE_LIBRARY_NAME );   
		   } 
		   catch (Exception e)
		   {
		         System.out.println("Error: " + e.getMessage());
		   }
		   
		   originalMat = loadImageToMat(image);
}
	
	public Mat loadImageToMat (BufferedImage image)
	{
		thresholdMat = null;
		reflectionsMat = null;
		sourceMat = null;
		
			 byte [] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
	         if (sourceMat == null) sourceMat = new Mat (image.getHeight(), image.getWidth(), CvType.CV_8UC1);
	         sourceMat.put(0, 0, data);
	         savedActions = new LinkedList<Mat> ();
	         if (copy == null) copy = new Mat ();
	         sourceMat.copyTo(copy);
	         savedActions.add(copy);
	         return sourceMat;
	}
	
	public void setThreshold ()
	{
		if (! (sourceMat.type() == CvType.CV_8UC1)) return;
		thresholdMat = new Mat (image.getHeight(), image.getWidth(), CvType.CV_8UC1);
		
		double sum = 0;
		int i, j = 0;
		
		for (i = 0; i < sourceMat.height(); i++)
		{
			for (j = 0; j < sourceMat.width(); j++)
			{
				sum += sourceMat.get(i, j) [0];
			}
		}
		
		double avg = sum / (i * j);
		double smallSum = 0;
		int count = 0;
		for (i = 0; i < sourceMat.height(); i++)
		{
			for (j = 0; j < sourceMat.width(); j++)
			{
				double intensity = sourceMat.get(i, j) [0];
				if (intensity < avg)
				{
					smallSum += intensity;
					count ++;
				}
			}
		}
		
		double smallAvg = smallSum / count;
		
		double threshold = smallAvg / 2;
				thresholdValue = threshold;
				
		Imgproc.threshold (sourceMat,thresholdMat, 30, 255, Imgproc.THRESH_BINARY);
		thresholdArray = matToArray(thresholdMat);
		doAction (thresholdMat);
		System.out.println("THRESHOLD " + thresholdMat.height()*thresholdMat.width());
	}
	
	public void findAndRemoveReflections ()
	{
		if (! (sourceMat.type() == CvType.CV_8UC1)) return;
		copy = new Mat ();
		beforeBlurMat = new Mat (sourceMat.size(), CvType.CV_8UC1);
		sourceMat.copyTo(beforeBlurMat);
		sourceMat.copyTo(copy);
		reflectionsMat = new Mat (image.getHeight(), image.getWidth(), CvType.CV_8UC1);
		Imgproc.threshold(sourceMat, reflectionsMat, 235, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C);
		sourceMat = reflectionsMat;
		dilate();
		dilate ();
		dilate ();

		HashMap <Point, Double> filledReflectionsMap = new HashMap<>();		
		Photo.inpaint(copy, sourceMat, copy, 10, Photo.INPAINT_NS);
		Photo.inpaint(beforeBlurMat, sourceMat, beforeBlurMat, 10, Photo.INPAINT_NS);
		
		int sum = 0;
		for (int i = 0; i < sourceMat.rows(); i++)
		{
			for (int j = 0; j < sourceMat.cols(); j++)
			{
				double intensity = sourceMat.get(i, j) [0];
				if (intensity != 0)
				{
					filledReflectionsMap.put(new Point (j,i), copy.get(i, j) [0]); // dodaje niezerowe piksele
				}																	// do mapy refleksow
				sum += copy.get(i, j) [0];	// i przy okazji liczy sume wartosci pikseli z obrazu bez refleksow
			}
		}
		
		double avg = sum / (sourceMat.height() * sourceMat.width()); // srednia intensywnosc bez refleksow
		
		for (Map.Entry<Point, Double> entry : filledReflectionsMap.entrySet())
		{
			double currentIntensity = entry.getValue();
			if (currentIntensity < 1.5 * thresholdValue)
			{
				Point currentPoint = entry.getKey ();
				
					copy.put((int) currentPoint.y, (int) currentPoint.x, 0);	
			}
		}
		
		Imgproc.medianBlur(copy, copy, 7);
		Imgproc.medianBlur(copy, copy, 7);
		doAction(copy);
		equalize();
	}
	
	public HashMap <Point, Double> setLocalMinimum ()
	{
		minimumMat = new Mat (sourceMat.rows()-25, sourceMat.cols()-25, CvType.CV_8UC1);
		Core.bitwise_not(minimumMat, minimumMat);
		double minimum = 255;
		int height = sourceArray.length, width = sourceArray [0].length;
		HashMap <Point, Double> map = new HashMap<>();
		
		if (copy == null) copy = sourceMat;
		double [][] copyArray = matToArray(copy);
		
		for (int i = 25; i < height-25; i++)
		{
			for (int j = 25; j < width-25; j++)
			{	
				double currentPixelIntensity = thresholdArray [i][j];
				if (currentPixelIntensity == 255)
					continue;
				
				//int x = 0, y = 0;
				// check neighbors
				if (i > 0 && j > 0 && i < height-1 && j < width - 1)
				{
					for (int a = 0; a < 3; a++)
					{
						for (int b = 0; b < 3; b++)
						{
							double data = copyArray [i-1 + a][j-1 + b];
							if (data < minimum)
							{
								minimum = data;
								//x = i-1 + a;
								//y = j-1 + b;
							}
						}
					}
					
					double intensity = copyArray [i][j];
					if (intensity == minimum)
					{
						//output [i][j] = intensity;
						minimumMat.put(i, j, intensity);
						//map.put(new Point (j,i), intensity);
						//minimumMat.put(x, y, 0);
						
					}
				}
				
				minimum = 255;
			}
		}
		
		sourceMat = minimumMat;
		//dilate ();
		doAction(sourceMat);
		
		for (int i = 25; i < height-25; i++)
		{
			for (int j = 25; j < width-25; j++)
			{							
					double intensity = sourceMat.get (i,j) [0];
					if (intensity != 255)
					{
						map.put(new Point (j,i), intensity);
					}
			}
		}
				
		
		workMap = map;
		System.out.println(originalMat.size());
		System.out.println("MAP SIZE " + workMap.size());

		return null;
	}
	
	public double [][] matToArray (Mat mat)
	{
		Size imgSize = mat.size();
		double width = imgSize.width;
		double height = imgSize.height;
		
		double array [][] = new double [(int) height][(int) width];

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
					array [i][j] = mat.get(i, j) [0];
			}
		}
		
		return array;
	}
	
	public Mat fromMapToMat (HashMap <Point, Double> map)
	{
		Mat mat = new Mat (sourceMat.rows (), sourceMat.cols(), CvType.CV_8UC1);
		for (Map.Entry<Point, Double> entry : map.entrySet())
		{
			Point p = entry.getKey();
			mat.put ((int) p.x, (int) p.y, entry.getValue());
		}
		
		return mat;
	}
	
	public HashMap <Point, Double> fromMatToMap (Mat mat)
	{
		HashMap <Point, Double> map = new HashMap<>();
		
		for (int i = 0; i < mat.rows(); i++)
		{
			for (int j = 0; j < mat.cols(); j++)
			{
				double intensity = mat.get(i, j) [0];
				map.put(new Point (j, i), intensity);
			}
		}
		
		return map;
	}

	public void setToBlack ()
	{
		if (sourceMat == null) return;
		//Mat mat = sourceMat.clone();
		
		for (int i = 0; i < sourceMat.rows(); i++)
		{
			for (int j = 0; j < sourceMat.cols(); j++)
			{
				if (sourceMat.get(i, j) [0] != 255) sourceMat.put(i, j, 0);
			}
		}
		
		doAction(sourceMat);
	}
	
	public void convertToGray ()
	{
		if (CvType.CV_8UC1 == sourceMat.channels()) return;
         grayMat = new Mat(image.getHeight(),image.getWidth(),CvType.CV_8UC1);
         Imgproc.cvtColor(sourceMat, grayMat, Imgproc.COLOR_RGB2GRAY);
         //Imgproc.equalizeHist(grayMat, grayMat);
         grayArray = matToArray(grayMat);
         
         doAction(grayMat); 
	}
	
	public void setThresholdInv (double threshold)
	{
		Mat newMat = sourceMat;
		double thresh = threshold * 255;
		Imgproc.threshold(grayMat, newMat, thresh, 255, Imgproc.THRESH_TOZERO_INV);
		
		doAction (newMat);
	}
	
	public void imcomplement ()
	{
		Mat newMat = sourceMat;
		Core.bitwise_not(sourceMat, newMat);
		doAction(newMat);
	}
	
	private void doAction (Mat mat)
	{
		setNewValues(mat);
		savedActions.add(mat);
	}
	

	public Mat setNewValues(Mat newMat)
	{
		sourceMat = newMat;
		byte [] newData = new byte [newMat.rows () * newMat.cols() * (int) newMat.elemSize()];
		newMat.get(0, 0, newData);
		BufferedImage newImage = new BufferedImage (newMat.cols(), newMat.rows(), BufferedImage.TYPE_BYTE_GRAY);
		newImage.getRaster().setDataElements(0, 0, newMat.cols(), newMat.rows(), newData);
		panel.setImage(newImage);	
		
		sourceArray = matToArray(sourceMat);
		lastMat = sourceMat;
		return sourceMat;
	}
	
	public Mat setGaborResult (int number)
	{
		Mat gabor;
		if (number == 2) gabor = gaborReal;
		else gabor = gaborImaginary;
		sourceMat = gabor;
		byte [] newData = new byte [gabor.rows () * gabor.cols() * (int) gabor.elemSize()];
		gabor.get(0, 0, newData);
		BufferedImage newImage = new BufferedImage (gabor.cols(), gabor.rows(), BufferedImage.TYPE_BYTE_GRAY);
		newImage.getRaster().setDataElements(0, 0, gabor.cols(), gabor.rows(), newData);
		panel.setImage(newImage);
		sourceArray = matToArray(sourceMat);
		lastMat = sourceMat;
		return sourceMat;
	}
	
	public void undo ()
	{
		if (savedActions.size () > 1)
		{
			savedActions.removeLast();
		}
		
		setNewValues(savedActions.getLast());
		panel.repaint();
	}
	
	public Circle findMaxChangeCircle (int changeSign, int rStart, int rMin, int rMax, Rect rect, Circle bound)
	{
		double diff = 0;
		double max = 0;
		double distance = -1;
		if (bound != null && irisContour != null)
		{
			distance = Math.hypot(Math.abs(irisContour.getX() - bound.getX()),
									Math.abs(irisContour.getY() - bound.getY()));
		}
		
		Circle c1 = null, maxChange = null;
				System.out.println("NOWY " + workMap.size());
		for (Map.Entry<Point, Double> entry : workMap.entrySet())
		{
			Point p = entry.getKey();			
			double xTest = p.x, yTest = p.y;
			int rTest = rStart;
			
			if (bound != null)
			{
				if (distance > Math.abs (irisContour.getR() - bound.getR()))
					continue;
			}
			
			if (p.inside (rect))
			{
				Circle previousCircle = new Circle ((int) p.x, (int) p.y, rMin, copy);
				double lastSum = previousCircle.getSumImproved();
				
				for (rTest = rMin+1; rTest+1 < rMax; rTest ++)
				{
					if (!Calculations.fitsImage(rTest,xTest, yTest, copy.size())) continue;

					c1 = new Circle ((int) xTest, (int) yTest, rTest, copy);
					double currentSum = c1.getSumImproved();
					diff = currentSum - lastSum;
					lastSum = currentSum;	
						if (diff > max)
						{
							max = diff;
							//c1.setR(rTest - 1);
							maxChange = c1;
						}
				}
			}
		}
		
		return maxChange;
	}
	
	public void findIris (int rMin, int rMax)
	{				
		Circle iris = findMaxChangeCircle (1, 50, rMin, rMax,
							 new Rect ((int) sourceMat.size().width/4, (int)  sourceMat.size().height/4,
									 	(int)  sourceMat.size().width/2, (int) sourceMat.size().height/2),
									 	null);

		irisContour = iris;
		panel.setCurrentCircle(iris);
		setNewValues(copy);
		irisRect = new Rectangle ((int) irisContour.getX() - irisContour.getR(), 
									(int) irisContour.getY() - irisContour.getR(),
									irisContour.getR() * 2, 
									irisContour.getR() * 2);
		
		irisContour.setRect(irisRect);
	}
	
	public Circle findPupilDaugmann (int rMin, int rMax)
	{
		pupil = findMaxChangeCircle (-1, 10, rMin, rMax,
											new Rect (irisRect.x+irisRect.width/3,
														irisRect.y+irisRect.height/3,
															irisRect.width/3, irisRect.height/3),
															irisContour);
		panel.setCurrentPupil(pupil);
		setNewValues(copy);
		return pupil;
	}
	
	public void findPupilCombined (int rMin, int rMax)
	{
		Circle pupilDaugmann = findPupilDaugmann(rMin, rMax);
		Circle pupilLines = findPupilLines();
		pupil = new Circle ((int) (pupilDaugmann.getX() + pupilLines.getX()) / 2, 
							(int) (pupilDaugmann.getY() + pupilLines.getY()) / 2, 
									(pupilDaugmann.getR() + pupilLines.getR()) / 2);
		panel.setCurrentPupil(pupil);
		setNewValues(copy);
	}
	
	public void panelSetLine (ArrayList <geometry.Point> list)
	{
		panel.linePoints = list;
	}
	
	public void getPolarImage ()
	{
		Mat result = new Mat ((int) radialRes, (int) angularRes, CvType.CV_8UC1);
		HashMap <geometry.Point, geometry.Point> polarCoords = calculations.convertToPolarCoords
												(irisContour, pupil, radialRes, angularRes);
		
		
		for (Map.Entry<geometry.Point, geometry.Point> entry : polarCoords.entrySet())
		{
			geometry.Point oldPoint = entry.getKey(),
					newPoint = entry.getValue ();
			
			try
			{
				if (beforeBlurMat == null) beforeBlurMat = originalMat;
				result.put ((int) newPoint.y, (int) newPoint.x,
						beforeBlurMat.get ((int) oldPoint.y, (int) oldPoint.x) [0]);
				/*System.out.println("SSSSAASASAS " + (int) newPoint.x + ", " + (int) newPoint.y + ", kolor: " +
						beforeBlurMat.get ((int) oldPoint.y, (int) oldPoint.x) [0] + " z "
						+ ((int) oldPoint.x + ", " + (int) oldPoint.y));*/
			}
			catch (NullPointerException e)
			{
				System.err.println("y = " + (int) newPoint.y + ", x = " + (int) newPoint.x +
						 ", old y = " + (int) oldPoint.y + ", old x = " + (int) oldPoint.x);
			}
						
		}
		
		polarMat = result;
		setNewValues(result);
		panel.save("Normalized");
		
		IrisEncoder ie = new IrisEncoder(polarMat);
		gaborReal = ie.gaborFilterReal();
				gaborImaginary = ie.gaborFilterImaginary();
				
				gaborReal.convertTo(gaborReal, CvType.CV_8UC1);
				
				
		panel.setCurrentCircle(null);
		panel.setCurrentPupil(null);
		double [][] wynik = ie.encodeIris();
		panel.displayAndSaveImage(wynik);
		panel.removeImage ();
		panel.repaint();
		return;
	}
	
	public void showEncoded ()
	{
		   setNewValues(encodedMat);
			panel.saveImage("Iriscode.jpg");

	}
	
	public Circle findPupilLines ()
	{	
		double m = (irisContour.getR() * Math.sqrt (2)) / 3 - 10;
		
		Line2D.Double lineVertical = new Line2D.Double (irisContour.getX(),
												 (irisContour.getY() - 2*irisContour.getR()/3 + 30),
												 irisContour.getX(),
												 irisContour.getY() + 2*irisContour.getR()/3),
								
			lineHorizontal = new Line2D.Double (irisContour.getX() - irisContour.getR()+15,
					   						   irisContour.getY(),
					   						   irisContour.getX() + irisContour.getR() - 15,
					   						   irisContour.getY()),
		
			 thirdLine = new Line2D.Double (irisContour.getX() - m,
											irisContour.getY() + m,
											irisContour.getX() + m,
											irisContour.getY() - m);
		
		
		geometry.Point p1 = countLineChange(lineVertical),
				p2 = countLineChange(lineHorizontal),
					p3 = countLineChange(thirdLine);
		
		/*System.out.println(p1.x + "," + p1.y + "\n" + p2.x + "," + p2.y + "\n" 
							+ p3.x + "," + p3.y);*/
		
		
		
		pupil = Calculations.findCircleCenter(p1, p2, p3);
		
		//if (pupil.getR() < irisContour.getR()/2)
		{
			panel.setCurrentPupil(pupil);
		}

		
		setNewValues(copy);
		return pupil;
		//System.out.println((int) x0 + " " + (int) y0 + ", r = " + radius);
	}
	
	public geometry.Point countLineChange (Line2D.Double line)
	{
		Rectangle2D bound = line.getBounds2D();
		double lx = bound.getX(),
				ly = bound.getY();
		double width = bound.getWidth()+1,
				height = bound.getHeight()+1;
		
		Rectangle2D newBound = null;
		if (bound.getWidth() == 0)
		{
			width = 1;
			newBound = new Rectangle2D.Double (lx,ly,width,height);
			newBound.setRect(lx, ly, width, height);
		}
		if (bound.getHeight() == 0)
		{
			height = 1;
			if (newBound != null)
			{
				newBound.setRect(lx, ly, width, height);
			}
			else newBound = new Rectangle2D.Double (lx,ly,width,height);
		}
		if (newBound != null) bound = newBound;
		
		double maxDiff = 0;
		geometry.Point maxPoint = null;
		geometry.Point previousPoint = new geometry.Point (line.getP1().getX(), line.getP1().getY());
		
		if (width != 1 && height != 1)
		{
			for (int x = (int) line.x1+1, y = (int) line.y1-1; x < line.x2 && y > line.y2; x++, y--)
			{
				geometry.Point currentPoint = new geometry.Point (x,y);
					if (!irisRect.contains(new Point2D.Double(currentPoint.x, currentPoint.y))) continue;
					if (previousPoint.equals(currentPoint)) continue;
					double currentDiff = sourceMat.get(y, x) [0] - 
										sourceMat.get ((int) previousPoint.y,(int) previousPoint.x) [0];
					if (Math.abs(currentDiff) > maxDiff)
					{
						maxPoint = currentPoint;
						maxDiff = currentDiff;
					
					}
					previousPoint = currentPoint;
			}
		}
		else
		for (int x = (int) lx; x < lx + width; x++)
		{
			for (int y = (int) ly; y < ly + height; y++)
			{
				geometry.Point currentPoint = new geometry.Point (x,y);
				if (bound.contains(currentPoint.x, currentPoint.y))
				{
					if (previousPoint.equals(currentPoint)) continue;
					double currentDiff = sourceMat.get(y, x) [0] - 
										sourceMat.get ((int) previousPoint.y,(int) previousPoint.x) [0];
					if (Math.abs(currentDiff) > maxDiff)
					{		
							maxPoint = currentPoint;
							maxDiff = currentDiff;
					}
				}
					previousPoint = currentPoint;
			}
		}
		
		if (height != 0 && width != 0)
		{
			return new geometry.Point (maxPoint.x +1, maxPoint.y);
		}
		return maxPoint;
	}
	
	public void dilate ()
	{ 
		dilatedMat = new Mat (sourceMat.rows(), sourceMat.cols(), CvType.CV_8UC1);
		Imgproc.dilate(sourceMat, dilatedMat, 
						Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
		setNewValues(dilatedMat);
		//imfill();
	}
	
	public void erode ()
	{ 
		erodedMat = new Mat (sourceMat.rows(), sourceMat.cols(), CvType.CV_8UC1);
		Imgproc.erode(sourceMat, erodedMat, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3,3)));
		doAction (erodedMat);
		//imfill();
	}
	
	public Mat getSourceMat()
	{
		return sourceMat;
	}

	public void equalize() 
	{
		Mat mat = sourceMat;
		Imgproc.equalizeHist(sourceMat, mat);
		if (copy != null) Imgproc.equalizeHist(copy, mat);
		doAction(mat);
	}

	public void handleThis(BufferedImage image, String name)
	{
		this.image = image;
			
			
		   originalMat = loadImageToMat(image);
		   //convertToGray();
		   equalize();
		   findAndRemoveReflections();
		   setThreshold();
		   setLocalMinimum();
		   findIris(55,65);
		   findPupilLines();
		   getPolarImage();
		   showEncoded();
		  // while (savedActions.size() > 1) undo ();
		   panel.saveImage (name);
	}
}
