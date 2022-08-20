# Game-of-life
Conway's Game of Life implemented with https://libgdx.com/
![image](https://user-images.githubusercontent.com/47903664/185724909-76832540-7bce-4d7e-a4ce-90b234d9c490.png)

# Options
Color: color of live cells. <br>
Ticks/sec: the maximum game ticks calculated per second. <br>
Show grid: toggle the grid which shows the outlines of each cell. <br>
Show chunks: toggle the grid which shows the outlines of each active chunk. <br>
Reset: delete all cells and chunks. <br>
Start/stop: start or stop the sim.

# Mechanics
## Chunks
This implementation does not have a statically sized grid. Instead, it has an infinite plane. To do this, it uses chunks, similar to Minecraft. 
When a live cell is detected in the border region of a chunk, the adjacent chunk is created. When a chunk has no live cells, it is deleted.
![image](https://im5.ezgif.com/tmp/ezgif-5-9c284c998f.gif)
