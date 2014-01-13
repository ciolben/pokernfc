package ch.epfl.pokernfc.Logic.texasholdem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActualPokerHand implements Comparable<ActualPokerHand>
{
  /**
   * This is an actual poker hand that can be made from the 7 available cards. For example:
   * 
   * player1 [TWO of HEARTS, THREE of SPADES]
   * community cards:  
   * ACE of DIAMONDS
   * SIX of CLUBS
   * EIGHT of HEARTS
   * FIVE of HEARTS
   * TEN of SPADES
   * 
   * Therefore, one pokerHand would be High Card
   */
  private PokerHandEnum pokerHandEnum;
  
  /**
   * This is the list of winning cards from the ActualPokerHand.  In the example above,
   * we would assign the ACE of DIAMONDS to this List (as it's the winning card for a HIGH_CARD scenario)
   */
  private List<Card> winningCards; 
  
  private List<Card> pokerHand;
  
  public ActualPokerHand(PokerHandEnum pokerHand, List<Card> cards)
  {
    this.pokerHandEnum = pokerHand;
    this.winningCards = cards;
  }

  /**
   * @return The Enumerated type that represents both the poker hand and
   * the strength of the poker hand
   */
  public PokerHandEnum getPokerHandEnum()
  {
    return pokerHandEnum;
  }

  public void setPokerHandEnum(PokerHandEnum pokerHandEnum)
  {
    this.pokerHandEnum = pokerHandEnum;
  }

  public List<Card> getWinningCards()
  {
    return winningCards;
  }

  public void setWinningCards(List<Card> winningCards)
  {
    this.winningCards = winningCards;
  }

  /**
   * @return the 5 cards that make up this particular poker hand
   */
  public List<Card> getPokerHand()
  {
    return pokerHand;
  }

  public void setPokerHand(List<Card> pokerHand)
  {
    this.pokerHand = pokerHand;
  }

  @Override
  public int compareTo(ActualPokerHand o)
  {
    return ((Integer)this.pokerHandEnum.getStrength()).compareTo(o.getPokerHandEnum().getStrength());
  }

  @Override
  public String toString()
  {
    if (pokerHandEnum.equals(PokerHandEnum.HIGH_CARD))
    {
      return "High Card - " + pokerHand.get(4);
    }
    else if (pokerHandEnum.equals(PokerHandEnum.PAIR))
    {
      return determineWinningDuplicates(pokerHand, 1);  
    }
    else if (pokerHandEnum.equals(PokerHandEnum.THREE_OF_A_KIND))
    {
      return determineWinningDuplicates(pokerHand, 3);
    }
    else if (pokerHandEnum.equals(PokerHandEnum.TWO_PAIR))
    {
      return determineWinningDuplicates(pokerHand, 2);
    }
    else if (pokerHandEnum.equals(PokerHandEnum.STRAIGHT))
      return pokerHand.get(4).getValue() + " high Straight";
    else if (pokerHandEnum.equals(PokerHandEnum.FLUSH))
      return pokerHand.get(0).getSuit() + " flush";
    else if (pokerHandEnum.equals(PokerHandEnum.FULL_HOUSE))
      return "Full House - " + pokerHand.get(0).getValue() + " full of " + pokerHand.get(4).getValue();
    else if (pokerHandEnum.equals(PokerHandEnum.FOUR_OF_A_KIND))
      return determineWinningDuplicates(pokerHand, 4);
    else if (pokerHandEnum.equals(PokerHandEnum.STRAIGHT_FLUSH))
      return pokerHand.get(0).getValue() + " high Straight Flush!";
    else 
      return "Royal Flush!";
  }

  private String determineWinningDuplicates(List<Card> winningCards, int numDuplicates)
  {
    Map<Value, List<Card>> valueMap = Game.buildValueMap(winningCards, null);
    List<Card> winningDupes = new ArrayList<Card>();
    for (Map.Entry<Value, List<Card>> entry : valueMap.entrySet())
    {
      if (numDuplicates == 1)
      {
        if (entry.getValue().size() == 2)
        {
          winningDupes.add(entry.getValue().get(0));
        }
      }
      else if (numDuplicates == 2)
      {
        if (entry.getValue().size() == 2)
        {
          winningDupes.add(entry.getValue().get(0));
        }
      }
      else if (numDuplicates == 3)
      {
        if (entry.getValue().size() == 3)
        {
          winningDupes.add(entry.getValue().get(0));
        }
      }
      else if (numDuplicates == 4)
      {
        if (entry.getValue().size() == 4)
        {
          winningDupes.add(entry.getValue().get(0));
        }
      }
      
    }
    if (numDuplicates == 1)
    {
      return "Pair of " + winningDupes.get(0).getValue() + "S";
    }
    else if (numDuplicates == 2)
    {
      return "Two pair - " + winningDupes.get(0).getValue() + "S and " + winningDupes.get(1).getValue() + "S";
    }
    else if (numDuplicates == 3)
    {
      return "Three " + winningDupes.get(0).getValue() + "S";
    }
    else if (winningDupes.size() == 4)
    {
      return "Four " + winningDupes.get(0).getValue() + "S";
    }
    return "";
  }
}
