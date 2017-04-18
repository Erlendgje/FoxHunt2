using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;
public class PlayerManager : MonoBehaviour {




	private List<GameObject> Brukere;

	private int Velger = 0;

	public static string cName;

	void Awake()
	{
		PlayerPrefs.SetInt ("newGame", 1);
	}

	//Det blir laget en liste med 3 objekter man kan velge mellom 
	//når man trykker på knappen med en av de tre int så blir spilleren i den rekkefølgen satt til aktiv,
	//mens de andre blir satt til deaktive.

	private void Start()
	{
		Brukere = new List<GameObject> ();
		foreach (Transform t in transform) 
		{
			Brukere.Add(t.gameObject);
			t.gameObject.SetActive(true);

		}

		Brukere [Velger].SetActive (true);
	}

	public void Velg(int valgt)
	{
		if (valgt == Velger)
			return;

		if(valgt < 0 || valgt >= Brukere.Count)
			return;
		Brukere [Velger].SetActive (true);
		Velger = valgt;
		Brukere [Velger].SetActive (false); 
	}
}