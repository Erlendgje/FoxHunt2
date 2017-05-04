using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.UI;
using System.Xml;
using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

public class GameManager : MonoBehaviour {
	
	//Score position, example first place
	private int position = 0;
	public float scale;
	private float fenceDistance = 3;

	//Game settings from server
	private double catchrange;
	private bool gps, opponents, points, gameOver, first;

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
		first = true;
		gameObjects = new Dictionary<int, GameObject>();
		scores = new Dictionary<int, TextMesh>();
		serverHandler = this.GetComponent<ServerHandler>();

		//Getting game settings and creates a environment
		InitiateMap();
		//Creating buttons to choose hunter
		createButtons();
		//Updating information from server every 0.1 seconds
		InvokeRepeating("UpdateGameObjects", 0, 0.1f);


		//createButtons();
	}

	//Moves camera after player
	private void Update() {
		if(Hunter.userID != 0) {
			Camera.main.transform.position = gameObjects[Hunter.userID].transform.position + new Vector3(-10, 25, 0);
		}
	}

	//Getting game settings and creates environment
	private void InitiateMap() {

		//Getting serverdata
		XmlDocument xmlData = serverHandler.GetConfig();

		//Getting map area
		XmlNodeList pointList = xmlData.GetElementsByTagName("point");

		CreateVector3.SetBoundary(pointList);

		//Creating map/tile
		tile = GameObject.CreatePrimitive(PrimitiveType.Plane);
		tile.GetComponent<MeshRenderer>().material = grass;
		tile.transform.localScale = new Vector3(Vector3.one.x + scale * CreateVector3.coordinateMapWidth, Vector3.one.y, Vector3.one.z + scale * CreateVector3.coordinateMapHeight);
		//Getting height and widt of tile, this value is used to calculate position later
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

			//Checks what class to instantiate
			switch (gObject.Attributes["class"].Value) {
				case "Fox":
					//Check what prefab the player picked
					if (PlayerPrefs.GetString("fox") == "FoxReal") {
						go = Instantiate(foxReal, this.transform.position, this.transform.rotation);
					}
					else if (PlayerPrefs.GetString("fox") == "FoxFake") {
						go = Instantiate(foxFake, this.transform.position, this.transform.rotation);
					}

					bool isCaught = false;

					//Need a try/catch, iscaught is not sent if it equals false
					try {
						isCaught = bool.Parse(gObject.Attributes["iscaught"].Value);
					}
					catch {
						Debug.Log("Reven er ikke fanget");
					}

					go.GetComponent<Fox>().SetFoxValues(lat, lon, id);

					break;
				case "Hunter":
					//Checks what avatar the player picked
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
			//Storing each object by id
			gameObjects.Add(id, go);
		}

		CreateEnvironment();
	}

	//Creating enviroment(Trees, grass, stones etc)
	public void CreateEnvironment() {

		//Creates a polygonArray that contains x and z values for each corner of the map in unity, this is needed to create environment outside the map area
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

			//Corner 2 will be boundary[0] if for loop is at final iteration to create a square.
			if (i < CreateVector3.boundary.Length - 1) {
				corner2 = CreateVector3.MakeVector(CreateVector3.boundary[i + 1].x, CreateVector3.boundary[i + 1].y);
			}
			else {
				corner2 = CreateVector3.MakeVector(CreateVector3.boundary[0].x, CreateVector3.boundary[0].y);
			}

			//How long the fence should be and how far it should be between each object.
			difference = Vector3.Distance(corner1, corner2);
			differenceX = Math.Abs(corner1.x - corner2.x) / (difference / fenceDistance);
			differenceY = Math.Abs(corner1.z - corner2.z) / (difference / fenceDistance);

			//Making the fence one at a time, moving differenceX in x-axis and differenceY in y-axis for each tree
			for (int k = 0; k < difference / fenceDistance; k++) {

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
				//Getting random prefab
				int random = UnityEngine.Random.Range(0, enviromentPrefabs.Count);

				Instantiate(enviromentPrefabs[random], new Vector3(x, enviromentPrefabs[random].GetComponent<Renderer>().bounds.size.y / 2, y), Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));

				//Creating one extra 
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
			//Creating three grass for each prefab that is created
			for (int i = 0; i < 3; i++) {
				float randomXGrass = UnityEngine.Random.Range(tile.transform.position.x - tile.GetComponent<Renderer>().bounds.size.x / 2, tile.transform.position.x + tile.GetComponent<Renderer>().bounds.size.x / 2);
				float randomZGrass = UnityEngine.Random.Range(tile.transform.position.z - tile.GetComponent<Renderer>().bounds.size.z / 2, tile.transform.position.z + tile.GetComponent<Renderer>().bounds.size.z / 2);
				randomPosition = new Vector3(randomXGrass, grassPrefab.GetComponent<Renderer>().bounds.size.y / 2, randomZGrass);
				Instantiate(grassPrefab, randomPosition, Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));
			}
		}
		//If random position is inside map, try again
		else {
			CreateForest(PolygonArray);
		}

	}

	//Getting data from server
	public void UpdateGameObjects() {
		
		if (first && Hunter.userID != 0) {
			UpdateScore(Hunter.userID, 0, gameObjects[Hunter.userID].GetComponent<Hunter>().GetName());
			checkScore();
			first = false;
		}
		XmlDocument xmlData = serverHandler.GetState();

		XmlNodeList objectList = xmlData.GetElementsByTagName("gameObject");

		//Variable that find what position the current hunter is in the list of hunters
		int countHunters = 0;

		//Getting value from XMLNode and set the value in GOScript
		foreach (XmlNode gObject in objectList) {

			float lon = float.Parse(gObject.Attributes["ln"].Value);
			float lat = float.Parse(gObject.Attributes["lt"].Value);
			int id = int.Parse(gObject.Attributes["id"].Value);
			//Only foxes can have isCaught
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
			//Only hunters can have a score
			else if(gameObjects[id].tag == "Hunter") {
				bool taken = bool.Parse(gObject.Attributes["taken"].Value);
				int score = 0;

				try {

					score = int.Parse(gObject.Attributes["score"].Value);

					//Only updating score if something have changes
					if(gameObjects[id].GetComponent<Hunter>().GetScore() != score) {
						UpdateScore(id, score, gameObjects[id].GetComponent<Hunter>().GetName());
						checkScore();
					}
				}
				catch {
					Debug.Log("Spilleren har ikke fanget noen rever enda");
				}

				//Updating buttons if user have failed to pick a hunter yet
				if (Hunter.userID == 0 && gameObjects[id].GetComponent<Hunter>().GetTaken() != taken) {
					UpdateButtons(countHunters, taken);
				}

				gameObjects[id].GetComponent<Hunter>().UpdateHunter(lat, lon, taken, score);

				countHunters++;
			}
		}
	}

	//Updating players position
	public void UpdatePlayer() {
		gameObjects[Hunter.userID].GetComponent<Hunter>().UpdateUser(serverHandler.GetLat(), serverHandler.GetLon(), serverHandler.GetRoration());
	}



	//Creates one button for each hunter
	void createButtons() {

		buttons = new List<GameObject>();

		//Checks how many buttons we need to position them correctly
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


				//Checks if player is available and disables the buttons if not
				if (go.Value.GetComponent<Hunter>().GetTaken() == true) {
					temp.GetComponent<Button>().interactable = false;
					temp.GetComponentInChildren<Animator>().enabled = false;
				}

				buttons.Add(temp);

				counter++;
			}
		}
	}
	//This is what happens when a button is clicked
	void ButtonClick(int id) {
		//Setting players id and replacing the hunter with the same id with the prefab the user picked
		Hunter.userID = id;
		Destroy(gameObjects[id]);
		if (PlayerPrefs.GetString("avatar") == "Boy") {
			gameObjects[id] = Instantiate(boy, this.transform.position, this.transform.rotation);
			gameObjects[id].GetComponent<Hunter>().SetPosition(serverHandler.GetLat(), serverHandler.GetLon());

		}
		else if (PlayerPrefs.GetString("avatar") == "Girl") {
			gameObjects[id] = Instantiate(girl, this.transform.position, this.transform.rotation);

		}
		//Playing start sound
		canvas.GetComponent<AudioSource>().Play();
		//Destroying all buttons
		foreach (GameObject go in buttons) {
			Destroy(go);
		}
		//Starting to update player position
		InvokeRepeating("UpdatePlayer", 0f, 0.05f);
	}

	//Updating buttons if a hunter gets taken in menu
	public void UpdateButtons(int position, bool taken) {

			buttons[position].GetComponent<Button>().interactable = !taken;
			buttons[position].GetComponentInChildren<Animator>().enabled = !taken;
	}

	//Updating hunters score
	public void UpdateScore(int id, int score, string name) {
		TextMesh temp;

		//Users score will update text at top of screen and not over hunter
		if (id == Hunter.userID) {
			scoreText.text = "Score: " + score;
		}
		else {
			//Updating score above player in-game
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

			//Checking each hunters score
			foreach (KeyValuePair<int, GameObject> go in gameObjects) {
				if (go.Value.tag == "Hunter") {
					//Checks if users score is lower then hunters score
					if (gameObjects[Hunter.userID].GetComponent<Hunter>().GetScore() < go.Value.GetComponent<Hunter>().GetScore()) {
						if (first) {
							nextScore = go.Value.GetComponent<Hunter>().GetScore();
						}
						//Checks if current found score is bigger then hunter score
						if (nextScore > go.Value.GetComponent<Hunter>().GetScore()) {
							nextScore = go.Value.GetComponent<Hunter>().GetScore();
						}

						position++;
					}
				}
			}

			//Sets text after position is found
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
