using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class Shopping : MonoBehaviour {


	public int berry;
	public int apple;
	public int candy;
	public int medi;

	private int money;
	public Text moneyText;

	public Text berrytekst;
	public Text appletekst;
	public Text candytekst;
	public Text meditekst;


	void Start ()
	{
		money = 100;
		UpdateScore ();


	}


	public void Candy ()
	{
		money -= 10;
		candy += 1;
		UpdateScore ();

	}

	public void Berry ()
	{
		money -= 10;
		berry += 1;
		UpdateScore ();

	}

	public void Apple ()
	{
		money -= 10;
		apple += 1;
		UpdateScore ();

	}

	public void Medi ()
	{
		money -= 10;
		medi += 1;
		UpdateScore ();

	}

	void UpdateScore ()
	{
		moneyText.text = "Penger: " + money;
		candytekst.text = "Du har: " + candy;
		berrytekst.text = "Du har: " + berry;
		meditekst.text = "Du har: " + medi;
		appletekst.text = "Du har: " + apple;
	}
}
