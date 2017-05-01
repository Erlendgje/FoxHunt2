using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class GOScript3 : MonoBehaviour {

	//Settings for the gameobject
    public float lt, ln;
    public int id;
	public string name;
    private float speed;
	private float rotationSpeed = 5f;
    public int score = 0;
    public GameObject gameManager;
    private GameManager3 gmScript;
    private bool first;
	public bool taken;
	private bool isCaught = false;
	private Vector3 lastPosition;

	//Finding GameManager
    private void Awake() {
        gameManager = GameObject.FindGameObjectWithTag("GameManager");
        gmScript = gameManager.GetComponent<GameManager3>();
        first = true;
		taken = false;
    }

    // Use this for initialization
    void Start() {
    }

    // Update is called once per frame
    void Update() {
	}

	

	//Set values and moving the gameobject
    public void SetValues(float lt, float ln, int id, int score, string name, bool taken, bool isCaught) {

        this.id = id;
		if(this.score != score && id == gameManager.GetComponent<GameManager3>().userID) {
			this.GetComponent<AudioSource>().Play();
		}

        this.score = score;
		this.taken = taken;

		if (isCaught && !this.isCaught) {
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}
		else if(!isCaught && this.isCaught) {
			this.transform.position = gmScript.MakeVector((float)ln, (float)lt);
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}

		this.isCaught = isCaught;

		if (isCaught) {
			return;
		}

		if(this.name == "") {
			this.name = name;
		}

		if (this.CompareTag("Hunter"))
        {
			gmScript.UpdateScore(id, score, this.name);
        }

		if(this.id != gameManager.GetComponent<GameManager3>().userID) {
			MovePlayer(lt, ln, 0);
		}
	}

	public void MovePlayer(float lt, float ln, float rotation) {
		//Checking if object is located on the field irl
		if (lt < gmScript.northernmostPoint && lt > gmScript.southernmosttPoint && ln < gmScript.easternmostPoint && ln > gmScript.westernmostPoint) {

			this.lt = lt;
			this.ln = ln;

			Vector3 temp = gmScript.MakeVector((float)ln, (float)lt);
			//If its first time to enter field, teleport object to position
			if (first == true) {
				transform.position = gmScript.MakeVector((float)ln, (float)lt);
				first = false;
			}

			foreach (Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
				renderer.enabled = true;
			}

			//Rotating the foxes
			if (this.CompareTag("Fox")) {

				if (PlayerPrefs.GetString("fox") == "FoxReal") {
					rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg + 90;
				}
				else if (PlayerPrefs.GetString("fox") == "FoxFake") {
					rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg - 90;
				}


				if (rotation < 0) {
					rotation = 360 + rotation;
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, rotation, 0), rotationSpeed);
			}
			else if (this.tag == "Hunter") {

				if (this.id != gameManager.GetComponent<GameManager3>().userID) {
					if (PlayerPrefs.GetString("avatar") == "Boy") {
						rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg + 180;
					}
					else {
						rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg + 90;
					}

				}
				else {
					if(this.id == gameManager.GetComponent<GameManager3>().userID) {
						if (PlayerPrefs.GetString("avatar") == "Girl") {
							rotation += 90;
						}
					}
				}

				if (rotation < 0) {
					rotation = 360 + rotation;
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, rotation, 0), rotationSpeed);
			}

			if ((transform.position.x != temp.x || transform.position.z != temp.z)) {

				speed = Vector3.Distance(transform.position, temp) / 0.2f;

				transform.position = Vector3.MoveTowards(transform.position, temp, speed * Time.deltaTime);
				if (this.tag == "Hunter") {
					this.GetComponent<Animator>().SetBool("moving", true);
				}
			}
			else if (this.tag == "Hunter") {
				this.GetComponent<Animator>().SetBool("moving", false);
			}
		}
		else {
			foreach (Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
				renderer.enabled = false;
			}
			first = true;
		}
	}


}