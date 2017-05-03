﻿using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonScript : MonoBehaviour {


	public GameObject yesNo;

	//når spilleren kolliderer med collidern blir han/hun sendt til neste bane, som er Olympus
	public void StartGame(){
		Camera.main.GetComponent<AudioSource>().Play();
		Application.LoadLevel(3);


	}

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
