# Cells
Cellular automaton tool implemented with [libGDX](https://libgdx.com/)

![image](https://user-images.githubusercontent.com/47903664/190571113-ba8f2fbe-f4e8-43eb-9eb7-730b29e784e9.png)


# Options
Color: color of live cells. <br><br>
Ticks/sec: the maximum game ticks calculated per second. <br><br>
Show grid: toggle the grid which shows the outlines of each cell. <br><br>
Show chunks: toggle the grid which shows the outlines of each active chunk. <br><br>
Reset: delete all cells and chunks. <br><br>
Start/stop: start or stop the sim. <br><br>
Edit rules: open or close the dialog to edit the number of live neighbors required for dead cells to become alive (Birth) and for alive cells to remain alive (Survive). The default is Conway's game of life: cells are born if they have 3 neighbors, and survive if they have 2 or 3 neighbors (notated as B3/S23). 

![cells](https://user-images.githubusercontent.com/47903664/190576531-aae7e82b-397c-4a3a-af48-ee97cb6770bc.gif)

# Rulesets (found [here](https://en.wikipedia.org/wiki/Life-like_cellular_automaton#A_selection_of_Life-like_rules)).
## Day & Night (B3678/S34678)
Symmetric under on-off reversal.
![daynnight](https://user-images.githubusercontent.com/47903664/190583461-83ec3f61-dd7c-46b3-8247-d3fece42f433.gif)

## Replicator (B1357/S1357)
Every pattern is replaced by copies of itself.
![replicator](https://user-images.githubusercontent.com/47903664/190586504-44092ce4-f228-4971-ad7f-c708bd90c496.gif)



# Mechanics
## Chunks
This implementation does not have a statically sized grid. Instead, it has an infinite plane. To do this, it uses chunks, similar to Minecraft. 
When a live cell is detected in the border region of a chunk, the adjacent chunk is created. When a chunk has no live cells, it is deleted.
