using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class GObject : MonoBehaviour {

	private float lat, lon;
	private int id;

	// Use this for initialization
	void Start () {
		
	}
	
	// Update is called once per frame
	void Update () {
		
	}

	public void SetObjectValue(float lat, float lon, int id) {
		this.lat = lat;
		this.lon = lon;
		this.id = id;
	}

	public void SetPosition(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;

		transform.position = CreateVector3.MakeVector(lon, lat);
	}

	public void MoveToPosition(float lat, float lon) {

	}

	public int GetId() {
		return id;
	}
}
