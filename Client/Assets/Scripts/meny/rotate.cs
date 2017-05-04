using UnityEngine;
using System.Collections;

public class rotate : MonoBehaviour {




	void FixedUpdate () 
		{
		//Rotates the object the script is placed on
		transform.Rotate (new Vector3 (0.0f, 30.0f, 0.0f) * Time.deltaTime);
		}
	}
