## :abc: WordTally - Build Your Own wc Tool Coding Challenge
> https://codingchallenges.fyi/challenges/challenge-wc/

<br>

:arrow_forward: Printing newline, word, and byte counts for each input file and a total number of lines if more than one FILE is specified. 
A word is a non-zero-length sequence of printable characters delimited by white space.

:arrow_forward: In case there is no file as input, the app will read from standard input. 
Since this is a linux/unix command, this standard input,
which is created by another tool and redirected with pipe (|),
will be read from the stdin file descriptor of the process id of wordtally
(/proc/PID-number/fd/0).

<br>

[![final-Small.png](https://i.postimg.cc/DfLpz7ky/final-Small.png)](https://moviesondemand.io)

## Concepts/technologies used:
1. [X] Object-Oriented Programming Principles;
2. [X] Collections Framework — Array, ArraysList and HashMap;
3. [X] Lambda functions, Streams and Method references;
4. [X] Design patterns from the GoF: Singleton and Builder along with Dependency Injection and Inversion of Control;
5. [X] Spring Boot with CommandLineRunner interface and Lombok, Jackson and Apache Commons CLI dependencies;
6. [X] Logging and banner were disabled (spring.main.banner-mode=OFF, logging.level.root=WARN) on Spring Boot along with Tomcat Server (spring.main.web-application-type=NONE);
7. [X] JSON and Serialization;
8. [X] Asynchronous with Concurrency processing implemented with CompletableStage/CompletableFuture API to handle multiple files as input much easier and faster;  
9. [X] IO and NIO libraries for input/output processing;

<br>

> [!NOTE]
> Challenge is :100: completed!

<br>

![](WordTallyClassicOptions.gif)