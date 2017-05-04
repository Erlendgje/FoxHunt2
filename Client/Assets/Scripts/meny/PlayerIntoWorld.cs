using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerIntoWorld : MonoBehaviour {


	private Text tekst;
	public GameObject position;
	public RuntimeAnimatorController GirlAni;
	public RuntimeAnimatorController BoyAni;



	void Awake ()
	{	//Henter inn hvem karakter som ble valgt fra PickPlayerScriptet
		DontDestroyOnLoad (this);
		//Klonen blir generert på satt posisjon
		//Instantiates the avatar that was picked in PickPlayerScript, on the positions(Gameobjects) position. And scales it to the right size for that scene 
		GameObject go = (GameObject)Instantiate(Resources.Load(PlayerPrefs.GetString ("avatar", "")), position.transform.position, Quaternion.Euler(0f, 0f, 0f));
		go.transform.localScale += new Vector3 (200f, 200f, 200f);

		//If the avatar is named Girl, the avatar will have rotation 180, and the animation placed in the public RuntimeAnimatorController GirlAni; will play
		if(PlayerPrefs.GetString ("avatar", "") == "Girl"){

			go.AddComponent <Animator>();
			go.GetComponent<Animator> ().runtimeAnimatorController = GirlAni;
			go.transform.rotation = Quaternion.Euler (0f, 180f, 0f);



		}

		//If the avatar is named Boy, the avatar will have rotation 90, and the animation placed in the public RuntimeAnimatorController BoyAni; will play
		else if(PlayerPrefs.GetString ("avatar", "") == "Boy"){

			go.GetComponent<Animator> ().runtimeAnimatorController = BoyAni;
			go.transform.rotation = Quaternion.Euler (0f, 90f, 0f);



		}
		//PlayerPrefs.GetString ("avatar", PickPlayerScript.cName);


	}







	

}
