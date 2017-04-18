using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ShopItems : MonoBehaviour {

	public GameObject berry;
	public GameObject apple;
	public GameObject medi;
	public GameObject candy;

	public GUIText score;
	public float scoreCurency;



	// Use this for initialization
	void Start () {

		scoreCurency = 0;

	}
	
	public void AddScore (int newScoreValue)
	{
		scoreCurency += newScoreValue;
		UpdateScore ();
	}

	void UpdateScore ()
	{
		score.text = "Score: " + score;
	}
}
