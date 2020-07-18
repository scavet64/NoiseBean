# NoiseBean

This is a personalized discord bot to be used in my personal discord server. This bot is build using the JDA implementation of the Discord API and leverages the dependency injection provided by spring boot as well as the database connectivity of Data JPA. The database is constructed using the automatic table generation that hibernate provides based on the defined entities in each package. The databases is a simple SQLite file and is saved locally in the current executing directory.

## Features

Currently the bot has the following features:
 - Playing songs from youtube
 - Playing custom sounds that users upload
 - Playing a custom sound when a member enters a voice channel
 - Playing a custom sound when a member leaves a voice channel
 - Tracks the total playtime/session for games that members are playing
 - Get a list of the total time played for an individual game
 - Get a list of the total playtime for all games 
 - Creating a list of games and conducting a private election

## Release History

* 1.3.1
    * Implemented sorting for Game Play Times
	* Addressing an issue regarding voting
	* Using new version format
* 0.1.3
    * Refactored the tracking of user information
    * Added the ability to track the gametime sessions for members
    * Added the ability to get a list of gametimes
* 0.1.2
    * Added the ability to create a list of games
    * Added the ability to run a private election in direct messages
* 0.1.1
    * Added the ability to upload custom sounds and play them
    * Added the ability to add custom introductions
    * Added the ability to add custom outros
* 0.1.0
    * Initial development of the bot
    * Implemented playing of youtube videos over voicechat

## Copyright

Vincent Scavetta – [@scavettapps](https://twitter.com/scavettapps) – scavettapps@gmail.com

## Contributing

1. Fork it (<https://github.com/scavet64/NoiseBean/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request