using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;

public class PickPlayerScript : MonoBehaviour {


	//A list that holds all the avatars
	private List<GameObject> Karakterer;
	//A string that holds all the avatar names
	public string[] karaktereNavn = {};
	//A int that holds the avatar
	public int Velger = 0;

	void Awake()
	{

		//PlayerPrefs.DeleteAll();

		if(Application.loadedLevel == 0) {
			string temp = PlayerPrefs.GetString("avatar");

			if (temp != "") {
				Application.LoadLevel(1);
			}
		}
		else {
			string temp = PlayerPrefs.GetString("fox");

			if (temp != "") {
				Application.LoadLevel(2);
			}
		}
		
	}

	//Det blir laget en liste med 3 objekter man kan velge mellom 
	//når man trykker på knappen med en av de tre int så blir spilleren i den rekkefølgen satt til aktiv,
	//mens de andre blir satt til deaktive.

	private void Start()
	{//Karakterer is the list that holds all the avatars
		Karakterer = new List<GameObject> ();
		foreach (Transform t in transform) 
		{
			//The gameobjects that are the avatars er set to deactive
			Karakterer.Add(t.gameObject);
			t.gameObject.SetActive(false);

		}
		//the avatar that is picked is aktive, this will always start with the avatar that have the int 0
		Karakterer[Velger].SetActive (true);
	}

	public void Velg(int valgt)
	{

		Camera.main.GetComponent<AudioSource>().Play();
		
		Karakterer [Velger].SetActive (false);
		Velger = valgt;
		Karakterer [Velger].SetActive (true); 

	}
	//load the game and saves the avtar that is picked 
	public void PickAvatar() {
		Debug.Log(karaktereNavn[Velger]);
		PlayerPrefs.SetString("avatar", karaktereNavn[Velger]);
		Application.LoadLevel(1);
	}

	//load the game and saves the fox that is picked 
	public void PlayGame() {
		Debug.Log(karaktereNavn[Velger]);
		PlayerPrefs.SetString("fox", karaktereNavn[Velger]);
		Application.LoadLevel(2);
	}
}



// Kommet fram til dette ved hjelp fra tutorial "https://www.youtube.com/watch?v=T-AbCUuLViA"

