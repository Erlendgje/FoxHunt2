using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class lightSlider : MonoBehaviour {

	public Slider mainSlider;
	float brightNess = 0.5f;

	public Light lys;


	void Awake(){



	}


	void Start(){
		

		DontDestroyOnLoad (gameObject);
		mainSlider.value = PlayerPrefs.GetFloat ("light", mainSlider.value);

	}


	public void Lightsetting()
	{
		lys.intensity = mainSlider.value;
		//RenderSettings.ambientLight = new Color (brightNess, brightNess, brightNess, 1);
		PlayerPrefs.SetFloat ("light", mainSlider.value);



	}



}
