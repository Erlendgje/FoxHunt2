/**
 * 
 */
package no.hiof.mobapp.foxhunt.game;

/**
 * @author joink
 *
 */
public class LatLongCoord {
	public double lat;
	public double lon;
	
	public static double metersPerLon = 55499.3136;
	public static double metersPerLat = 110998.0968;
	
	public LatLongCoord (double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public LatLongCoord (String lat, String lon) {
		this.lat = Double.parseDouble(lat);
		this.lon = Double.parseDouble(lon);
	}
	
	public double getDistance(LatLongCoord other)
	{
		/*
        double x = 69.1 * (other.lat - this.lat);
        double y = 69.1 * (other.lon - this.lon) * Math.cos(this.lat/57.3);
        return Math.sqrt(x * x + y * y)*1.609344*1000;
        */
        // Haversine
        
		//System.out.println("Dist between (" + lat + ", "+ lon+") - ("+ other.lat + ", " + other.lon + ")");
        double R = 6372797.560856; // earth's radius in meters
        
        double dLat = Math.toRadians(other.lat - lat);
        double dLon = Math.toRadians(other.lon - lon);
       
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(other.lat)) * 
                Math.sin(dLon/2) * Math.sin(dLon/2); 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
        double d = R * c;
        return d;
        
	}
	
	public int getBearing(LatLongCoord other)
	{
        double dLat = Math.toRadians(other.lat - lat);
        double dLon = Math.toRadians(other.lon - lon);
        
        
        //System.out.println ("dLat: " + dLat + " - dLon: " + dLon);
		double y = Math.sin(dLon) * Math.cos(other.lat);
		//System.out.println("y: " + y);
		double x = Math.cos(Math.toRadians(lat))*Math.sin(Math.toRadians(other.lat)) -
		        Math.sin(Math.toRadians(lat))*Math.cos(Math.toRadians(other.lat))*Math.cos(dLon);
		//System.out.println("x: " + x);
		double brng = Math.atan2(y, x);
		
		//brng += Math.PI/2;
		
		//System.out.println("BEARING: " + Math.toDegrees(brng));
		
		/*
		double brng = Math.atan2(
				  Math.cos(Math.toRadians(other.lat))*Math.sin(dLon),
				  (Math.cos(Math.toRadians(lat))*Math.sin(Math.toRadians(other.lat)) - Math.sin(Math.toRadians(lat)*Math.cos(Math.toRadians(other.lat))*Math.cos(dLon))));
		
		*/
		//System.out.println("BEARING: " + Math.toDegrees(brng) + " - " + (int)((Math.toDegrees(brng) + 360) % 360));
		return (int)((Math.toDegrees(brng) + 360) % 360);
		
	}
	public void mergeAvg(LatLongCoord other)
	{
		lat = (lat + other.lat)/2;
		lon = (lon + other.lon)/2;
	}
	
	public void mergeWeight(LatLongCoord other, double w1, double w2)
	{
		lat = (lat*w1 + other.lat*w2);
		lon = (lon*w1 + other.lon*w2);
	}
	
	public void moveDistance(double meters, int heading)
	{
		//System.out.println("Moving: "+meters + " with heading: " + heading);
		
		double lonDelta = (meters * Math.sin(Math.toRadians(heading)));
		double latDelta =(meters * Math.cos(Math.toRadians(heading)));
		
		//System.out.println("Deconstructed move (raw):: LonDelta: " + lonDelta + " LatDelta: " + latDelta);
		lonDelta /= metersPerLon;
		latDelta /= metersPerLat;
		
		//System.out.println("Deconstructed move (mpl):: LonDelta: " + lonDelta + " LatDelta: " + latDelta);
		
		this.lon -= lonDelta;
		this.lat += latDelta;
		
	}
	
	public void clipCoordinates(BoundingBox bb)
	{
		// Check if this object is outside bb and if so clamp it's coordinates onto the normal of the closest edge
		//LatLongCoord xy[] = bb.closestEdgeIntersectionCoordinates(this);
		
		bb.clipPoint(this);
		
		// Check which polygon segment we intersect.
		
		/*
		// Is the point to the left of the segment?
		if (xy[0].lon > lon)
		
		
		// Lower left - to avoid calculations where location is completely on the edge we move it just inside the bounding box
		if (lon <= bb.c1.lon)
			lon = bb.c1.lon*1.000001;
		
		if (lat <= bb.c1.lat)
			lat = bb.c1.lat*1.000001;
		
		if (lon >= bb.c2.lon)
			lon = bb.c2.lon*0.999999;
		
		if (lat >= bb.c2.lat)
			lat = bb.c2.lat*0.999999;
			
		*/
		
	}
	
	public String toString() {
		return Double.toString(lat) + "," + Double.toString(lon);
	}
}