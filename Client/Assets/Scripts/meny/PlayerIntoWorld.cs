using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerIntoWorld : MonoBehaviour {


	private Text tekst;
	public GameObject position;
	public RuntimeAnimatorController GirlAni;
	public RuntimeAnimatorController BoyAni;


	//Fikk hjelp av Jonas med Resources funksjonen
	void Awake ()
	{	//Henter inn hvem karakter som ble valgt fra PickPlayerScriptet
		DontDestroyOnLoad (this);
		Debug.Log (PlayerPrefs.GetString ("avatar"));
		//Klonen blir generert på satt posisjon
		//Instantiate(Resources.Load(PickPlayerScript.cName), new Vector3(3f, 23f, 86f), Quaternion.Euler(0f, 90f, 0f));
		GameObject go = (GameObject)Instantiate(Resources.Load(PlayerPrefs.GetString ("avatar", "")), position.transform.position, Quaternion.Euler(0f, 0f, 0f));
		go.transform.localScale += new Vector3 (200f, 200f, 200f);

		if(PlayerPrefs.GetString ("avatar", "") == "Girl"){

			go.AddComponent <Animator>();
			go.GetComponent<Animator> ().runtimeAnimatorController = GirlAni;
			go.transform.rotation = Quaternion.Euler (0f, 180f, 0f);



		}

		else if(PlayerPrefs.GetString ("avatar", "") == "Boy"){

			go.AddComponent <Animator>();
			go.GetComponent<Animator> ().runtimeAnimatorController = BoyAni;
			go.transform.rotation = Quaternion.Euler (0f, 90f, 0f);



		}
		//PlayerPrefs.GetString ("avatar", PickPlayerScript.cName);


	}







	

}
