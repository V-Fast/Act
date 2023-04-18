<center><div align="center">

# Act
<sup>Act as if you had friends... But you are in a singleplayer world.</sup>

<!-- Put a showcase image if you can/want -->

<img src="https://raw.githubusercontent.com/lumaa-dev/lumaa-dev/main/assets/fabric-banner.png" width=600>

<a href="https://github.com/lumaa-dev/Act"><img src="https://raw.githubusercontent.com/lumaa-dev/art/main/badges/star_github.png" width=150></a>
<a href="https://modrinth.com/mod/acts"><img src="https://raw.githubusercontent.com/lumaa-dev/art/main/badges/only_modrinth.png" width=150></a>
<a href="https://discord.gg/Rqpn3C7yR5"><img src="https://raw.githubusercontent.com/lumaa-dev/art/main/badges/support_discord.png" width=150></a>
<a href="https://docs.google.com/spreadsheets/d/1zrBJshX48qSnxicYFW-AIy_CmrKOT0d91QpU24vDvdQ/edit?usp=sharing"><img src="https://raw.githubusercontent.com/lumaa-dev/art/main/badges/modrinth_stats.png" width=150></a>

</div></center>

## Description
Create NPCs (also called "Actors") using the `/npc [Player Name]` command. This will change in the future.

## How can Act be used?
Act can be used to feel less lonely (this depends on people), take screenshots with persons you could never take screenshots with! Or just have fancy statues.

In the future, Actors will be able to move using a user-defined path. or defend players from mobs!

## Build
To build the mod, you need to have [git](https://git-scm.com/downloads).  
After you installed git, create a new directory and open a command prompt in that directory then type `git clone https://github.com/lumaa-dev/Act.git`.  

Once done, type in the command prompt the following lines in order:
- `cd ./Act-master`
- `chmod +x ./gradlew` - Allows the access to the `gradlew` file
- `./gradlew build`. - Builds the .jar file

Once the loading is done:
- Find the mod in *Act-master/build/libs/act-[VERSION].jar*
- Copy and paste in *.minecraft/mods*
- Install the latest version of [Fabric API](https://modrinth.com/mod/fabric-api)

## Known Bugs
- NPC entity only appears after a death event
- NPC entities do not save
- NPC entity does not have knockback when damaged by entity or player
- `/npc` "*ign*" parameter is not case sensitive

<!-- modrinth_exclude.start -->
## License
This mod is under the [MIT License](/LICENSE).
<!-- modrinth_exclude.end -->