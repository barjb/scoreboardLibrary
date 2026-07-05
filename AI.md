# AI usage

## General
IDE used: Open Code GUI
Model used: DeepSeek V4 Flash Free - the best free reasoning model in Open Code for this task.

## Prompt history

## Artifacts used

## Design phase
```aiignore
You are a senior software architect at a company producing software 
for sport data sector. You specialize in producing system designs.
Your task is to design a library that is going to be used in prototype application.
The library provides utility services to its users.

The library should:
- support multiple simultaneous matches,
- allow to start a new match,
- update the score,
- finish a match,
- get a summary of matches in progress.

Don't create dependencies on an infrastructure like Redis or Kafka.
If needed, then design interfaces, so the infrastructures providers
can be decided in the future.

For persistence use in-memory database
The library should consist of one module.

Design it with good software design practices.
The recipients of the documents are engineers, keep the response concise and to the point.
Don't produce Java code in your design, just give an overview of created classes.
```
