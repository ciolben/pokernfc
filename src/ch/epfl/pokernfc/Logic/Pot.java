package ch.epfl.pokernfc.Logic;

import ch.epfl.pokernfc.Logic.network.Server;

public class Pot {

	private float m_value = 0.f;
	
	protected Pot() {
		
	}
	
	public void addCash(float amount) {
		m_value += amount;
	}
	
	/**
	 * Return the capital.
	 * @return
	 */
	public float getCash() {
		return m_value;
	}
	
	/**
	 * Remove the given amount of cash.
	 * @param amount
	 * @return false if the Pot has not a sufficient capital (no cash is removed)
	 */
	public boolean removeCash(float amount) {
		if (amount > 0) {
			amount = -amount;
		}
		if (m_value < amount) {
			return false;
		}
		m_value -= amount;
		return true;
	}
	
	/**
	 * Remove a fraction of the current Pot cash.
	 * Ex : current cash = 100, frac = 0.75
	 * 		Then, it returns 75, remaining cash in the Pot : 25.
	 * @param frac
	 * @return cash removed.
	 */
	public float getFraction(float frac) {
		float removedFrac = m_value * frac;
		m_value -= removedFrac;
		assert(m_value >= 0);
		return removedFrac;
	}
	
	/**
	 * Clear the Pot.
	 * @return the content of the Pot before being cleared.
	 */
	public float clear() {
		float tmp = m_value;
		m_value = 0.f;
		return tmp;
	}
}
