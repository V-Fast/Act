# Act
<sup>Act as if you had friends... But you are in a singleplayer world.</sup>

<!-- image later -->

[![Only available on Modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png)](https://modrinth.com/mod/acts)
[![Requires Fabric API](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/requires/fabric-api_64h.png)](https://modrinth.com/mod/fabric-api)
![Won't Support Forge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/unsupported/forge_64h.png)  
[![Support Discord](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-singular_46h.png)](https://discord.gg/Rqpn3C7yR5)


## Description
Create Actors (also called "NPCs") using the `/actor [Player Name]` command. This will maybe change in the future.\
Actors can follow you if you right-click using the **Follow Stick**. You can make them stop at any time by using the **Follow Stick** again!

**WARNING**: This new version ([1.2.0](https://modrinth.com/mod/act/version/1.2.0)) includes a new artificial intelligence, it might break or crash sometimes! If it does occur, please [fill an issue](https://github.com/lumaa-dev/Act/issues/new) unless it's already [listed](#known-bugs)

## How can Act be used?
Act can be used to feel less lonely (this depends on people), take screenshots with persons you could never take screenshots with! Or just have fancy statues.

In the future, Actors will be able to move using a user-defined path. or defend players from mobs!

## Actor AI
Actor AI is what powers an Actor's movement, it uses hard equations and conditions to make an Actor go from one place to another. Actor AI doesn't use any of Minecraft's AI.\
Actor AI is moving from axis to axis (Y axis is still bugged) instead of moving blocks to blocks or just going forward.

## Build
To build the mod, you need to have [git](https://git-scm.com/downloads).\
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
- Actor entities do not save
- Actor entity does not have knockback when damaged by entity or player
- `/actor` "*ign*" parameter is not case-sensitive
- TPS drops to 0 when loading Actor
- Actor AI sometimes makes the entity turn on itself (then continues)
- Actor AI has a hard time with obstacles, and blocks that aren't at his level

