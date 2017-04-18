using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ItemsInfo : MonoBehaviour {

	public GameObject Info;

	public void OpenInfo() {

		Info.SetActive (!Info.activeSelf);
	}

	// Update is called once per frame
	void Update () {

	}
}

