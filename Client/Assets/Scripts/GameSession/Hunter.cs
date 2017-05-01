using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Hunter : GObject {

	private bool taken;
	private int score;
	private string name;

	public static int userID = 0;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void SetPlayerValues(float lat, float lon, int id, bool taken, int score, string name) {
		SetObjectValue(lat, lon, id);

		this.taken = taken;
		this.score = score;
		this.name = name;

		MoveHunter(lat, lon, taken, score);
	}

	public void MoveHunter(float lat, float lon, bool taken, int score) {
		if (this.score != score && GetId() == userID) {
			this.GetComponent<AudioSource>().Play();
		}
	}
}
