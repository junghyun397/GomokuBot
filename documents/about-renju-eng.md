# ENG

## What is Renju?

Q. What do you mean, GomokuBot and Renju?

A. Gomoku is very simple. But Gomoku is not a fair game. Therefore, GomokuBot uses Renju, which has some very simple rules added, not plain Gomoku.

But don't worry. Renju is really similar to Gomoku. In the game between beginners, it's the same so that, even if they don't know what Renju is, it doesn't affect them at all.

```fname = intro, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X O . . . . . . . . 11
10 . . . . O . . . X O . . . . . 10
 9 . . . . X . X O X X . . . . . 9
 8 . . X . . O . X . . O . . . . 8
 7 . . . O . X X . O X . . . . . 7
 6 . . . X O X O . . . . . . . . 6
 5 . . . . X O . . . . . . . . . 5
 4 . . . . O . O . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*Black wins!*

Renju is a variant game that adds **Forbidden moves** that only apply to black in Gomoku. Before diving into Renju and Forbidden moves, you need to understand why Renju only applies Forbidden moves to black, and how unfair a plain Gomoku game is.

### Gomoku is a Solved Game.

If you've played Gomoku for a while, you've probably realized that the black you start first with is very advantageous in Gomoku, where there are no other restrictions. So, how good is black? What would be the result if both black and white had their best moves?

A plain Gomoku with no additional rules was proved in 1980 by Stefan Reisch. Even if both black and white have their best moves, black can always find a winning strategy.

In other words, in a plain Gomoku, black **always** wins, even if both black and white have their best moves. The higher the level of both players, the closer they get to tossing a coin. In order to get out of the coin toss, a special rule is indispensable to solve the overwhelming advantage of black first.

### Renju has "Forbidden Moves"

Renju chose the "Forbidden moves" rule to address the overwhelming advantage of black First. There are three types of forbids: 3-3 forbid, 4-4 forbid, and overline forbid.

If it looks complicated, don't worry. Forbidden moves are fairly rare in beginner's games, and even if they don't know what a Forbidden move is, it's unlikely to change their win or loss.

To understand forbidden moves correctly, we must first understand how Gomoku defines three and four, and in which situations the forbidden move appears. Here we start with the definition of four.

## What is "Four"?

**Four** is **a row of four stones containing one space**, which means a shape that can be won by adding one more stone.

You can win by making one more move, so if you don't have four, the opponent makes four, you have to block immediately.

The four stones arranged in a straight line are four. Four stone blocks on one side are also four. The four stones arranged one space apart are also four. If you can make a five-in-a-row with a single move, it's all four.

```fname = four, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . X . X X X . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X . X X . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X X . . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . O X X X X . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . X X X . X . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*When making one more move…*

```fname = four-expanded, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . X X X X X . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X X X X . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X X X . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . O X X X X X . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . X X X X X . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```
*...You can win!*

## What is "4-4 Forbid"?

**4-4 forbid** means **a place where you can make two or more fours with a single move**. Even if you can create three fours in a single move, it is 4-4 forbid.

Remember: forbidden moves are rules designed to solve Black's overwhelming advantage. Therefore, all forbidden moves apply only to black. White is free to make 4-4 fork to win!

```fname = double-four-forbid, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . X . . . . X . . . . . 15
14 . . . . X . . . . . . . . . . 14
13 . . X . 4 X X . O 4 X X . X . 13
12 . . . . X . . . . X . . . . . 12
11 . . . . . . . . . X . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . 4 X X X . . . . . . . 8
 7 . . . . X . . . . . . . . . . 7
 6 . . . . X . . O X . X 4 X . . 6
 5 . . . . X . . . . . . . . . . 5
 4 . . . . O . . . . X . . . . . 4
 3 . . . . . . . . X . . . . . . 3
 2 . . . . . . . X . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*GomokuBot marks forbidden moves with a red dot. Black cannot place stones on the red dots, but white can.*

Although less common, more than one 4-4 forbid can appear on the same line. If you can make more than one four with a single move, even on the same line, it's 4-4 forbid.

```fname = double-four-forbid-in-a-line, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . X . X 4 X . X . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . X . X 4 X 4 X 4 X . X . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . . . . . . . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . X X X . 4 . X X X . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . X X . 4 X . X X . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

## What is "Three"?

Three is a little special. That's because it's a bit far from Gomoku's win conditions. Gomoku defines three as a shape that can make a "straight four" in a single move. What exactly is straight four defined by Gomoku?

### Straight Four – Some Fours Are More Strong Than Others.

Here are the five fours we looked at two pages ago. In fact, one of the four has one difference from the other four. This is because the fours in row 8 are **straight fours**.

```fname = straight-four, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . X . X X X . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X . X X . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X X . . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . O X X X X . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . X X X . X . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*If you don't block immediately, you lose, so you have to block four right away. Let's defend.*

```fname = straight-four-had-blocked, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . X O X X X . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X O X X . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X X O . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . O X X X X O . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . X X X O X . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

The other four could have been blocked, but the four in row 8 could not be blocked with a single move. You have to move twice to block, but in Gomoku you can't block because you can only move once per turn.

In this way, the strong four that cannot be blocked with a single move, and the four stones arranged in a row with both sides open are called straight four.

### If You Can Make a Straight Four With a Single Move, It's Three.

Gomoku defines **three** as **the shape that can make a straight four in a single move**. Three is a weaker shape than four, but it's a strong shape that should be blocked right away if your opponent makes three when you don't have three or four.

```fname = three, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X . X . . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X . . . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . . X . X X . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*When making one more move…*

*…Straight four are here!*

```fname = three-expanded, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . X X X X . . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . X X X X . . . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . . X X X X . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

## What is "3-3 Forbid"?

Now that we know what three is defined by Gomoku, we can clearly define 3-3 forbid. **3-3 forbid** means **a position where you can make two or more threes that can make straight four by single move**. Even if you can make three threes with a single move, it's even 3-3 forbid.

*Please remember again. All forbidden moves apply only to black. White is free to make 3-3 to win.*

```fname = dobule-three-forbid, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . X . . . . . . . . . . . 14
13 . . X . X . . . . . X . . X . 13
12 . . . X . . . . . . . . X . . 12
11 . . . . . . . . . . . . X . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . . X X . . . . . . . 8
 7 . . . . . . . . X . . . . . . 7
 6 . . . . . . . . X . . . . . . 6
 5 . . . . . . . . . . . . . X . 5
 4 . . . . . . . . . . . . X . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . X X . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

So far, we have looked at very simple shapes, but in real games, sometimes complex shapes appear. Here are some simple examples. The shapes below do not contain any 3-3 forbids.

```fname = dobule-three-forbid-pseudo, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . X . X . . . . . . . . 14
13 . . X . . . . . . . . . . X . 13
12 . X . . . . . . . . . . X . . 12
11 . . . . . . . . . O . . X X . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . X . . X X . . . . . . . 8
 7 . . . . . X . . . . . . . . . 7
 6 . . . . . X . . . . . . . . . 6
 5 . . . . . . . . . . . . X . . 5
 4 . . . . . . . . . . . . X . . 4
 3 . . . . . . . . X . . X . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

This is because all these shapes are not three overlapping shapes, but actually only three shapes. Even if it looks like three, it's not three if you can't make straight four out of it. If you can't make more than one three in a single move, it's not 3-3 forbid.

## What is "Overline Forbid"?

We're almost there. An **overline forbid** means **a place where you can make 6 or more stones arranged in a row in a single move**.

Even if you can make a 7-in-a-row in a single move, it's an overline forbid. Neither 8-in-a-row nor 9-in-a-row. However, 10-in-a-row is an exception. If someone makes a 10-in-a-row, you must immediately turn off all electronics and leave the area.

*Please remember. Overline forbid is also a rule that only applies to black, and white is free to create more than five-in-a-row to win.*

```fname = overline-forbid, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . X X X . X X X X . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . X . X X X X . X . . . . 8
 7 . . . . . . . . . . . . . . . 7
 6 . . . . . . . . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . X X X X . X . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

## Five-in-a-Row Can Ignore All Forbidden Moves.

Here's the good news (only black unfortunately). Even if forbidden point is created on a point that can be won with a five-in-a-row, if you win with a five-in-a-row, you can move ignoring any forbidden moves.

```fname = five-in-a-row-and-forbid, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . O O X . X . . . . . . 10
 9 . . . X X X X . X X X X X . . 9
 8 . . . . . . X X X O . . . . . 8
 7 . . . . . . O X . . . . . . . 7
 6 . . . . . . . X O . . . . . . 6
 5 . . . . . . . X . . . . . . . 5
 4 . . . . . . . O . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

## Great!

Welcome to the much fairer Gomoku! If you've followed this guide far, you know all the basic Renju rules. Now, even if forbidden moves appear, you will be able to unlock that or use other strategies to continue the game without panicking.

Now start a game with your friends with the ``/start @mention`` command. Even if you don't have friends, the GomokuBot AI will always be with you. Don't forget to configure the GomokuBot with the ``/setting`` command as well.

In the next chapter, we will learn how to figure out forbidden moves in very complex situations and how to attack and defend using forbidden moves. These are strategies for Renju, all possible only in Renju. It can be a little difficult for beginners.

## Like a Forbidden Move, But May Not Be a Forbidden Move.

Consider the following situation. Will Black be able to move with ``h9``? At first glance, it seems that ``h9`` is a 3-3 forbid made by two stones arranged vertically in column h and two stones arranged horizontally in column 9, and it seems that black should not be able to move with ``i9``.

```fname = pseudo-forbid-simple, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . O . . . . . . . 11
10 . . . . . O . . . . . . . . . 10
 9 . . . . . X X . . . . . . . . 9
 8 . . . . . . O X O . . . . . . 8
 7 . . . . . . O X O . . . . . . 7
 6 . . . . . . . . X X X O . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

While these situations aren't common(in fact, they're really, very rare in real games), but you need to be aware of them in order to fully understand what forbidden moves are.

### Imagine What Happens Next.

In complex situations, a good way to figure out forbidden moves is to move one step at a time. First, let's make move to ``h9``, which is the point that we want to figure out.

```fname = pseudo-forbid-simple-s1, forbid = true, lmove = h9
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . O . . . . . . . 11
10 . . . . . O . . . . . . . . . 10
 9 . . . . . X X[X]. . . . . . . 9
 8 . . . . . . O X O . . . . . . 8
 7 . . . . . . O X O . . . . . . 7
 6 . . . . . . . 4 X X X O . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

In this situation, row h is blocked by 4-4 forbid and cannot create straight four. Two stones in row h were not three that could make a straight four in a single move. Let's remember that again. If three or four is not valid, then forbidden move is also invalid. So ``h9`` that can only make one three is not 3-3 forbidden.

## It Doesn't Seem Like a Forbidden Move, But It Could Be a Forbidden Move.

The ``g10`` looks like 3-3 forbid. But since ``i8`` is also forbid, ``g10`` doesn't seem to be forbid 3-3 forbid. How can we figure out how black can move in ``g10`` in this complex situation?

```fname = pseudo-forbid-complex, forbid = false, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . O . . O . . . . . . . . 11
10 . . . . X .[.]X . O . . . . . 10
 9 . . . . O X O X X . . . . . . 9
 8 . . . . . . X X . . O . . . . 8
 7 . . . . . . O O X X . . . . . 7
 6 . . . . . X O . . . . . . . . 6
 5 . . . . O X . . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

### Imagine a Situation In the Future.

A good way to figure out forbidden moves in a very complex situation is also to move them step by step. Let's make move to ``h9``, the point that we want to figure out.

```fname = pseudo-forbid-complex-s1, forbid = false, lmove = g10
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . O . . O . . . . . . . . 11
10 . . . . X .[X]X . O . . . . . 10
 9 . . . . O X O X X . . . . . . 9
 8 . . . . . . X X . . O . . . . 8
 7 . . . . . . O O X X . . . . . 7
 6 . . . . . X O . . . . . . . . 6
 5 . . . . O X . . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

At first glance, ``i8`` seems to be 3-3 forbid where two stones placed vertically in row i and two stones placed horizontally in row 8. So black can't make straight four diagonally, so isn't ``g10`` a forbidden point?

That said, the three stones arranged vertically in a row f seem odd. It's too early to judge. Let's make one more move for ``i8``.

```fname = pseudo-forbid-complex-s2, forbid = true, lmove = i8
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . O . . O . . . . . . . . 11
10 . . . . X O X X . O . . . . . 10
 9 . . . . O X O X X . . . . . . 9
 8 . . . . . 4 X X[X]. O . . . . 8
 7 . . . . . . O O X X . . . . . 7
 6 . . . . . X O . . . . . . . . 6
 5 . . . . O X . . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

After making a move on ``i8``, Black is blocked by 4-4 forbid in the 8th row and cannot make a straight four. The ``i8`` after making one move in ``g10`` was not 3-3 forbid!

Now we can figure out that ``i8`` after moving to ``g10`` is not a forbidden point. Therefore, ``g10``, which can make two straight fours by moving the black once, can figure out that 3-3 forbid is correct.

```fname = pseudo-forbid-complex-s3, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . O . . O . . . . . . . . 11
10 . . . . X . 3 X 3 O . . . . . 10
 9 . . . . O X O X X . . . . . . 9
 8 . . . . . . X X 3 . O . . . . 8
 7 . . . . . . O O X X . . . . . 7
 6 . . . . . X O . . . . . . . . 6
 5 . . . . O X . . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

In the same way, we can figure out that ``i10`` is also a 3-3 forbid. (The ``i10`` is a little more complicated.) Slowly, if you think about it one by one, it's not difficult.

## White Can Attack By Targeting Forbidden Points.

Renju's forbidden moves are just a limitation for black, but for white, it's a strategy and an opportunity. Re-focus on the definition of "forbidden moves". Black cannot be placed in a forbidden point in any case except five-in-a-row. Even if White can move to the forbidden point and win, it can't be an exception.

```fname = forbid-trap-simple, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . . . X . . . . . . . 8
 7 . . . . . . . X O . . . . . . 7
 6 . . . . . . . 3 X X . . . . . 6
 5 . . . . . . O . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

Here's an interesting situation. Black has one 3-3 forbid, and white has 3 stones lined up with 3-3 forbid in between, so white is ready for the four-attack. If white attacks by making four with the black forbidden point, how can black defend itself?

```fname = forbid-trap-simple-s1, forbid = true, lmove = e3
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . . . . . . . . 10
 9 . . . . . . . . . . . . . . . 9
 8 . . . . . . . X . . . . . . . 8
 7 . . . . . . . X O . . . . . . 7
 6 . . . . . . . 3 X X . . . . . 6
 5 . . . . . . O . . . . . . . . 5
 4 . . . . . O . . . . . . . . . 4
 3 . . . .[O]. . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

In order to remove 3-3 forbid, Black must make one move and remove one three. But it's too late for that. Very unfortunately, Black has no way to block White from attacking Four. Black should be watching White win by creating a five-in-a-row.

## White Can Win by Creating a Forbidden Move Trap.

Now if white attacks with a forbidden point, we know that black is going to have to watch white win. But you can't rely on luck or mistakes forever. Given the right circumstances, aggressive attacks can lead the black to create a forebiden point and win.

Here's a situation that looks awful for White. black attacked by making a three with ``f6``. At first glance, white seems to have to block black's three. The black stones that line lower-left also look very strong. Should white be attacked and defeated by black?

```fname = forbid-trap-complex, forbid = true, lmove = f6
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O . . O O . . . . . . 9
 8 . . . . X . . X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X[X]X . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

Let's take a closer look at the situation again. White has 3 stones arranged diagonally and 3 stones arranged horizontally. Four attacks could go on twice. But it's like having a fit.

What we need to note in this situation is that white can create four and make black move to ``g9``, and if the black moves to ``g9``, ``g8`` becomes 3-3 forbid.

```fname = forbid-trap-complex-s1, forbid = true, lmove = f9
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O[O]. O O . . . . . . 9
 8 . . . . X . . X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X X X . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*If white makes four, black must block four.*

```fname = forbid-trap-complex-s2, forbid = true, lmove = g9
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O O[X]O O . . . . . . 9
 8 . . . . X 3 3 X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X X X . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

Black was able to defend four, but two stones arranged horizontally in row 8 and two stones arranged vertically in column g resulted in 3-3 forbid.

```fname = forbid-trap-complex-s3, forbid = true, lmove = j11
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . .[O]. . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O O X O O . . . . . . 9
 8 . . . . X 3 3 X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X X X . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*Since ``g8`` is a 3-3 forbid, black has no way to stop the subsequent diagonal four attacks of white. White wins!!*

This is the beauty of Renju! White can use 4-4 forbid and overline forbid in the same way to create and win traps. Black should read the situation well and be extra careful not to fall into the trap and ruin the game.

## Black Can Escape the Trap by Marking It Non-Forbidden Move

Here's a twist. Black was able to win without being trapped. ``f6`` is an excellent point for black. But as we've seen before, if black puts it in ``f6``, Black will be caught in white's trap and defeated. How can black move safely to ``f6``?

```fname = counter-forbid-trap, forbid = true, lmove = null
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O . . O O . . . . . . 9
 8 . . . . X . . X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X . X . . . . . . . . 6
 5 . . . . . . . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

Here we need to re-imagine the conditions of forbidden moves. If three or four is not valid, then forbidden moves are also invalid. The black can remove forbidden point that will be generated by moving to ``g9`` in advance.

```fname = counter-forbid-trap-s1, forbid = true, lmove = e5
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O . . O O . . . . . . 9
 8 . . . . X . . X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X . X . . . . . . . . 6
 5 . . . .[X]. . . . . . . . . . 5
 4 . . . . . . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

*White must defend black's four.*

```fname = counter-forbid-trap-s2, forbid = true, lmove = e4
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . . . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O . . O O . . . . . . 9
 8 . . . . X . . X . . . . . . . 8
 7 . . . . X O . . . . . . . . . 7
 6 . . . . X . X . . . . . . . . 6
 5 . . . . X . . . . . . . . . . 5
 4 . . . .[O]. . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

That's it! Now Black can move freely with ``f6``. Black has made a place in advance to make a diagonal four, so three of the two horizontal and vertical threes that will be made as White's trap is no longer three.

```fname = counter-forbid-trap-s4, forbid = true, lmove = g8
   A B C D E F G H I J K L M N O
15 . . . . . . . . . . . . . . . 15
14 . . . . . . . . . . . . . . . 14
13 . . . . . . . . . . . . . . . 13
12 . . . . . . . . . . . . . . . 12
11 . . . . . . . . . O . . . . . 11
10 . . . . . . . . O . . . . . . 10
 9 . . . . O O X O O . . . . . . 9
 8 . . . . X . X X . . . . . . . 8
 7 . . . . X O 4 . . . . . . . . 7
 6 . . . . X X X . . . . . . . . 6
 5 . . . . X . . . . . . . . . . 5
 4 . . . . O . . . . . . . . . . 4
 3 . . . . . . . . . . . . . . . 3
 2 . . . . . . . . . . . . . . . 2
 1 . . . . . . . . . . . . . . . 1
   A B C D E F G H I J K L M N O
```

Although not always possible (this is a very rare situation in real games), Black can get out of the trap by making a "vaccine move" that removes the condition of the forbidden moves. Well worth one last thought before giving up!

## Perfect!

Now you know everything you need to know about Renju. You know what's wrong with Gomoku, and you know how Renju solved Gomoku's problem. You know how to apply the Renju rule correctly in very complex situations, and you know how to use Renju to attack and defend.

Renju is a very simple but very engaging game with an interesting and endless strategy. With GomokuBot, challenge more complex problems with your friends. It will definitely be fun.

If you have any questions, please feel free to visit the GomokuBot [support channel](https://discord.gg/vq8pkfF). I hope you have a good time with GomokuBot. — *GomokuBot developer junghyun397.*
