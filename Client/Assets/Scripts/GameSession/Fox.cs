using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Fox : GObject {

	private bool isCaught;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void SetFoxValues(float lat, float lon, int id, bool isCaught) {
		SetObjectValue(lat, lon, id);
		this.isCaught = isCaught;
		SetPosition(lat, lon);
	}

	public void MoveFox(float lat, float lon, bool isCaught) {
		if (isCaught && !this.isCaught) {
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}
		else if (!isCaught && this.isCaught) {
			SetPosition(lat, lon);
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}
	}
}
