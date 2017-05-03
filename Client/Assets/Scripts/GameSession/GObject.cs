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

	public void SetObjectValue(float lat, float lon, int id) {
		this.id = id;

		SetPosition(lat, lon);
	}

	public void SetPosition(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
		if (CreateVector3.ContainsPoint(CreateVector3.boundary, new Vector2(lon, lat))) {
			transform.position = CreateVector3.MakeVector(lon, lat);
		}
	}

	public void MoveToPosition(float lat, float lon) {

		this.lat = lat;
		this.lon = lon;

		nextPosition = CreateVector3.MakeVector(lon, lat);

		if ((transform.position.x != nextPosition.x || transform.position.z != nextPosition.z)) {

			if(id == Hunter.userID) {
				speed = Vector3.Distance(transform.position, nextPosition) / 0.1f;
			}
			else {
				speed = Vector3.Distance(transform.position, nextPosition) / 0.2f;
			}
			

			transform.position = Vector3.MoveTowards(transform.position, nextPosition, speed * Time.deltaTime);
			if (this.tag == "Hunter") {
				this.GetComponent<Animator>().SetBool("moving", true);
			}
		}
		else if (this.tag == "Hunter") {
			this.GetComponent<Animator>().SetBool("moving", false);
		}

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
