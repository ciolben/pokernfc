package ch.epfl.pokernfc.Logic;

import ch.epfl.pokernfc.Logic.network.Server;

public class Pot {

	private float m_value = 0.f;
	
	protected Pot() {
		
	}
	
	public void addCash(float amount) {
		m_value += amount;
	}
	
	public float getFraction(float frac) {
		float removedFrac = m_value * frac;
		m_value -= removedFrac;
		assert(m_value >= 0);
		return removedFrac;
	}
	
	public void clear() {
		m_value = 0.f;
	}
}
