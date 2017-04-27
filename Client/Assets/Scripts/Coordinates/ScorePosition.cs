using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScorePosition : MonoBehaviour {

	// Use this for initialization
	void Start () {

		this.GetComponent<RectTransform>().sizeDelta = new Vector2(this.transform.parent.GetComponent<RectTransform>().rect.width - 40, this.transform.parent.GetComponent<RectTransform>().rect.height / 6);
		this.GetComponent<RectTransform>().localPosition = new Vector3(0, (this.transform.parent.GetComponent<RectTransform>().rect.height / 2) - (this.GetComponent<RectTransform>().rect.height / 2), 0);

	}
	
	// Update is called once per frame
	void Update () {
		
	}
}
