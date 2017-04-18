using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerIntoWorld : MonoBehaviour {


	private Text tekst;


	//Fikk hjelp av Jonas med Resources funksjonen
	void Awake ()
	{	//Henter inn hvem karakter som ble valgt fra PickPlayerScriptet
		GetComponent<OneTimeScene>();
		GetComponent<PickPlayerScript>();
		DontDestroyOnLoad (this);
		//Klonen blir generert på satt posisjon
		//Instantiate(Resources.Load(PickPlayerScript.cName), new Vector3(3f, 23f, 86f), Quaternion.Euler(0f, 90f, 0f));
		GameObject instance = Instantiate(Resources.Load(PlayerPrefs.GetString ("avatar", PickPlayerScript.cName), typeof(GameObject))) as GameObject;
		//PlayerPrefs.GetString ("avatar", PickPlayerScript.cName);


	}








	

}
