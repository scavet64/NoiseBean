# NoiseBean

This is a personalized discord bot to be used in my personal discord server. This bot is build using the JDA implementation of the Discord API and leverages the dependency injection provided by spring boot as well as the database connectivity of Data JPA. The database is constructed using the automatic table generation that hibernate provides based on the defined entities in each package. The databases is a simple SQLite file and is saved locally in the current executing directory.

## Features

Currently, the bot has the following features:
 - Playing songs from youtube
 - Playing custom sounds that users upload
 - Playing a custom sound when a member enters a voice channel
 - Playing a custom sound when a member leaves a voice channel
 - Tracks the total playtime/session for games that members are playing
 - Get a list of the total time played for an individual game
 - Get a list of the total playtime for all games 
 - Creating a list of games and conducting a private election
 - Slash command to get an embedded list of games separated by pages and can be interacted with.

## Planned Features
 - Moving to an external database instead of SQLite
 - Adding a soundboard slash command
 - Adding a "Wrapped" feature that sums up the play sessions for the year

## Copyright

Vincent Scavetta – [@scavettapps](https://twitter.com/scavettapps) – scavettapps@gmail.com

## Contributing

1. Fork it (<https://github.com/scavet64/NoiseBean/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Commit your changes (`git commit -am 'Add some fooBar'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request