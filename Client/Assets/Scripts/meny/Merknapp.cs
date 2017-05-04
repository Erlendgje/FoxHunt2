using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Merknapp : MonoBehaviour {

	//Public gameobject that holds a panel
	public GameObject MerKnapp;

	//When pushed the panel become active or deactive 
	public void Openmeny () {

		MerKnapp.SetActive (!MerKnapp.activeSelf);
	}
	


}
