using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class MenuNavi : MonoBehaviour {

	private Transform cameraTransform;
	private Transform cameraLookAt;
	public float speed;


	private void Start(){
	
		cameraTransform = Camera.main.transform;
	
	}

	private void Update(){
	
		if (cameraLookAt != null) {
		
			cameraTransform.rotation = Quaternion.Slerp (cameraTransform.rotation, cameraLookAt.rotation, speed * Time.deltaTime);
		}
	
	}


	public void LookATmenue(Transform menueTransform){
		Debug.Log (menueTransform.tag);
		if (menueTransform.tag == "options" || menueTransform.tag == "shop") {
			speed = 6f;
		
		} else if (menueTransform.tag == "items" || menueTransform.tag == "rever") {
			speed = 3f;
		}
		Debug.Log (speed);
		cameraLookAt = menueTransform.transform;
	}

}
