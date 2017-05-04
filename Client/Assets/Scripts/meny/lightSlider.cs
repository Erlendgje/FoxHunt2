using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class lightSlider : MonoBehaviour {


	//Public slider that holds the slider you want to respond 
	public Slider mainSlider;

	//Holds the light in the gamescene
	public Light lys;



	void Start(){
		
		//Loads the light settings in the game 
		DontDestroyOnLoad (gameObject);
		mainSlider.value = PlayerPrefs.GetFloat ("light", mainSlider.value);

	}


	public void Lightsetting()
	{
		//Changes the intensity of the light based on the sliders value
		lys.intensity = mainSlider.value;
		//saves the light intensity 
		PlayerPrefs.SetFloat ("light", mainSlider.value);



	}



}
