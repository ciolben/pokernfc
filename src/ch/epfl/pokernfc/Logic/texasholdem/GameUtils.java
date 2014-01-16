package ch.epfl.pokernfc.Logic.texasholdem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 
 * @author http://howtoprogramwithjava.com/java-practice-assignment-6/
 *
 */
public class GameUtils
{
  private Deck deck;
  private List<Player> players = new ArrayList<Player>();
  private List<Card> communityCards = new ArrayList<Card>();
  
  public GameUtils ()
  {
//    for (int i=1; i<=4; i++)
//      players.add(new Player("player"+i));
//    
//    deck = new Deck();
//    deck.shuffleDeck();
//    
//    deal();
//    System.out.println(determineWinner());
  }
  
  //CHANGED : String -> Player
  public Player determineWinner()
  {
    int winningPlayerHandStrength = 0;
    List<Card> winningPlayerWinningCards = null;
    List<Card> winningPlayerWinningHand = null;
    Player winningPlayer = null;
    boolean draw = false;
    String kicker = "";
    
    for (Player player : players)
    {
      player.setPlayerHandStrength(checkForHand(player.getHand(), communityCards));
      player.setPlayableHand(player.getPlayerHandStrength().getPokerHand());
      List<Card> playerWinningCards = player.getPlayerHandStrength().getWinningCards();
      if (player.getPlayerHandStrength().getPokerHandEnum().getStrength() > winningPlayerHandStrength)
      {
        winningPlayer = player;
        winningPlayerHandStrength = player.getPlayerHandStrength().getPokerHandEnum().getStrength();
        winningPlayerWinningCards = playerWinningCards;
        winningPlayerWinningHand = player.getPlayableHand();
        draw = false;
        kicker = "";
      }
      else if (player.getPlayerHandStrength().getPokerHandEnum().getStrength() == winningPlayerHandStrength && winningPlayer != player)
      {
        Boolean newWinner = null;
        
        // compare two players' winning cards first
        Collections.sort(winningPlayerWinningCards);
        Collections.sort(playerWinningCards);
        for (int i=playerWinningCards.size()-1; i>0; i--)
        {
          if (playerWinningCards.get(i).getValue().getSuitValue() > winningPlayerWinningCards.get(i).getValue().getSuitValue())
          {
            newWinner = true;
            draw = false;
            kicker = "";
            if (player.getPlayerHandStrength().getPokerHandEnum().equals(PokerHandEnum.FLUSH))
            {
              kicker = ", " + playerWinningCards.get(i).getValue() + " kicker";
            }
          }
          else if (playerWinningCards.get(i).getValue().getSuitValue() == winningPlayerWinningCards.get(i).getValue().getSuitValue())
          {
          }
          else
          {
            newWinner = false;
            break;
          }
        }
        
        // if no winner is found then compare the entire hands against each other
        if (newWinner == null)
        {
          for (int i=4; i>=0; i--)
          {
            if (player.getPlayableHand().get(i).getValue().getSuitValue() > winningPlayerWinningHand.get(i).getValue().getSuitValue())
            {
              newWinner = true;
              draw = false;
              if (!player.getPlayerHandStrength().getPokerHand().equals(PokerHandEnum.HIGH_CARD))
                  kicker = ", " + player.getPlayableHand().get(i).getValue() + " kicker";
              break;
            }
            else if (player.getPlayableHand().get(i).getValue().getSuitValue() == winningPlayerWinningHand.get(i).getValue().getSuitValue())
            {
            }
            else
            {
              newWinner = false;
              break;
            }
          }
        }
        
        if (newWinner == null)
        {
          draw = true;
        }
        else if (newWinner)
        {
          draw = false;
          winningPlayer = player;
          winningPlayerHandStrength = player.getPlayerHandStrength().getPokerHandEnum().getStrength();
          winningPlayerWinningHand = player.getPlayableHand();
          winningPlayerWinningCards = playerWinningCards;
        }
      }
      System.out.print(player);
      System.out.print(" - " + player.getPlayerHandStrength());
      System.out.println();
    }
    
    System.out.println();
    for (Card card : communityCards)
    {
      System.out.println(card);
    }
    System.out.println();
//    if (draw)
//     return "There was a draw with hand: " + winningPlayer.getPlayerHandStrength();
//    else
//      return winningPlayer + " wins with " + winningPlayer.getPlayerHandStrength() + kicker;
    return winningPlayer;
  }

  ActualPokerHand checkForHand(List<Card> hand, List<Card> communityCards)
  {
    List<ActualPokerHand> pokerHands = new ArrayList<ActualPokerHand>();
    
    checkForHighcard(hand, communityCards, pokerHands);
    checkForPair(hand, communityCards, pokerHands);
    checkForThreeOfAKind(hand, communityCards, pokerHands);
    checkForTwoPair(hand, communityCards, pokerHands);
    checkForStraight(hand, communityCards, pokerHands);
    checkForFlush(hand, communityCards, pokerHands);
    checkForFullHouse(hand, communityCards, pokerHands);
    checkForFourOfAKind(hand, communityCards, pokerHands);
    checkForStraightFlush(hand, communityCards, pokerHands);
    Collections.sort(pokerHands);
    ActualPokerHand bestPokerHand = pokerHands.get(pokerHands.size() - 1);
    
    bestPokerHand.setPokerHand(buildBestPokerHand(bestPokerHand.getWinningCards(), hand, communityCards));
    
    Collections.sort(bestPokerHand.getPokerHand());
    
    return bestPokerHand;
  }

  private List<Card> buildBestPokerHand(List<Card> winningCards, List<Card> hand, List<Card> communityCards)
  {
    List<Card> allCards = new ArrayList<Card>();
    allCards.addAll(hand);
    allCards.addAll(communityCards);
    for (Card card : winningCards)
    {
      allCards.remove(card);
    }
    
    Collections.sort(allCards);
    
    int size = winningCards.size();
    List<Card> playableHand = new ArrayList<Card>();
    playableHand.addAll(winningCards);
    for (int i=0; i<(5-size); i++)
    {
      playableHand.add(allCards.remove(allCards.size()-1));
    }
    return playableHand;
  }

  private void checkForStraightFlush(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    List<List<Card>> straightHands = new ArrayList<List<Card>>();
    List<List<Card>> flushHands = new ArrayList<List<Card>>();
    
    for (ActualPokerHand pokerHand : pokerHands)
    {
      if (pokerHand.getPokerHandEnum().equals(PokerHandEnum.STRAIGHT))
      {
        straightHands.add(pokerHand.getWinningCards());
      }
      else if (pokerHand.getPokerHandEnum().equals(PokerHandEnum.FLUSH))
      {
        flushHands.add(pokerHand.getWinningCards());
      }
    }
    
    for (List<Card> straightHand : straightHands)
    {
      for (List<Card> flushHand : flushHands)
      {
        if (straightHand.equals(flushHand))
        {
          if (flushHand.get(4).getValue().equals(Value.ACE))
          {
            pokerHands.add(new ActualPokerHand(PokerHandEnum.ROYAL_FLUSH, straightHand));
          }
          pokerHands.add(new ActualPokerHand(PokerHandEnum.STRAIGHT_FLUSH, straightHand));
          
        }
      }
    }
  }

  private void checkForFullHouse(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    List<Card> pair = null;
    List<Card> three = null;
    for (ActualPokerHand pokerHand : pokerHands)
    {
      if (pokerHand.getPokerHandEnum().equals(PokerHandEnum.PAIR))
      {
        pair = pokerHand.getWinningCards();
      }
      if (pokerHand.getPokerHandEnum().equals(PokerHandEnum.THREE_OF_A_KIND) && pokerHand.getWinningCards().get(0).getValue() != pair.get(0).getValue())
      {
        three = pokerHand.getWinningCards();
      }
    }
    
    if (pair != null && three != null)
    {
      List<Card> winningCards = new ArrayList<Card>();
      winningCards.addAll(pair);
      winningCards.addAll(three);
      pokerHands.add(new ActualPokerHand(PokerHandEnum.FULL_HOUSE, winningCards));
    }
  }

  private void checkForTwoPair(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    int pairCount = 0;
    List<Card> pairValues = new ArrayList<Card>();
    for (ActualPokerHand pokerHand : pokerHands)
    {
      if (pokerHand.getPokerHandEnum().equals(PokerHandEnum.PAIR))
      {
        pairCount++;
        pairValues.addAll(pokerHand.getWinningCards());
      }
    }
    
    if (pairCount >= 2)
    {
      Collections.sort(pairValues);
      List<Card> winningCards = new ArrayList<Card>();
      for (int i=0; i<4; i++)
      {
        winningCards.add(pairValues.remove(pairValues.size()-1));
      }
      pokerHands.add(new ActualPokerHand(PokerHandEnum.TWO_PAIR, winningCards));
    }
  }
  
  private void checkForHighcard(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    List<Card> highCardDeck = new ArrayList<Card>();
    
    highCardDeck.addAll(hand);
    highCardDeck.addAll(communityCards);
    
    Collections.sort(highCardDeck);
    
    List<Card> winningCard = new ArrayList<Card>();
    winningCard.add(highCardDeck.get(highCardDeck.size()-1));
    pokerHands.add(new ActualPokerHand(PokerHandEnum.HIGH_CARD, winningCard));
  }

  private void checkForFlush(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    Map<Suit, List<Card>> suitMap = buildSuitMap(hand, communityCards);
    
    for (Map.Entry<Suit, List<Card>> entry : suitMap.entrySet())
    {
      List<Card> winningCardsPool = entry.getValue();
      if (winningCardsPool.size() >= 5)
      {
        Collections.sort(winningCardsPool);
        for (int i=0; i<=(winningCardsPool.size()-5);i++)
        {
          List<Card> winningCards = new ArrayList<Card>();
          for (int j=0; j<5; j++)
          {
            winningCards.add(winningCardsPool.get(j+i));
          }
          pokerHands.add(new ActualPokerHand(PokerHandEnum.FLUSH, winningCards));
        }
      }
    }
  }

  private Map<Suit, List<Card>> buildSuitMap(List<Card> hand, List<Card> communityCards)
  {
    Map<Suit, List<Card>> suitMap = new HashMap<Suit, List<Card>>();
    
    if (hand != null)
    {
      for (Card handCard : hand)
      {
        Suit key = handCard.getSuit();
        if (suitMap.containsKey(key))
        {
          suitMap.get(key).add(handCard);
        }
        else
        {
          List<Card> card = new ArrayList<Card>();
          card.add(handCard);
          suitMap.put(key, card);
        }
      }
    }
    
    if (communityCards != null)
    {
      for (Card communityCard : communityCards)
      {
        Suit key = communityCard.getSuit();
        if (suitMap.containsKey(key))
        {
          suitMap.get(key).add(communityCard);
        }
        else
        {
          List<Card> card = new ArrayList<Card>();
          card.add(communityCard);
          suitMap.put(key, card);
        }
      }
    }
    return suitMap;
  }

  private void checkForStraight(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    
    // construct a new possible straight deck by eliminating duplicates and ordering them
    List<Card> straightDeck = new ArrayList<Card>();
    List<Card> duplicates = new ArrayList<Card>();
    
    buildStraightDeck(hand, straightDeck, duplicates);
    buildStraightDeck(communityCards, straightDeck, duplicates);
    
    Collections.sort(straightDeck);
    // Iterate through the number of possible straights
    // i.e. if there are 5 cards, there's only one possible straight
    //     if there are 6 cards, two possible straights
    //     if there are 7 cards, three possible straights
    if (straightDeck.size() >= 5)
    {
      for (int i=0; i<=Math.abs((5-straightDeck.size())); i++)
      {
        boolean straight = true;
        List<Card> straightHand = new ArrayList<Card>();
        for (int j=0; j<4; j++)
        {
          if ((straightDeck.get(j+i).getValue().getSuitValue() + 1) != straightDeck.get((j+i)+1).getValue().getSuitValue())
          {
            straight = false;
            break;
          }
          else
          {
            straightHand.add(straightDeck.get(j+i));
          }
        }
        if (straight)
        {
          straightHand.add(straightDeck.get(4+i));
          if (straightHand.size() > 0)
          {
            List<Card> winningStraight = straightHand;
            pokerHands.add(new ActualPokerHand(PokerHandEnum.STRAIGHT, winningStraight));
          }
          if (duplicates.size() > 0)
          {
            for (Card duplicate : duplicates)
            {
              List<Card> anotherStraight = new ArrayList<Card>();
              for (Card card : straightHand)
              {
                if (card.getValue().equals(duplicate.getValue()))
                {
                  anotherStraight.add(duplicate);
                }
                else
                {
                  anotherStraight.add(card);
                }
              }
              pokerHands.add(new ActualPokerHand(PokerHandEnum.STRAIGHT, anotherStraight));
            }
          }
        }
      }
    }
    
//    List<List<Card>> straightHands = new ArrayList<List<Card>>();
//    for (int i=0; i<3; i++)
//    {
//      List<Card> possibleStraight = verifyStraight(straightDeck, i, i+4);
//      if (possibleStraight != null)
//      {
//        Collections.sort(possibleStraight);
//        straightHands.add(possibleStraight);
//      }
//    }
//    
//    Collections.sort(straightDeck, new Comparator<Card>()
//    {
//      @Override
//      public int compare(Card o1, Card o2)
//      {
//        int compare = ((Integer)o1.getSuit().getSuitValue()).compareTo(o2.getSuit().getSuitValue());
//        if (compare == 0)
//          compare = ((Integer)o1.getValue().getSuitValue()).compareTo(o2.getValue().getSuitValue());
//        return compare;
//      }
//    });
//    
//    for (int i=0; i<3; i++)
//    {
//      List<Card> possibleStraight = verifyStraight(straightDeck, i, i+4);
//      if (possibleStraight != null)
//      {
//        Collections.sort(possibleStraight);
//        straightHands.add(possibleStraight);
//      }
//    }
//    
//    if (straightHands.size() > 0 && straightHands.get(straightHands.size()-1) != null)
//    {
//      List<Card> winningStraight = straightHands.get(straightHands.size()-1);
//      pokerHands.add(new PokerHandWithValue(PokerHand.STRAIGHT, winningStraight));
//    }
  }

  private void buildStraightDeck(List<Card> hand, List<Card> straightDeck,
      List<Card> duplicates)
  {
    for (Card card : hand)
    {
      boolean containsCard = false;
      for (Card straightCard : straightDeck)
      {
        if (card.getValue().equals(straightCard.getValue()))
        {
          containsCard = true;
        }
      }
      if (containsCard)
      {
        duplicates.add(card);
      }
      else
      {
        straightDeck.add(card);
      }
    }
  }

  private void checkForPair(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    Map<Value, List<Card>> valueMap = buildValueMap(hand, communityCards);
    
    List<Card> winningCards = duplicateCards(valueMap, 2);
    
    if (winningCards != null && winningCards.size() > 0)
    {
      pokerHands.add(new ActualPokerHand(PokerHandEnum.PAIR, winningCards));
      List<Card> newHand = new ArrayList<Card>(hand);
      List<Card> newCommunityCards = new ArrayList<Card>(communityCards);
      for (Card card : winningCards)
      {
        newHand.remove(card);
        newCommunityCards.remove(card);
      }
      checkForPair(newHand, newCommunityCards, pokerHands);
    }
  }

  private List<Card> duplicateCards(Map<Value, List<Card>> valueMap, int numDuplicates)
  {
    List<Card> winningCards = new ArrayList<Card>();
    for (Map.Entry<Value, List<Card>> entry : valueMap.entrySet())
    {
      if (entry.getValue().size() >= numDuplicates)
      {
        winningCards.addAll(entry.getValue());
        break;
      }
    }
    return winningCards;
  }
  
  private void checkForThreeOfAKind(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    Map<Value, List<Card>> valueMap = buildValueMap(hand, communityCards);
    
    List<Card> winningCards = duplicateCards(valueMap, 3);
    
    if (winningCards != null && winningCards.size() > 0)
    {
      pokerHands.add(new ActualPokerHand(PokerHandEnum.THREE_OF_A_KIND, winningCards));
    }
  }
  
  private void checkForFourOfAKind(List<Card> hand, List<Card> communityCards, List<ActualPokerHand> pokerHands)
  {
    Map<Value, List<Card>> valueMap = buildValueMap(hand, communityCards);
    
    List<Card> winningCards = duplicateCards(valueMap, 4);
    
    if (winningCards != null && winningCards.size() > 0)
      pokerHands.add(new ActualPokerHand(PokerHandEnum.FOUR_OF_A_KIND, winningCards)); 
  }

  public static Map<Value, List<Card>> buildValueMap(List<Card> hand,
      List<Card> communityCards)
  {
    Map<Value, List<Card>> valueMap = new HashMap<Value, List<Card>>();
    
    if (hand != null)
    {
      for (Card handCard : hand)
      {
        Value key = handCard.getValue();
        if (valueMap.containsKey(key))
        {
          valueMap.get(key).add(handCard);
        }
        else
        {
          valueMap.put(key, new ArrayList<Card>());
          valueMap.get(key).add(handCard);
        }
      }
    }
    
    if (communityCards != null)
    {
      for (Card communityCard : communityCards)
      {
        Value key = communityCard.getValue();
        if (valueMap.containsKey(key))
        {
          valueMap.get(key).add(communityCard);
        }
        else
        {
          valueMap.put(key, new ArrayList<Card>());
          valueMap.get(key).add(communityCard);
        }
      }
    }
    
    return valueMap;
  }

  void deal ()
  {
    for (Player player : players)
    {
      List<Card> playerHand = new ArrayList<Card>();
      playerHand.add(deck.getCards().remove(0));
      playerHand.add(deck.getCards().remove(0));
      
      player.setHand(playerHand);
    }
    
    for (int i=0; i<5; i++)
    {
      communityCards.add(deck.getCards().remove(0));
    }
  }

  public void setDeck(Deck deck)
  {
    this.deck = deck;
  }

  public void setPlayers(List<Player> players)
  {
    this.players = players;
  }

  public void setCommunityCards(List<Card> communityCards)
  {
    this.communityCards = communityCards;
  }
  
}
