# Added
Saving & Loading feature - Actors are now saved in the world's folder as ***ActorData.nbt***\
Placing blocks

Data being saved:
- Actor name and UUID
- Actor position and head orientation
- Actor inventory (*not selected slot*)

# Changed
**Actor AI**:
- Actors can now jump on blocks and go down blocks (still a little buggy)

# Fixed
- Actor entities do not save
- Actor AI has a hard time with blocks that aren't at his level

* * *

## Known Bugs
- TPS drops to 0 when loading Actor
- Actor entity does not have knockback when damaged by entity or player
- Actor AI sometimes makes the entity turn on itself (then continues)
- Actor AI has a hard time with obstacles
- Actor cannot fully break a block