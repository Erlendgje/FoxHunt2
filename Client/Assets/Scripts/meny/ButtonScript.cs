using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonScript : MonoBehaviour {

	//når spilleren kolliderer med collidern blir han/hun sendt til neste bane, som er Olympus
	public void StartGame(){
		Camera.main.GetComponent<AudioSource>().Play();
		Application.LoadLevel(2);

	}

	public void Reset(){
		Camera.main.GetComponent<AudioSource>().Play();
		PlayerPrefs.DeleteAll ();
		Application.Quit();
	}

}
