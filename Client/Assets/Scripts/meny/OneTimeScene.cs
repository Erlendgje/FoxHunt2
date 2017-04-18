using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class OneTimeScene : MonoBehaviour {

	private int levelReached;
	public bool test;

	void LoadScene(){
		//Allows time for text to be read 


		//Checks PlayerPrefs number 
		levelReached = PlayerPrefs.GetInt("SavedLevel");

		//If playerPrefs is 0 or above, goes from main menu to first game level 
		if (levelReached >= 0 ){
			Application.LoadLevel(1);
		}

		else if (levelReached <= 0){ // moves to prologue 
			Application.LoadLevel(0);
		}
	
	}

	void Start(){
		if (test == true) {
			PlayerPrefs.SetInt ("SavedLevel", 0);
		 
		} 

		else if (test == !true) {
			LoadScene ();
		}
			
	
	}
}
