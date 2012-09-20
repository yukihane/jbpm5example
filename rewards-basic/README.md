jbpm5example - rewards-basic
============

This is a basic jBPM5 web application, which aims to provide an example usage of:
- Human Task
- Persistence
- LocalTaskService and SyncWSHumanTaskHandler
- mvn building

fork元のプログラムをJBoss AS7.1.1.Final と jBPM5.3.0.Final で動作するよう改変したものです。

### Steps to run

- mvn clean package
- cp ear/target/rewards-basic-ear-1.0-SNAPSHOT.ear $JBOSS_HOME/server/$PROFILE/deploy
- access to http://localhost:8080/rewards-basic/ with a browser
 - [Start Reward Process] is to start a new process
 - [John's Task] is to list John's tasks and approve them
 - [Mary's Task] is to list Mary's tasks and approve them
