package operations;

import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;

public class IrisEncoder 
{
	private Mat gaborKernelReal, gaborKernelImaginary, sourceMat, filteredReal,
				filteredImaginary;
	
	public IrisEncoder (Mat sourceMat)
	{
		this.sourceMat = sourceMat;
		filteredReal = new Mat (sourceMat.size (), CvType.CV_32F);
		filteredImaginary = new Mat (sourceMat.size(), CvType.CV_32F);
		
				
		gaborKernelReal = Imgproc.getGaborKernel(sourceMat.size(),
																	3, 			// standard deviation
																	Math.PI/2,  // angle
																	Math.PI, 			// lambda
																	1, 		// gamma
																	Math.PI/4, 			// phase
																	CvType.CV_32F);
		
		gaborKernelImaginary=getGaborKernelImaginary(sourceMat.size(),
																	3, 			// standard deviation
																	Math.PI/2,  // angle
																	Math.PI,  			// lambda
																	1, 		// gamma
																	Math.PI/4, 			// phase
																	CvType.CV_32F);
		
		
	/*	for (int i = 0; i < gaborKernelReal.height(); i++)
		{
			for (int j = 0; j < gaborKernelReal.width(); j++)
			{
				System.out.println(gaborKernelReal.get(i,j) [0]);
			}
		}*/
	}
	
	public Mat gaborFilterReal ()
	{
		Imgproc.filter2D(sourceMat, filteredReal, CvType.CV_32F, gaborKernelReal);
		for (int i = 0; i < filteredReal.height(); i++)
		{
			for (int j = 0; j < filteredReal.width(); j++)
			{
				if (i < 64 && j < 64)
				{
					System.out.print("Kernel: " + gaborKernelReal.get(i, j) [0] + ", ");
					System.out.println((int) filteredReal.get(i,j) [0]);
					System.out.println();
				}
					
			}
		}
		Mat filtered8 = new Mat (filteredReal.size(), CvType.CV_8UC1);
		for (int i = 0; i < filteredReal.height(); i++)
		{
			for (int j = 0; j < filteredReal.width(); j++)
			{
				double data255 = filteredReal.get(i, j) [0]*255;
				filtered8.put(i,j,data255);
				//System.out.println(data255);
			}
		}
		
		return filtered8;
	}
	
	public Mat gaborFilterImaginary ()
	{
		Imgproc.filter2D(sourceMat, filteredImaginary, CvType.CV_32F, gaborKernelImaginary);
		Mat filtered8 = new Mat (filteredImaginary.size(), CvType.CV_8UC1);
		for (int i = 0; i < filteredImaginary.height(); i++)
		{
			for (int j = 0; j < filteredImaginary.width(); j++)
			{
				double data255 = filteredImaginary.get(i, j) [0]*255;
				filtered8.put(i,j,data255);
				//System.out.println(data255);
			}
		}
		
		return filtered8;
		//return filteredImaginary;
	}
	
	public Mat getGaborKernelReal ()
	{
		return gaborKernelReal;
	}
	
	public Mat getGaborKernelImaginary (Size ksize, double sigma, double theta,
            double lambd, double gamma, double psi, int ktype)
	{
	double sigma_x = sigma;
	double sigma_y = sigma/gamma;
	int nstds = 3;
	int xmin, xmax, ymin, ymax;
	double c = Math.cos(theta), s = Math.sin(theta);
	
	if (ksize.width > 0)
	{
		xmax = (int) ksize.width/2;
	}
	else
	{
		xmax = Math.round ((int) Math.max(Math.abs(nstds*sigma_x*c),
											Math.abs(nstds*sigma_y*s)));
	}
	
	if(ksize.height > 0)
	ymax = (int) ksize.height/2;
	else
	ymax = Math.round ((int) Math.max (Math.abs(nstds*sigma_x*s),
										Math.abs(nstds*sigma_y*c)));
	
	xmin = -xmax;
	ymin = -ymax;
	
	if (! (ktype == CvType.CV_32F || ktype == CvType.CV_64F )) 
		throw new CvException
					("Failed to assert! "
							+ "(ktype == CvType.CV_32F || ktype == CvType.CV_64F )");
	
	Mat kernel = new Mat (ymax - ymin + 1, xmax - xmin + 1, ktype);
	double scale = 1;
	double ex = -0.5/(sigma_x*sigma_x);
	double ey = -0.5/(sigma_y*sigma_y);
	double cscale = Math.PI*2/lambd;
	
	for( int y = ymin; y <= ymax; y++ )
	for( int x = xmin; x <= xmax; x++ )
	{
		double xr = x*c + y*s;
		double yr = -x*s + y*c;
		
		double v = scale*Math.exp(ex*xr*xr + ey*yr*yr)*Math.sin(cscale*xr + psi);
		if( ktype == CvType.CV_32F )
		    kernel.put (ymax - y, xmax - x, (float) v);
		else
		    kernel.put (ymax - y, xmax - x, v);
	}
	
		return kernel;
	}
	
	public double [][] encodeIris ()
	{
		Mat result = new Mat (16, 64, 
								CvType.CV_32F); // to get 32 blocks in x and 16 blocks in y
		gaborFilterReal();
		gaborFilterImaginary();
		
		
		int borderValue = 255 / 2;
		Mat meansReal = new Mat (16, 32, CvType.CV_32F);
		Mat meansImaginary = new Mat (16, 32, CvType.CV_32F);
		int x = 0, y = 0;

	/*	for (int i = 0; i < filteredReal.height(); i += 4, y++)
		{
			x = 0;
			for (int j = 0; j < filteredReal.width(); j += 16, x++)
			{
				double meanReal = Core.mean (filteredReal.submat(new Rect (j, i, 16,4))).val [0];
				double meanI = Core.mean (filteredImaginary.submat(new Rect (j, i, 16,4))).val [0];
				meansReal.put(y, x, meanReal);
				meansImaginary.put (y,x, meanI);
			}
		}*/
		
		Mat [][] arrayReal = new Mat [16][32],
				  arrayImg = new Mat [16][32];
		for (int i = 0; i < filteredReal.height(); i += 4, y++)
		{
			x = 0;
			for (int j = 0; j < filteredReal.width(); j += 16, x++)
			{
				Mat submatReal = filteredReal.submat(new Rect (j,i,16,4));
				Mat submatImg = filteredImaginary.submat(new Rect (j,i,16,4));
				arrayReal [y][x] = submatReal;
				arrayImg [y][x] = submatImg;
			}
		}
		
		double [][] resultArray = new double [16][64];
		int col = 0;
		for (int i = 0; i < arrayReal.length; i++)
		{
			col = 0;
			for (int j = 0; j < arrayReal [i].length; j++)
			{
				double meanReal = Core.mean(arrayReal [i][j]).val [0];
				meansReal.put(i, j, meanReal);
				System.out.print((meanReal < 0) ? 0 : 1);
				resultArray [i][col++] = (meanReal < 0) ? 0 : 1;
				
				double meanImg = Core.mean(arrayImg [i][j]).val [0];
				resultArray [i][col++] = (meanImg < 0) ? 0 : 1;
				
				
				meansImaginary.put(i, j, meanImg);
				System.out.print((meanImg < 0) ? 0 : 1);
				System.out.print("  ");
			}
			System.out.println();
		}
		
		return resultArray;
		
		/*MinMaxLocResult minmaxReal = Core.minMaxLoc(filteredReal),
						minmaxImg = Core.minMaxLoc(filteredImaginary);
		
		double minReal = minmaxReal.minVal,
				maxReal = minmaxReal.maxVal,
				minImg = minmaxImg.minVal,
				maxImg = minmaxImg.maxVal;
		
		System.out.println("Min real : " + minReal);
		System.out.println("Max real : " + maxReal);
		System.out.println("Min img : " + minImg);
		System.out.println("Max img : " + maxImg);

		
		double thresReal = (Math.abs(minReal - maxReal))/3.0,
				thresImg = (Math.abs(minImg - maxImg)) / 3.0;
		
		System.out.println("Thres real: " + thresReal);
		System.out.println("Thres 2x real:");
		
		for (int i = 0; i < filteredReal.height(); i++)
		{
			int col = 0;
			for (int j = 0; j < filteredReal.width(); j++, col += 2)
			{
				double currentRealValue, currentImaginaryValue;
				//System.out.println(meansReal.get(i, j) [0]);
				double dataReal = (filteredReal.get(i, j) [0]);
				double dataImaginary = (filteredImaginary.get(i, j) [0]);
				
				if (dataReal < minReal + thresReal)
				currentRealValue = 0;
				else if (dataReal < minReal + 2*thresReal)
					currentRealValue = 0.5;
				else currentRealValue = 1;
				
				if (dataImaginary < minImg + thresImg)
					currentImaginaryValue = 0;
				else if (dataImaginary < minImg + 2*thresImg)
					currentImaginaryValue = 0.5;
				else currentImaginaryValue = 1;
				
				result.put(i, col, currentRealValue);
				result.put(i, col+1, currentImaginaryValue);
				//if (currentRealValue == 0 && currentImaginaryValue != 0) System.out.println("kurwa");
			}
			
			//System.out.println(result.get (i, 2) [0]);
		}
		
		Mat result2 = new Mat (result.size(), CvType.CV_8UC1);
		int i = 0;
		for (i = 0; i < result.height(); i++)
		{
			for (int j = 0; j < result.width(); j++)
			{
				double data255 = result.get(i, j) [0]*255;
				result2.put(i,j,data255);
				//System.out.println(data255);
			}
		}
		
		return result2;*/
	}
}
