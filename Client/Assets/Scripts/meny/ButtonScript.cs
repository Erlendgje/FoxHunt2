using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class ButtonScript : MonoBehaviour {

	//når spilleren kolliderer med collidern blir han/hun sendt til neste bane, som er Olympus
	public void Options(){


		Application.LoadLevel(2);

	}

	public void Okey(){


		Application.LoadLevel(1);

	}


	public void BackHome(){


		Application.LoadLevel(1);

	}


	public void Shop(){


		Application.LoadLevel(4);

	}

	public void Foxes(){


		Application.LoadLevel(5);

	}

	public void Inventory(){


		Application.LoadLevel(3);

	}

	public void PlayGame(){


		Application.LoadLevel(0);

	}




}
