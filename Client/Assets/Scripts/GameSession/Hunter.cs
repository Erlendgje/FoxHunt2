using System.Collections;
using System.Collections.Generic;
using UnityEngine;

//Class implements GObject
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

	//Sets hunter values
	public void SetPlayerValues(float lat, float lon, int id, string name) {
		SetObjectValue(lat, lon, id);

		this.name = name;
	}

	//Updates values that can change and moves the other hunters
	public void UpdateHunter(float lat, float lon, bool taken, int score) {
		this.taken = taken;

		//Checks if player have caught a fox
		if (this.score != score && GetId() == userID) {
			this.GetComponent<AudioSource>().Play();
		}
		this.score = score;

		//Hunter will not move if he/her is outside the map area.
		if(CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {

			//Is only running one time player get back inside the map area. 
			if(outSide) {
				//Setting the hunter visible
				foreach(Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
					renderer.enabled = true;
				}
				outSide = false;
			}

			//Players position should not update from server. Player and other hunters position have different update rate.
			if (GetId() != userID) {
				MoveToPosition(lat, lon);

				//Sets different rotation for different avatars, this can be fixed in maya if both avatars is rotated the same way.
				if (PlayerPrefs.GetString("avatar") == "Boy") {
					SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg + 180);
				}
				else {
					SetRotation(Mathf.Atan2(transform.position.x - GetNextPosition().x, transform.position.z - GetNextPosition().z) * Mathf.Rad2Deg + 90);
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, GetRotation(), 0), GetRotationSpeed());
			}
		}
		//Only turn hunter invisible one time if he/her is outside map area
		else if(!outSide) {
			foreach (Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
				renderer.enabled = false;
			}
		}
	}

	//Function to update player position
	public void UpdateUser(float lat, float lon, float rotaiton) {
		if (CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {
			MoveToPosition(lat, lon);
		}

		SetRotation(rotaiton);

		//The girl is rotated different and need another rotaition
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
