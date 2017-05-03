using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Hunter : GObject {

	private bool taken;
	private int score;
	private string name;
	private bool outSide;

	public static int userID = 0;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void SetPlayerValues(float lat, float lon, int id, string name) {
		SetObjectValue(lat, lon, id);

		this.name = name;
	}

	public void UpdateHunter(float lat, float lon, bool taken, int score) {
		this.taken = taken;

		if (this.score != score && GetId() == userID) {
			this.GetComponent<AudioSource>().Play();
		}

		this.score = score;

		if(CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {

			if(outSide) {
				foreach(Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
					renderer.enabled = true;
				}
			}

			outSide = false;
			if (GetId() != userID) {
				MoveToPosition(lat, lon);

				if (PlayerPrefs.GetString("avatar") == "Boy") {
					SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg + 180);
				}
				else {
					SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg + 90);
				}

				if (GetRotation() < 0) {
					SetRotation(360 + GetRotation());
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, GetRotation(), 0), GetRotationSpeed());
			}
		}
		else if(!outSide) {
			foreach (Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
				renderer.enabled = false;
			}
		}
	}

	public void UpdateUser(float lat, float lon, float rotaiton) {
		if (CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {
			MoveToPosition(lat, lon);
		}

		SetRotation(rotaiton);

		if (PlayerPrefs.GetString("avatar") == "Girl") {
			SetRotation(GetRotation() + 90);
		}

		transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, GetRotation(), 0), GetRotationSpeed());
	}

	public string GetName() {
		return name;
	}

	public bool GetTaken() {
		return taken;
	}

	public int GetScore() {
		return score;
	}
}
