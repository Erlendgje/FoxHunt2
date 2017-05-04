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



	//This is just for show, it dosent really do anything beside deducting ints when a button is pushed
	//All the functions are placed on the items buttons in the shop
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

	//It gets the public text and ads the new int value of the object 
	void UpdateScore ()
	{
		moneyText.text = "Penger: " + money;
		candytekst.text = "Du har: " + candy;
		berrytekst.text = "Du har: " + berry;
		meditekst.text = "Du har: " + medi;
		appletekst.text = "Du har: " + apple;
	}
}
