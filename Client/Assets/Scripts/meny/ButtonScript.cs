using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonScript : MonoBehaviour {

	//når spilleren kolliderer med collidern blir han/hun sendt til neste bane, som er Olympus
	public void StartGame(){


		Application.LoadLevel(2);

	}

	public void Okey(){


		Application.LoadLevel(1);

	}





}
