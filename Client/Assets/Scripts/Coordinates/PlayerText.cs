using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PlayerText : MonoBehaviour {

	private Quaternion quaternion;

	// Use this for initialization
	void Start () {
		quaternion = Camera.main.transform.rotation;
	}
	
	// Update is called once per frame
	void Update () {
		
		if(this.transform.rotation != quaternion) {
			this.transform.rotation = quaternion;
		}

	}
}
