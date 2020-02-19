# README #

This is the AngularJS proxy for the CTS. Once deployed, people can use this front end to interact with the CTS.

### How do I get set up? ###

* Setting up

        `mvn clean install`

Hey congrats you did it.

* Configuration
* Dependencies
* Database configuration
* How to run tests

### Swarm deployment instructions

* push image to repo
* ssh to swarm manager node
* pull image from repo
* update the service

        `docker service update --force cts_ctsproxy`


### Who do I talk to? ###

* Diego for back end stuff
* Diego?? for front end stuff
