using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class lightSlider : MonoBehaviour {

	public static lightSlider control;
	public Slider mainSlider;
	float brightNess = 0.5f;

	public Light lys;


	void Awake(){
		if (control == null) {
	
			DontDestroyOnLoad (gameObject);
			control = this;
		} 
		else if (control != this) {
			Destroy (this);
		}
	
	} 


	void Start(){

		mainSlider.value = PlayerPrefs.GetFloat ("light", mainSlider.value);

	}

	//Invoked when a submit button is clicked.
	 public void Lightsetting()
	{
		lys.intensity = mainSlider.value;
		//RenderSettings.ambientLight = new Color (brightNess, brightNess, brightNess, 1);
		PlayerPrefs.SetFloat ("light", mainSlider.value);



	}



}
