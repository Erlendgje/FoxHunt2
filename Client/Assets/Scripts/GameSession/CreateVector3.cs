using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Xml;

public class CreateVector3{

	public static Vector2[] boundary;
	public static float northernmostPoint, easternmostPoint, southernmostPoint, westernmostPoint, inGameMapWidth, inGameMapHeight, coordinateMapWidth, coordinateMapHeight;

	public static void SetBoundary(XmlNodeList pointList) {
		boundary = new Vector2[4];
		int i = 0;
		foreach (XmlNode point in pointList) {
			boundary[i] = new Vector2(float.Parse(point.Attributes["lon"].Value), float.Parse(point.Attributes["lat"].Value));

			if (i == 0) {
				southernmostPoint = boundary[i].y;
				northernmostPoint = boundary[i].y;
				westernmostPoint = boundary[i].x;
				easternmostPoint = boundary[i].x;
			}

			if (southernmostPoint > boundary[i].y) {
				southernmostPoint = boundary[i].y;
			}

			if (northernmostPoint < boundary[i].y) {
				northernmostPoint = boundary[i].y;
			}

			if (westernmostPoint > boundary[i].x) {
				westernmostPoint = boundary[i].x;
			}

			if (easternmostPoint < boundary[i].x) {
				easternmostPoint = boundary[i].x;
			}

			i++;
		}

		coordinateMapHeight = northernmostPoint - southernmostPoint;
		coordinateMapWidth = easternmostPoint - westernmostPoint;
	}

	public static Vector3 MakeVector(float lon, float lat) {
		float z = (easternmostPoint - lon) * (inGameMapWidth / coordinateMapWidth);
		float x = (lat - southernmostPoint) * (inGameMapHeight / coordinateMapHeight);

		return new Vector3(x, 0, z);
	}

	//Function that checks if polygon contains point
	public static bool ContainsPoint(Vector2[] polyPoints, Vector2 p) {
		int j = polyPoints.Length - 1;
		bool inside = false;

		for (var i = 0; i < polyPoints.Length; j = i++) {
			if (((polyPoints[i].y <= p.y && p.y < polyPoints[j].y) || (polyPoints[j].y <= p.y && p.y < polyPoints[i].y)) &&
			   (p.x < (polyPoints[j].x - polyPoints[i].x) * (p.y - polyPoints[i].y) / (polyPoints[j].y - polyPoints[i].y) + polyPoints[i].x)) {
				inside = !inside;
			}
		}
		return inside;
	}
}
