using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class GOScript3 : MonoBehaviour {

	//Settings for the gameobject
    public decimal lt, ln;
    public int id;
	public string name;
    private float speed = 20f;
	private float rotationSpeed = 5f;
    public int score;
    public GameObject gameManager;
    private GameManager3 gmScript;
    private bool first;
	public bool taken;
	private bool isCaught = false;

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
    public void SetValues(decimal lt, decimal ln, int id, int score, string name, bool taken, bool isCaught) {

        this.id = id;
        this.score = score;
		this.taken = taken;

		if (isCaught && !this.isCaught) {
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}
		else if(!isCaught && this.isCaught) {
			this.GetComponent<Animator>().SetBool("caught", isCaught);
		}

		this.isCaught = isCaught;

		if (isCaught) {
			return;
		}

		if(this.name == "") {
			this.name = name;
		}

		if (this.CompareTag("Hunter") && first == false)
        {
			gmScript.UpdateScore(id, score, this.name);
        }

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

				float rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg + 90;

				if(rotation < 0) {
					rotation = 360 + rotation;
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, rotation, 0), rotationSpeed);
			}
			else if(this.tag == "Hunter") {
				float rotation = Mathf.Atan2(transform.position.x - temp.x, transform.position.z - temp.z) * Mathf.Rad2Deg + 180;

				if (rotation < 0) {
					rotation = 360 + rotation;
				}

				transform.rotation = Quaternion.RotateTowards(transform.rotation, Quaternion.Euler(0, rotation, 0), rotationSpeed);
			}


			transform.position = Vector3.MoveTowards(transform.position, temp, speed * Time.deltaTime);
			
		}
        else {
			foreach (Renderer renderer in this.GetComponentsInChildren<Renderer>()) {
				renderer.enabled = false;
			}
			first = true;
        }
    }

    
}