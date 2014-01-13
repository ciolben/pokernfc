package ch.epfl.pokernfc.Logic.texasholdem;

import java.util.Comparator;

public enum Value implements Comparator<Value>  
{
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5),
  SIX(6),
  SEVEN(7),
  EIGHT(8),
  NINE(9),
  TEN(10),
  JACK(11),
  QUEEN(12),
  KING(13),
  ACE(14);
  
  private int suitValue;
  
  private Value (int suitValue)
  {
    this.suitValue = suitValue;
  }
  
  public int getSuitValue()
  {
    return suitValue;
  }

  @Override
  public int compare(Value o1, Value o2)
  {
    return ((Integer)o1.getSuitValue()).compareTo(o2.getSuitValue());
  }
}
