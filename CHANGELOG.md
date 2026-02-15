# v1.4.1
### Bug Fixes
* Fixed an issue that would cause the eye trails to turn into lazers when shaders were enabled

### Chores
* Updated muks eye textures to be in line with the new model

# v1.4
### Bug Fixes
* Fixed an issue that could cause the game to crash if an alpha that didn't exist attempted to spawn #5
### New Configs
* Added a config option to turn off the spawn message #4
* Added a config option to do a global spawn or a per player spawn attempt
* Added a config option to change the boost extra player players give to the spawn chance
* Added a config option to determine whether an Alpha should keep it's eyes after being caught
### New Features
* Alpha pokemon now spawn with a random tutor move #3
* Added alpha eyes for gen1 and gen2 pokemon
* Added a glowing tracer effect to alpha eyes
* Added the Alpha mark to "former alpha pokemon"
* The "wild might" message is now displayed in the battle chat
* Mega Showdown Support
### Refactors
* Reworked the README
### Removals
* Removed wild might particles
* Removed wild might sound effect
* Removed alpha particles
* Removed super alpha particles
### Development
* Set up gradlew
* Set up a justfile
* Wild might message is now under `assets/cobblemon/lang`
### Known issues
### WIP
* Alpha eyes for gen3 through gen10
* Eye anchors for all gens