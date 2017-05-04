using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Xml;

public class ServerHandler : MonoBehaviour {

	private float lat, lon;

	// Use this for initialization
	IEnumerator Start() {

		//Check if gps is on
		while (!Input.location.isEnabledByUser) {
			yield return new WaitForSeconds(1f);
		}

		Input.location.Start(1f, 0.5f);
		Input.compass.enabled = true;

		//Trying to connect to gps service
		int maxWait = 20;
		while (Input.location.status == LocationServiceStatus.Initializing && maxWait > 0) {
			yield return new WaitForSeconds(1f);
			maxWait--;
		}

		if (maxWait < 1) {
			yield break;
		}

		//Check if connection failed
		if (Input.location.status == LocationServiceStatus.Failed) {
			yield break;
		}
		else {
			lat = Input.location.lastData.latitude;
			lon = Input.location.lastData.longitude;
		}

		//Check if gps is on
		while (Input.location.isEnabledByUser) {
			yield return new WaitForSeconds(1f);
		}

		//Restart function if gps is turned off
		yield return StartCoroutine(Start());
	}

	//Returns latitude
	public float GetLat() {
		return Input.location.lastData.latitude;
	}

	//Returns longitude
	public float GetLon() {
		return Input.location.lastData.longitude;
	}

	//Returns heading from compass
	public float GetRoration() {
		return Input.compass.trueHeading;
	}

	//Returns xmlData recived from server
	public XmlDocument GetConfig() {

		string url = "http://asia.hiof.no/foxhunt-servlet/getConfig";

		XmlDocument xmlData = new XmlDocument();
		xmlData.Load(url);

		return xmlData;
	}


	//Returning data recived from server about the game flow and sending information to server about the players location 
	public XmlDocument GetState() {

		string url = "http://asia.hiof.no/foxhunt-servlet/getState";

		//Not sending information to server if user is still picking hunter
		if (Hunter.userID != 0) {
			url = "http://asia.hiof.no/foxhunt-servlet/getState?userid=" + Hunter.userID + "&lat=" + GetLat() + "&lon=" + GetLon();
		}

		XmlDocument xmlData = new XmlDocument();
		xmlData.Load(url);

		return xmlData;
	}

	//Stopping all services
	private void OnApplicationQuit() {
		Input.location.Stop();
		Input.compass.enabled = false;
	}
}
