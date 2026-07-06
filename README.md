# scoreboardLibrary

## Assumptions
1. I did not rely on infrastructure.
2. The library should expose utility services to its users.
3. I assumed that AI aspect of the task was more important that the produced code. 

## Reasoning
I tried to implement the solution as simple as possible.
Library with spring annotations still allows for a high customisation in customer applications. 

## Trade-offs made
1. Slow synchronized methods in ScoreboardServiceImpl. Unrelated matches cannot be finished at once.
This is a big scaling bottleneck.
2. Team can play in only one match at once. Potential issue when data would end up in invalid state,
for example when service finishing match would go down and the transaction finishing match end up uncommited.
3. All implementation in one service, splitting into smaller coherent services to consider.