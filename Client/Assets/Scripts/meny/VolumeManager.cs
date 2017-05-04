using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class VolumeManager : MonoBehaviour {

	//Public slider that holds the slider you want to respond 
	public Slider Volume;
	//The audio you want to play off
	public AudioSource music;



	void Start(){
	//Loads the volume value that where saved
		Volume.value = PlayerPrefs.GetFloat ("music", Volume.value);
	
	}



	void Update () {
		//the music volume is the sliders value 
		music.volume = Volume.value;
		//saves the value 
		PlayerPrefs.SetFloat ("music", Volume.value);
	}
		


}
