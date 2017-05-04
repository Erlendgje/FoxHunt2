using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GObject : MonoBehaviour {

	private float lat, lon;
	private int id;
	private float rotation;
	private float rotationSpeed = 5;
	private float speed = 15;
	private Vector3 nextPosition;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	//Sets objects standard values and sets the objects position
	public void SetObjectValue(float lat, float lon, int id) {
		this.id = id;

		SetPosition(lat, lon);
	}

	//Sets objects position if object is inside the map area
	public void SetPosition(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
		if (CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {
			transform.position = CreateVector3.MakeVector(lon, lat);
		}
	}

	//Moves object from current position to target position
	public void MoveToPosition(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;

		//Calculates the position the object will move to.
		nextPosition = CreateVector3.MakeVector(lon, lat);

		//Checks if the object stands still
		if ((transform.position.x != nextPosition.x || transform.position.z != nextPosition.z)) {

			//Calculate speed, user position updates every 0.05 seconds while every other gameobject updates every 0.1 seconds.
			if(id == Hunter.userID) {
				speed = Vector3.Distance(transform.position, nextPosition) / 0.05f;
			}
			else {
				speed = Vector3.Distance(transform.position, nextPosition) / 0.1f;
			}

			transform.position = Vector3.MoveTowards(transform.position, nextPosition, speed * Time.deltaTime);

			//Start moving animation
			if (this.tag == "Hunter") {
				this.GetComponent<Animator>().SetBool("moving", true);
			}
		}
		//Starts idle animation
		else if (this.tag == "Hunter") {
			this.GetComponent<Animator>().SetBool("moving", false);
		}

		this.lat = lat;
		this.lon = lon;

	}

	public int GetId() {
		return id;
	}

	public float GetRotation() {
		return rotation;
	}
	public void SetRotation(float rotation) {
		this.rotation = rotation;
	}

	public float GetRotationSpeed() {
		return rotationSpeed;
	}

	public Vector3 GetNextPosition() {
		return nextPosition;
	}
}
