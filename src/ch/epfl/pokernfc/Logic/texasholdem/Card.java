package ch.epfl.pokernfc.Logic.texasholdem;

public class Card implements Comparable<Card>
{
  private Suit suit;
  private Value value;
  
  public Card (Suit suit, Value value)
  {
    this.suit = suit;
    this.value = value;
  }
  
  public Suit getSuit()
  {
    return suit;
  }
  public Value getValue()
  {
    return value;
  }
  
  @Override
  public String toString()
  {
    return value + " of " + suit;
  }

  @Override
  public int compareTo(Card o)
  {
    int compare = ((Integer)this.getValue().getSuitValue()).compareTo(o.getValue().getSuitValue());
    if (compare == 0)
      compare = ((Integer)this.getSuit().getSuitValue()).compareTo(o.getSuit().getSuitValue());
    return compare;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((suit == null) ? 0 : suit.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Card other = (Card) obj;
    if (suit != other.suit)
      return false;
    if (value != other.value)
      return false;
    return true;
  }
}
