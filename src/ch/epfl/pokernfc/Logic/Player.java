package ch.epfl.pokernfc.Logic;

import ch.epfl.pokernfc.Logic.network.Client;

public class Player {
	
	private float m_cash = 0.f;
	
	protected Player() {
		
	}
	
	public float getCash() {
		return m_cash;
	}
	
	public void addCash(float amount) {
		m_cash += amount;
	}
}
