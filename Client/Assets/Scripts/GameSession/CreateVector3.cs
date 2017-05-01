using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CreateVector3{

	public static float easternmostPoint, southernmostPoint, inGameMapWidth, inGameMapHeight, coordinateMapWidth, coordinateMapHeight;

	public static Vector3 MakeVector(float ln, float lt) {
		float z = ((float)easternmostPoint - ln) * (inGameMapWidth / (float)coordinateMapWidth);
		float x = (lt - (float)southernmostPoint) * (inGameMapHeight / (float)coordinateMapHeight);
		return new Vector3(x, 0, z);
	}
}
