using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.UI;
using System.Xml;
using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

public class GameManager : MonoBehaviour {

	//Variables used for game calculations
	
	private int position = 0;

	public float scale;

	//Game settings from server
	private double catchrange;
	private bool gps, opponents, points, gameOver;

	//Objects
	private ServerHandler serverHandler;
	private GameObject tile;
	private List<GameObject> buttons;
	private Dictionary<int, GameObject> gameObjects;
	private Dictionary<int, TextMesh> scores;

	public List<GameObject> enviromentPrefabs;
	public GameObject button;
	public GameObject canvas;
	public GameObject girl, boy, foxReal, foxFake, obstacle;
	public GameObject grassPrefab;
	public Material grass;
	public Text scoreText;
	public Text nextScore;

	

	// Use this for initialization
	void Start() {
		gameObjects = new Dictionary<int, GameObject>();
		scores = new Dictionary<int, TextMesh>();
		serverHandler = this.GetComponent<ServerHandler>();

		//Getting server settings and starting constant update
		InitiateMap();
		createButtons();
		InvokeRepeating("UpdateGameObjects", 0, 0.2f);


		//createButtons();
	}

	private void Update() {
		if(Hunter.userID != 0) {
			Camera.main.transform.position = gameObjects[Hunter.userID].transform.position + new Vector3(-10, 25, 0);
		}
	}

	private void InitiateMap() {

		XmlDocument xmlData = serverHandler.GetConfig();

		//Getting map area
		XmlNodeList pointList = xmlData.GetElementsByTagName("point");

		CreateVector3.SetBoundary(pointList);

		//Creating map/tile
		tile = GameObject.CreatePrimitive(PrimitiveType.Plane);
		tile.GetComponent<MeshRenderer>().material = grass;
		tile.transform.localScale = new Vector3(Vector3.one.x + scale * CreateVector3.coordinateMapWidth, Vector3.one.y, Vector3.one.z + scale * CreateVector3.coordinateMapHeight);
		CreateVector3.inGameMapHeight = tile.GetComponent<Renderer>().bounds.size.x;
		CreateVector3.inGameMapWidth = tile.GetComponent<Renderer>().bounds.size.z;
		tile.transform.position = new Vector3(CreateVector3.inGameMapHeight / 2, 0, CreateVector3.inGameMapWidth / 2);
		//Increasing tile size so camera cant see outside the map
		tile.transform.localScale = new Vector3(tile.transform.localScale.x + 4, Vector3.one.y, tile.transform.localScale.z + 4);


		//Getting game settings
		XmlNodeList settings = xmlData.GetElementsByTagName("display");

		foreach (XmlNode setting in settings) {

			catchrange = double.Parse(setting.Attributes["catchrange"].Value);
			gps = bool.Parse(setting.Attributes["gps"].Value);
			opponents = bool.Parse(setting.Attributes["opponents"].Value);
			points = bool.Parse(setting.Attributes["points"].Value);
		}


		//Creating gameobjects
		XmlNodeList objectList = xmlData.GetElementsByTagName("gameObject");

		GameObject go = this.gameObject;

		foreach (XmlNode gObject in objectList) {


			int id = int.Parse(gObject.Attributes["id"].Value);
			float lon = float.Parse(gObject.Attributes["ln"].Value);
			float lat = float.Parse(gObject.Attributes["lt"].Value);

			switch (gObject.Attributes["class"].Value) {
				case "Fox":
					if (PlayerPrefs.GetString("fox") == "FoxReal") {
						go = Instantiate(foxReal, this.transform.position, this.transform.rotation);
					}
					else if (PlayerPrefs.GetString("fox") == "FoxFake") {
						go = Instantiate(foxFake, this.transform.position, this.transform.rotation);
					}

					bool isCaught = false;

					try {
						isCaught = bool.Parse(gObject.Attributes["iscaught"].Value);
					}
					catch {
						Debug.Log("Reven er ikke fanget");
					}

					go.GetComponent<Fox>().SetFoxValues(lat, lon, id);

					break;
				case "Hunter":
					if (PlayerPrefs.GetString("avatar", "") == "Girl") {
						go = Instantiate(boy, this.transform.position, this.transform.rotation);
					}
					else if (PlayerPrefs.GetString("avatar", "") == "Boy") {
						go = Instantiate(girl, this.transform.position, this.transform.rotation);
					}

					String name = gObject.Attributes["name"].Value;
					go.GetComponent<Hunter>().SetPlayerValues(lat, lon, id, name);

					break;
				case "Obstacle":
					go = Instantiate(obstacle, this.transform.position, this.transform.rotation);

					go.GetComponent<GObject>().SetObjectValue(lat, lon, id);
					break;
			}

			gameObjects.Add(id, go);
		}

		CreateEnvironment();
	}

	public void CreateEnvironment() {

		Vector2[] polygonArray = new Vector2[CreateVector3.boundary.Length];

		for(int i = 0; i < CreateVector3.boundary.Length; i++) {
			Vector3 temp = CreateVector3.MakeVector(CreateVector3.boundary[i].x, CreateVector3.boundary[i].y);
			polygonArray[i] = new Vector2(temp.x, temp.z);
		}


		//Making a fence around the map
		for (int i = 0; i < CreateVector3.boundary.Length; i++) {

			Vector3 corner1;
			Vector3 corner2;
			float difference;
			float differenceX;
			float differenceY;


			//Getting 2 points that decide where the fence will be built
			corner1 = CreateVector3.MakeVector(CreateVector3.boundary[i].x, CreateVector3.boundary[i].y);

			if (i < CreateVector3.boundary.Length - 1) {
				corner2 = CreateVector3.MakeVector(CreateVector3.boundary[i + 1].x, CreateVector3.boundary[i + 1].y);
			}
			else {
				corner2 = CreateVector3.MakeVector(CreateVector3.boundary[0].x, CreateVector3.boundary[0].y);
			}

			//How long the fence should be and how far it should be between each object.
			difference = Vector3.Distance(corner1, corner2);
			differenceX = Math.Abs(corner1.x - corner2.x) / (difference / 3);
			differenceY = Math.Abs(corner1.z - corner2.z) / (difference / 3);

			//Making the fence
			for (int k = 0; k < difference / 3; k++) {

				float x = 0;
				float y = 0;

				if (corner1.x > corner2.x) {
					x = corner1.x - differenceX * k;
				}
				else {
					x = corner1.x + differenceX * k;
				}

				if (corner1.z > corner2.z) {
					y = corner1.z - differenceY * k;
				}
				else {
					y = corner1.z + differenceY * k;
				}

				int random = UnityEngine.Random.Range(0, enviromentPrefabs.Count);

				Instantiate(enviromentPrefabs[random], new Vector3(x, enviromentPrefabs[random].GetComponent<Renderer>().bounds.size.y / 2, y), Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));


				CreateForest(polygonArray);
			}
		}
	}

	//Creating object randomly outside map
	public void CreateForest(Vector2[] PolygonArray) {

		//Getting random position on the map
		float randomX = UnityEngine.Random.Range(tile.transform.position.x - tile.GetComponent<Renderer>().bounds.size.x / 2, tile.transform.position.x + tile.GetComponent<Renderer>().bounds.size.x / 2);
		float randomZ = UnityEngine.Random.Range(tile.transform.position.z - tile.GetComponent<Renderer>().bounds.size.z / 2, tile.transform.position.z + tile.GetComponent<Renderer>().bounds.size.z / 2);

		//Getting random prop
		int random = UnityEngine.Random.Range(0, enviromentPrefabs.Count);
		Vector3 randomPosition = new Vector3(randomX, enviromentPrefabs[random].GetComponent<Renderer>().bounds.size.y / 2, randomZ);

		//Checking if randomPosition is inside polygon/map
		if (!CreateVector3.ContainsPoint(PolygonArray, new Vector2(randomX, randomZ))) {
			Instantiate(enviromentPrefabs[random], randomPosition, Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));

			for (int i = 0; i < 3; i++) {
				float randomXGrass = UnityEngine.Random.Range(tile.transform.position.x - tile.GetComponent<Renderer>().bounds.size.x / 2, tile.transform.position.x + tile.GetComponent<Renderer>().bounds.size.x / 2);
				float randomZGrass = UnityEngine.Random.Range(tile.transform.position.z - tile.GetComponent<Renderer>().bounds.size.z / 2, tile.transform.position.z + tile.GetComponent<Renderer>().bounds.size.z / 2);
				randomPosition = new Vector3(randomXGrass, grassPrefab.GetComponent<Renderer>().bounds.size.y / 2, randomZGrass);
				Instantiate(grassPrefab, randomPosition, Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));
			}
		}
		else {
			CreateForest(PolygonArray);
		}

	}

	//Updating gameobjects
	public void UpdateGameObjects() {

		XmlDocument xmlData = serverHandler.GetState();

		XmlNodeList objectList = xmlData.GetElementsByTagName("gameObject");

		int countHunters = 0;

		//Getting value from XMLNode and set the value in GOScript
		foreach (XmlNode gObject in objectList) {

			float lon = float.Parse(gObject.Attributes["ln"].Value);
			float lat = float.Parse(gObject.Attributes["lt"].Value);
			int id = int.Parse(gObject.Attributes["id"].Value);
		
			if (gameObjects[id].tag == "Fox") {

				bool isCaught = false;

				try {
					isCaught = bool.Parse(gObject.Attributes["iscaught"].Value);
				}
				catch {
					Debug.Log("Reven er ikke fanget");
				}

				gameObjects[id].GetComponent<Fox>().UpdateFox(lat, lon, isCaught);
			}
			else if(gameObjects[id].tag == "Hunter") {
				bool taken = bool.Parse(gObject.Attributes["taken"].Value);
				int score = 0;

				try {
					score = int.Parse(gObject.Attributes["score"].Value);

					if(gameObjects[id].GetComponent<Hunter>().GetScore() != score) {
						UpdateScore(id, score, gameObjects[id].GetComponent<Hunter>().GetName());
						checkScore();
					}
				}
				catch {
					Debug.Log("Spilleren har ikke fanget noen rever enda");
				}

				if (Hunter.userID == 0 && gameObjects[id].GetComponent<Hunter>().GetTaken() != taken) {
					UpdateButtons(countHunters, taken);
				}

				gameObjects[id].GetComponent<Hunter>().UpdateHunter(lat, lon, taken, score);

				countHunters++;
			}
		}
	}


	public void UpdatePlayer() {
		gameObjects[Hunter.userID].GetComponent<Hunter>().UpdateUser(serverHandler.GetLat(), serverHandler.GetLon(), serverHandler.GetRoration());
	}


	void createButtons() {

		buttons = new List<GameObject>();

		//Checks how many buttons we need
		int counter = 1;
		int buttonCounter = 0;

		foreach (KeyValuePair<int, GameObject> go in gameObjects) {
			if (go.Value.tag == "Hunter") {
				buttonCounter++;
			}
		}


		//Creating the buttons
		foreach (KeyValuePair<int, GameObject> go in gameObjects) {
			if (go.Value.tag == "Hunter") {

				GameObject temp = Instantiate(button, canvas.transform.position, canvas.transform.rotation);
				temp.transform.SetParent(canvas.transform);
				temp.GetComponent<RectTransform>().sizeDelta = new Vector2(canvas.GetComponent<RectTransform>().rect.width - 10, canvas.GetComponent<RectTransform>().rect.height * 0.1f);
				temp.transform.localScale = new Vector3(1, 1, 1);
				temp.GetComponent<RectTransform>().localPosition = new Vector3(0, ((temp.GetComponent<RectTransform>().rect.height) * (buttonCounter / 2)) - ((temp.GetComponent<RectTransform>().rect.height + 5) * counter), 0);

				temp.GetComponentInChildren<Text>().text = go.Value.GetComponent<Hunter>().GetName();
				temp.GetComponentInChildren<Text>().fontSize = (int)(canvas.GetComponent<RectTransform>().rect.height * 0.1f);
				temp.GetComponent<Button>().onClick.AddListener(() => ButtonClick(go.Key));


				//Checks if player is available
				if (go.Value.GetComponent<Hunter>().GetTaken() == true) {
					temp.GetComponent<Button>().interactable = false;
					temp.GetComponentInChildren<Animator>().enabled = false;
				}

				buttons.Add(temp);

				counter++;
			}
		}
	}

	void ButtonClick(int id) {
		Hunter.userID = id;
		Destroy(gameObjects[id]);
		if (PlayerPrefs.GetString("avatar") == "Boy") {
			gameObjects[id] = Instantiate(boy, this.transform.position, this.transform.rotation);

		}
		else if (PlayerPrefs.GetString("avatar") == "Girl") {
			gameObjects[id] = Instantiate(girl, this.transform.position, this.transform.rotation);

		}

		canvas.GetComponent<AudioSource>().Play();

		foreach (GameObject go in buttons) {
			Destroy(go);
		}

		InvokeRepeating("UpdatePlayer", 0f, 0.05f);
	}

	public void UpdateButtons(int position, bool taken) {

			buttons[position].GetComponent<Button>().interactable = !taken;
			buttons[position].GetComponentInChildren<Animator>().enabled = !taken;
	}

	public void UpdateScore(int id, int score, string name) {
		TextMesh temp;
		if (id == Hunter.userID) {
			scoreText.text = "Score: " + score;
		}
		else {
			if (!scores.TryGetValue(id, out temp)) {
				scores.Add(id, gameObjects[id].GetComponentInChildren<TextMesh>());
			}
			scores[id].text = name + "\r\n" + "Score: " + score;
		}
	}

	//check your score, and finds the score to the player before you
	public void checkScore() {
		if (Hunter.userID != 0) {
			int nextScore = 0;
			bool first = true;
			position = 0;

			foreach (KeyValuePair<int, GameObject> go in gameObjects) {
				if (go.Value.tag == "Hunter") {

					if (gameObjects[Hunter.userID].GetComponent<Hunter>().GetScore() < go.Value.GetComponent<Hunter>().GetScore()) {
						if (first) {
							nextScore = go.Value.GetComponent<Hunter>().GetScore();
						}

						if (nextScore > go.Value.GetComponent<Hunter>().GetScore()) {
							nextScore = go.Value.GetComponent<Hunter>().GetScore();
						}

						position++;
					}
				}
			}

			switch (position) {
				case 0:
					this.nextScore.text = "1st place";
					break;
				case 1:
					this.nextScore.text = "1st place: " + nextScore;
					break;
				case 2:
					this.nextScore.text = "2nd place: " + nextScore;
					break;
				case 3:
					this.nextScore.text = "3rd place: " + nextScore;
					break;
				case 4:
					this.nextScore.text = "4th place: " + nextScore;
					break;
			}
		}
	}
}
