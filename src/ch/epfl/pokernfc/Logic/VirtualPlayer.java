package ch.epfl.pokernfc.Logic;

public class VirtualPlayer {
	
	private float m_cash = 0.f;
	private float mFollowAmount = 0.f;
	
	protected VirtualPlayer() {
		
	}
	
	public float getCash() {
		return m_cash;
	}
	
	/**
	 * Remove the given amount of cash.
	 * @param amount
	 * @return false if the player cannot pay (no cash is removed)
	 */
	public boolean removeCash(float amount) {
		if (amount < 0) {
			amount = -amount;
		}
		if (m_cash < amount) {
			return false;
		}
		m_cash -= amount;
		return true;
	}
	
	public void addCash(float amount) {
		m_cash += amount;
	}
	
	public void setFollowAmount(float amount) {
		mFollowAmount = amount;
	}
	
	public float getFolowAmount() {
		return mFollowAmount;
	}
}
