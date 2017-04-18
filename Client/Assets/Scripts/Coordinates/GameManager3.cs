using System.Collections;
using System.Collections.Generic;
using System;
using UnityEngine;
using UnityEngine.UI;
using System.Runtime.Serialization.Formatters.Binary;
using System.IO;

public class GameManager3 : MonoBehaviour {

	//Variables used for game calculations
    public decimal[,] boundary;
    public decimal southernmosttPoint;
    public decimal northernmostPoint;
    public decimal westernmostPoint;
    public decimal easternmostPoint;
    public decimal coordinateMapHeight;
    public decimal coordinateMapWidth;
    public float inGameMapHeight;
    public float inGameMapWidth;
    public float scale;
	public int userID;
	public int position;

	//Game settings from server
    public double catchrange;
    public bool gps;
    public bool opponents;
    public bool points;
    public bool gameOver;

    

    //Objects
    public GameObject serverHandler;
    public GameObject tile;
	public GameObject cam;
	public GameObject button;
	public GameObject canvas;
	public List<GameObject> buttons;
	public List<GameObject> prefabs;
	public Material grass;
	public Text scoreText;
	public Text nextScore;

	public Dictionary<int, GameObject> gameObjects;
    public Dictionary<int, TextMesh> scores;

    // Use this for initialization
    void Start() {
		position = 0;
        gameObjects = new Dictionary<int, GameObject>();
        scores = new Dictionary<int, TextMesh>();
        //Getting server settings and starting constant update
        serverHandler.GetComponent<GetData3>().getConfig();
        serverHandler.GetComponent<GetData3>().startUpdate();

		createButtons();
    }

	//Creates buttons from how many players available
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
			if(go.Value.tag == "Hunter") {

				GameObject temp = Instantiate(button, canvas.transform.position, canvas.transform.rotation);
				temp.transform.SetParent(canvas.transform);
				temp.GetComponent<RectTransform>().sizeDelta = new Vector2(canvas.GetComponent<RectTransform>().rect.width - 10, canvas.GetComponent<RectTransform>().rect.height * 0.1f);
				temp.transform.localScale = new Vector3(1, 1, 1);
				temp.GetComponent<RectTransform>().localPosition = new Vector3(0, ((temp.GetComponent<RectTransform>().rect.height) * (buttonCounter / 2)) - ((temp.GetComponent<RectTransform>().rect.height + 5) * counter), 0);

				temp.GetComponentInChildren<Text>().text = go.Value.GetComponent<GOScript3>().name;
				temp.GetComponentInChildren<Text>().fontSize = (int) (canvas.GetComponent<RectTransform>().rect.height * 0.1f) + 10;
				temp.GetComponent<Button>().onClick.AddListener(() => ButtonClick(go.Key));


				//Checks if player is available
				if (go.Value.GetComponent<GOScript3>().taken == true) {
					temp.GetComponent<Button>().interactable = false;
					temp.GetComponentInChildren<Animation>().enabled = false;
					temp.GetComponentInChildren<Animator>().enabled = false;
				}

				buttons.Add(temp);

				counter++;
			}
		}
	}

	void ButtonClick(int id) {
		this.userID = id;

		foreach (GameObject go in buttons) {
			Destroy(go);
		}
	}

	//Checks the players available
	public void UpdateButtons() {

		int counter = 0;

		foreach(KeyValuePair<int, GameObject> go in gameObjects) {
			if(go.Value.tag == "Hunter") {
				if (go.Value.GetComponent<GOScript3>().taken) {
					buttons[counter].GetComponent<Button>().interactable = false;
					buttons[counter].GetComponentInChildren<Animation>().enabled = false;
					buttons[counter].GetComponentInChildren<Animator>().enabled = false;
				}
				counter++;
			}
		}
	}

    // Update is called once per frame
    void Update() {
		//moving camera after player
		try {
			cam.transform.position = gameObjects[userID].transform.position + new Vector3(-10, 15, 0);
		}
		catch {

		}		
    }

	//Update the score of a player
    public void UpdateScore(int id, int score, string name)
    {
        TextMesh temp;
        if (id == userID)
        {
            scoreText.text = "Score: " + score;
        }
        else
        {
            if (!scores.TryGetValue(id, out temp))
            {
				scores.Add(id, gameObjects[id].GetComponentInChildren<TextMesh>());
			}
			scores[id].text = name + "\r\n" + "Score: " + score;
		}
    }

    public void SetSettings(decimal[,] boundary, double catchrange, bool gps, bool opponents, bool points) {
        this.boundary = boundary;
        this.catchrange = catchrange;
        this.gps = gps;
        this.opponents = opponents;
        this.points = points;

		//Getting highest and lowest value from the maps coordinates
        for (int i = 0; i < boundary.Length/2; i++) {

            if (i == 0) {
                southernmosttPoint = boundary[0, i];
                northernmostPoint = boundary[0, i];
                westernmostPoint = boundary[1, i];
                easternmostPoint = boundary[1, i];
            }

            if (southernmosttPoint > boundary[0, i]) {
                southernmosttPoint = boundary[0, i];
            }

            if (northernmostPoint < boundary[0, i]) {
                northernmostPoint = boundary[0, i];
            }

            if (westernmostPoint > boundary[1, i]) {
                westernmostPoint = boundary[1, i];
            }

            if (easternmostPoint < boundary[1, i]) {
                easternmostPoint = boundary[1, i];
            }
        }

		//getting height of map in coordinates
        coordinateMapHeight = northernmostPoint - southernmosttPoint;
        coordinateMapWidth = easternmostPoint - westernmostPoint;

		//Creating map/tile
        tile = GameObject.CreatePrimitive(PrimitiveType.Plane);
		tile.GetComponent<MeshRenderer>().material = grass;
        tile.transform.localScale = new Vector3(Vector3.one.x + scale * (float)coordinateMapWidth, Vector3.one.y, Vector3.one.z + scale * (float)coordinateMapHeight);
        inGameMapHeight = tile.GetComponent<Renderer>().bounds.size.x;
        inGameMapWidth = tile.GetComponent<Renderer>().bounds.size.z;
        tile.transform.position = new Vector3(inGameMapHeight/2, 0, inGameMapWidth/2);
		//Increasing tile size so camera cant see outside the map
		tile.transform.localScale = new Vector3(tile.transform.localScale.x + 2, Vector3.one.y, tile.transform.localScale.z + 2);

		Vector2[] polygonArray = new Vector2[boundary.Length / 2];

		for (int i = 0; i < boundary.Length / 2; i++) {

			Vector3 temp = MakeVector((float)boundary[1, i], (float)boundary[0, i]);
			polygonArray[i] = new Vector2(temp.x, temp.z);
		}

			//Making a fence around the map
			for (int i = 0; i < boundary.Length/2; i++) {

			Vector3 corner1;
			Vector3 corner2;
			float difference;
			float differenceX;
			float differenceY;


			//Getting 2 points that decide where the fence will be built
			corner1 = MakeVector((float)boundary[1, i], (float)boundary[0, i]);

			if (i < boundary.Length/2 - 1) {
				corner2 = MakeVector((float)boundary[1, i + 1], (float)boundary[0, i + 1]);
			}
			else {
				corner2 = MakeVector((float)boundary[1, 0], (float)boundary[0, 0]);
			}

			//How long the fence should be and how far it should be between each object.
			difference = Vector3.Distance(corner1, corner2);
			differenceX = Math.Abs(corner1.x - corner2.x) / (difference / 3);
			differenceY = Math.Abs(corner1.z - corner2.z) / (difference / 3);

			//Making the fence
			for (int k = 0; k < difference/3; k++) {

				float x = 0;
				float y = 0;

				if (corner1.x > corner2.x) {
					x = corner1.x - differenceX * k + (UnityEngine.Random.Range(-0.2f, 0.2f));
				}else {
					x = corner1.x + differenceX * k + (UnityEngine.Random.Range(-0.2f, 0.2f));
				}

				if(corner1.z > corner2.z) {
					y = corner1.z - differenceY * k + (UnityEngine.Random.Range(-0.5f, 0.5f));
				}else {
					y = corner1.z + differenceY * k + (UnityEngine.Random.Range(-0.5f, 0.5f));
				}

				int random = UnityEngine.Random.Range(0, prefabs.Count);



				Instantiate(prefabs[random], new Vector3(x, prefabs[random].GetComponent<Renderer>().bounds.size.y / 2, y), Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360),0)));

				
				CreateForest(polygonArray);
			}
		}
	}
	//Creating object randomly outside map
	public void CreateForest(Vector2[] PolygonArray) {

		//Getting random position on the map
		float randomX = UnityEngine.Random.Range (tile.transform.position.x - tile.GetComponent<Renderer>().bounds.size.x / 2, tile.transform.position.x + tile.GetComponent<Renderer>().bounds.size.x / 2);
		float randomZ = UnityEngine.Random.Range (tile.transform.position.z - tile.GetComponent<Renderer>().bounds.size.z / 2, tile.transform.position.z + tile.GetComponent<Renderer>().bounds.size.z / 2);

		//Getting random prop
		int random = UnityEngine.Random.Range(0, prefabs.Count);
		Vector3 randomPosition = new Vector3 (randomX, prefabs[random].GetComponent<Renderer>().bounds.size.y / 2, randomZ);

		//Checking if randomPosition is inside polygon/map
		if (!ContainsPoint(PolygonArray, new Vector2(randomX, randomZ))) {
			Instantiate(prefabs[random], randomPosition, Quaternion.Euler(new Vector3(0, UnityEngine.Random.Range(0, 360), 0)));
		}
		else {
			CreateForest(PolygonArray);
		}
		
	}

	//Function that checks if polygon contains point
	public bool ContainsPoint(Vector2[] polyPoints, Vector2 p) {
		int j = polyPoints.Length - 1;
		bool inside = false;

		for (var i = 0; i < polyPoints.Length; j = i++) {
			if (((polyPoints[i].y <= p.y && p.y < polyPoints[j].y) || (polyPoints[j].y <= p.y && p.y < polyPoints[i].y)) &&
			   (p.x < (polyPoints[j].x - polyPoints[i].x) * (p.y - polyPoints[i].y) / (polyPoints[j].y - polyPoints[i].y) + polyPoints[i].x)) {
				inside = !inside;
			}
		}
		return inside;
	}


	public void SetGameOver(bool gameOver) {

        this.gameOver = gameOver;

    }

	//Konverting irl coordinates to Vector3
	public Vector3 MakeVector(float ln, float lt) {
		float z = ((float)easternmostPoint - ln) * (inGameMapWidth / (float)coordinateMapWidth);
		float x = (lt - (float)southernmosttPoint) * (inGameMapHeight / (float)coordinateMapHeight);
		return new Vector3(x, 0, z);
	}

	//check your score, and finds the score to the player before you
	public void checkScore() {
		if(userID != 0) {
			int nextScore = 0;
			position = 0;

			foreach (KeyValuePair<int, GameObject> go in gameObjects) {
				if(go.Value.tag == "Hunter") {

					if (gameObjects[userID].GetComponent<GOScript3>().score < go.Value.GetComponent<GOScript3>().score) {
						if(nextScore < go.Value.GetComponent<GOScript3>().score) {
							nextScore = go.Value.GetComponent<GOScript3>().score;
						}
					}

					if (go.Value.GetComponent<GOScript3>().score > gameObjects[userID].GetComponent<GOScript3>().score) {
						position++;
					}
				}
			}

			switch (position) {
				case 0:
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
				case 5:
					this.nextScore.text = "5th place: " + nextScore;
					break;
			}
		}
	}
}