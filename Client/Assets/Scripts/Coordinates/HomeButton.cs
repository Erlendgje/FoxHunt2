using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class HomeButton : MonoBehaviour {

	// Use this for initialization
	void Start () {
		this.GetComponent<RectTransform>().sizeDelta = new Vector2(this.transform.parent.GetComponent<RectTransform>().rect.width / 2, this.transform.parent.GetComponent<RectTransform>().rect.height / 12);
		this.GetComponent<RectTransform>().localPosition = new Vector3((this.transform.parent.GetComponent<RectTransform>().rect.width / 2) - (this.GetComponent<RectTransform>().rect.width / 5), 0 - (this.transform.parent.GetComponent<RectTransform>().rect.height / 2) + (this.GetComponent<RectTransform>().rect.height / 2) + 5, 0);

	}

	// Update is called once per frame
	void Update () {
		
	}
}
