using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ItemsInfo : MonoBehaviour {


	//Public gameobject that holds a panel
	public GameObject Info;

	//When pushed the panel become active or deactive 
	public void OpenInfo() {

		Info.SetActive (!Info.activeSelf);
	}


}

