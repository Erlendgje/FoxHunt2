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

	public void SetFoxValues(float lat, float lon, int id) {
		SetObjectValue(lat, lon, id);
	}

	public void UpdateFox(float lat, float lon, bool isCaught) {
		if (isCaught && !this.isCaught) {
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}
		else if (!isCaught && this.isCaught) {
			SetPosition(lat, lon);
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}

		this.isCaught = isCaught;

		MoveToPosition(lat, lon);

		if (PlayerPrefs.GetString("fox") == "FoxReal") {
			SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg + 90);
		}
		else if (PlayerPrefs.GetString("fox") == "FoxFake") {
			SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg - 90); 
		}

		if (GetRotation() < 0) {
			SetRotation(360 + GetRotation());
		}

		transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, GetRotation(), 0), GetRotationSpeed());
	}
}
