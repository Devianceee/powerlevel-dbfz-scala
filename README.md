# Powerlevel - A Rating System for DBFZ
Ratings in Dragonball Fighterz

# How does this work?
Replays are pulled from Strive's servers using the API described in [dbfz-api-py](https://github.com/Devianceee/dbfz-api-py). 
They are then processed using a modified Glicko algorithm, which you can read more about 
[here](https://github.com/halvnykterist/rating-update/blob/master/docs/modified-glicko.md).

# Setting up a local database for development
Since this uses Docker to create a postgres SQL database, 
you must have docker installed for your machine, and then you can go to the docker folder 
and in a terminal type `docker-compose up` to get the database created and running.

# Starting the server locally
After making sure the database is running (will crash if database is not running), Go to the root folder and type `sbt run` to run the server.

Go to `localhost:9000` in your preferred browser, and you'll be able to see Powerlevel up and running!

# Any questions or improvements you want to make?
This is a fun side project so any questions you have, please shoot them to my Discord: Deviance#3806 or feel free to make an issue on GitHub.

If you have any improvements or suggestions, feel free to shoot me a message, make an issue or even create your own fork!
