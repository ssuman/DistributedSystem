

import java.io.Serializable;

public class Point implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double x_axis;
	double y_axis;
	
	Point(){
		
	}
	
	Point(double xaxis,double yaxis){
		this.x_axis=xaxis;
		this.y_axis=yaxis;
	}
	
	public double getXaxis(){
		return x_axis;
	}
	
	public double getYaxis(){
		return y_axis;
	}
	public void setXaxis(double x){
		x_axis=x;
	}
	
	public void setYaxis(double y){
		y_axis=y;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "x_axis=" + x_axis + ", y_axis=" + y_axis;
	}

	
	public static double calculateDistance(Point p1,Point p2) {
		return Math.sqrt(Math.pow(p1.x_axis-p2.x_axis, 2)+Math.pow(p1.y_axis-p2.y_axis, 2));
	}
	
	
}
