using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Xml;

public class GetData3 : MonoBehaviour {

    public GameObject spawn, foxReal, foxFake, girl, boy, obstacle, gameManager;
	public float lt, ln;

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

	public void GetConfig() {

		//string url = "http://asia.hiof.no/foxhunt-servlet/getConfig";

		lt = Input.location.lastData.latitude;
		ln = Input.location.lastData.longitude;
		string url = "http://asia.hiof.no/foxhunt-servlet/getConfig";
		//string url = "http://localhost:8080/getConfig?userid=" + gameManager.GetComponent<GameManager3>().userID + "&lat=" + lt + "&lon=" + ln;

		XmlDocument xmlData = new XmlDocument();
        xmlData.Load(url);



        //Getting map area
        XmlNodeList pointList = xmlData.GetElementsByTagName("point");
        float[,] boundary = new float[2, 4];
        int i = 0;
        foreach (XmlNode point in pointList) {
            boundary[0, i] = float.Parse(point.Attributes["lat"].Value);
            boundary[1, i] = float.Parse(point.Attributes["lon"].Value);
            i++;
        }


        //Getting game settings
        XmlNodeList settings = xmlData.GetElementsByTagName("display");

        foreach (XmlNode setting in settings) {

            double catchrange = double.Parse(setting.Attributes["catchrange"].Value);
            bool gps = bool.Parse(setting.Attributes["gps"].Value);
            bool opponents = bool.Parse(setting.Attributes["opponents"].Value);
            bool points = bool.Parse(setting.Attributes["points"].Value);

            gameManager.GetComponent<GameManager3>().SetSettings(boundary, catchrange, gps, opponents, points);
        }


        //Creating gameobjects
        XmlNodeList gameObjects = xmlData.GetElementsByTagName("gameObject");

        GameObject go = gameManager;

        foreach (XmlNode gameObject in gameObjects) {


			int id = int.Parse(gameObject.Attributes["id"].Value);

			switch (gameObject.Attributes["class"].Value) {
                case "Fox":
					if (PlayerPrefs.GetString("fox") == "FoxReal") {
						go = Instantiate(foxReal, spawn.transform.position, spawn.transform.rotation);
					}
					else if (PlayerPrefs.GetString("fox") == "FoxFake") {
						go = Instantiate(foxFake, spawn.transform.position, spawn.transform.rotation);
					}
                    
                    break;
                case "Hunter":
					if(PlayerPrefs.GetString("avatar", "") == "Girl") {
						go = Instantiate(boy, spawn.transform.position, spawn.transform.rotation);
					}
					else if(PlayerPrefs.GetString("avatar", "") == "Boy") {
						go = Instantiate(girl, spawn.transform.position, spawn.transform.rotation);
					}
                    
                    break;
                case "Obstacle":
                    go = Instantiate(obstacle, spawn.transform.position, spawn.transform.rotation);
                    break;
            }

            
            gameManager.GetComponent<GameManager3>().gameObjects.Add(id, go);
        }

        getGameObjects(gameObjects);
    }

	//Starting update to/from server each 0.2 sec
	public void StartUpdate() {
		InvokeRepeating("GetState", 0f, 0.1f);
	}

	//Getting info from server about the games flow.
    public void GetState() {

		string url = "http://asia.hiof.no/foxhunt-servlet/getState";

		if (gameManager.GetComponent<GameManager3>().userID != 0) {

			lt = Input.location.lastData.latitude;
			ln = Input.location.lastData.longitude;

			url = "http://asia.hiof.no/foxhunt-servlet/getState?userid=" + gameManager.GetComponent<GameManager3>().userID + "&lat=" + lt + "&lon=" + ln;
		}
		
		//string url = "http://localhost:8080/getState?userid=" + gameManager.GetComponent<GameManager3>().userID + "&lat=" + lt + "&lon=" + ln;
		XmlDocument xmlData = new XmlDocument();
        xmlData.Load(url);


        //Checks if the game is done
        XmlNodeList messages = xmlData.GetElementsByTagName("msg");

        foreach (XmlNode msg in messages) {
            bool gameOver = bool.Parse(msg.Attributes["gameOver"].Value);
            gameManager.GetComponent<GameManager3>().SetGameOver(gameOver);
        }

        XmlNodeList gameObjects = xmlData.GetElementsByTagName("gameObject");

		//Updateing each gameobject
        getGameObjects(gameObjects);

		try {
			gameManager.GetComponent<GameManager3>().UpdateButtons();
		}
		catch {

		}
		
	}



	//Updating gameobjects
    public void getGameObjects(XmlNodeList gameObjects) {

		//Getting value from XMLNode and set the value in GOScript
		foreach (XmlNode gameObject in gameObjects) {

			float ln = float.Parse(gameObject.Attributes["ln"].Value);
			float lt = float.Parse(gameObject.Attributes["lt"].Value);
			int id = int.Parse(gameObject.Attributes["id"].Value);
			bool taken = bool.Parse(gameObject.Attributes["taken"].Value);
			string name = "";
			int score = 0;
			bool isCaught = false;

			try {
				isCaught = bool.Parse(gameObject.Attributes["iscaught"].Value);
			}
			catch {

			}
			try {
				score = int.Parse(gameObject.Attributes["caught"].Value);
			}
			catch {

			}

			try {
				name = gameObject.Attributes["name"].Value;
			}
			catch {

			}
			
            GameObject tempGO;

            gameManager.GetComponent<GameManager3>().gameObjects.TryGetValue(id, out tempGO);

            tempGO.GetComponent<GOScript3>().SetValues(lt, ln, id, score, name, taken, isCaught);
        }

		gameManager.GetComponent<GameManager3>().checkScore();

    }


	private void OnApplicationQuit() {
		Input.location.Stop();
		Input.compass.enabled = false;
	}
}