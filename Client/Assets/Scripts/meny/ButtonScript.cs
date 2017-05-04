using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonScript : MonoBehaviour {


	public GameObject yesNo;

	//The player are sent to the next scene
	//The sound on the button is played of when pushed
	public void StartGame(){
		Camera.main.GetComponent<AudioSource>().Play();
		Application.LoadLevel(3);


	}
	//All the playerprefs in the game get deleleted
	//The sound on the button is played of when pushed
	public void Reset(){
		Camera.main.GetComponent<AudioSource>().Play();
		PlayerPrefs.DeleteAll ();
		Application.Quit();
	}


	public void GoHome() {

		if(Hunter.userID == 0) {
			Application.LoadLevel(2);
		}
		else {
			yesNo.SetActive(true);
		}
			


	}

	public void Yes() {
		Hunter.userID = 0;
		Application.LoadLevel(2);
	}

	public void No() {

		yesNo.SetActive(false);

	}

}
