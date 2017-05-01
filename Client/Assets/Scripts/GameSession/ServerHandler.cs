using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Xml;

public class ServerHandler : MonoBehaviour {

	private float lt, ln;
	private XmlDocument xmlData;

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
			lt = Input.location.lastData.latitude;
			ln = Input.location.lastData.longitude;
		}

		//Check if gps is on
		while (Input.location.isEnabledByUser) {
			yield return new WaitForSeconds(1f);
		}

		//Restart function if gps is turned off
		yield return StartCoroutine(Start());
	}

	public float GetLat() {
		return Input.location.lastData.latitude;
	}

	public float GetLon() {
		return Input.location.lastData.longitude;
	}

	public float GetRoration() {
		return Input.compass.trueHeading;
	}

	public XmlDocument GetConfig() {

		lt = Input.location.lastData.latitude;
		ln = Input.location.lastData.longitude;
		string url = "http://asia.hiof.no/foxhunt-servlet/getConfig";

		xmlData = new XmlDocument();
		xmlData.Load(url);

		return xmlData;
	}


	//Getting info from server about the games flow.
	public XmlDocument GetState(int userID) {

		string url = "http://asia.hiof.no/foxhunt-servlet/getState";

		if (userID != 0) {
			lt = Input.location.lastData.latitude;
			ln = Input.location.lastData.longitude;
			url = "http://asia.hiof.no/foxhunt-servlet/getState?userid=" + userID + "&lat=" + lt + "&lon=" + ln;
		}

		XmlDocument xmlData = new XmlDocument();
		xmlData.Load(url);

		return xmlData;
	}


	private void OnApplicationQuit() {
		Input.location.Stop();
		Input.compass.enabled = false;
	}
}
