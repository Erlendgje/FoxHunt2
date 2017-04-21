using UnityEngine;
using System.Collections;
using System.Collections.Generic;
using UnityEngine.UI;

public class PickPlayerScript : MonoBehaviour {

	private List<GameObject> Karakterer;
	public string[] karaktereNavn = { "Baby", "Boy", "Girl"};

	public int Velger = 0;

	void Awake()
	{

		//PlayerPrefs.DeleteAll();
		string temp = PlayerPrefs.GetString("avatar");

		if (temp != "") {
			Application.LoadLevel(1);
		}
	}

	//Det blir laget en liste med 3 objekter man kan velge mellom 
	//når man trykker på knappen med en av de tre int så blir spilleren i den rekkefølgen satt til aktiv,
	//mens de andre blir satt til deaktive.

	private void Start()
	{
		Karakterer = new List<GameObject> ();
		foreach (Transform t in transform) 
		{
			Karakterer.Add(t.gameObject);
			t.gameObject.SetActive(false);

		}

		Karakterer[Velger].SetActive (true);
	}

	public void Velg(int valgt)
	{
		Karakterer [Velger].SetActive (false);
		Velger = valgt;
		Karakterer [Velger].SetActive (true); 
	}

	public void PlayGame() {
		PlayerPrefs.SetString("avatar", karaktereNavn[Velger]);
		Application.LoadLevel(1);

	}
}



// Kommet fram til dette ved hjelp fra tutorial "https://www.youtube.com/watch?v=T-AbCUuLViA"

