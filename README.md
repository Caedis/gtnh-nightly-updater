
## GTNH Nightly Updater
A tool for updating the GTNH modpack to the latest nightly version via the GTNH maven.

### Requirements
Java: Version 21 or later.


### Usage
#### Command-Line Options
|Option|Description|  
|---|---|
| -s, --side|Required. Specify the side (CLIENT or SERVER).|  
|-m, --minecraft|Required. Path to the target Minecraft directory.  
|-l, --latest|Optional. Use the latest release instead of the specific nightly version.|  
|-S, --symlinks|Optional. Use symlinks instead of copying mods. Mac/Linux only|  

#### Example Command

`java -jar gtnh-nightly-updater.jar -s CLIENT -m "/mnt/games/Minecraft/Instances/GTNH Nightly/.minecraft/" -l`

### Caching
The cache directory can be found at:  
Windows: `%LOCALAPPDATA%\gtnh-nightly-updater\`  
macOS: `~/Library/Caches/gtnh-nightly-updater/`  
Linux: `$XDG_CACHE_HOME/gtnh-nightly-updater` or `~/.cache/gtnh-nightly-updater`  

Cached mods can be found in the `mods` subdirectory.

### Local Assets and Exclusions
Local Asset File:
- File Name: local-assets.txt
- Format: MOD_NAME|SIDE
- List of mods to be included in addition to the nightly mods list. Use mod name from the maven. (Backhand|BOTH, etc) 

Exclusions:  
- File Name: mod-exclusions.txt
- Mods to be excluded from the update process (JourneyMap, etc)
