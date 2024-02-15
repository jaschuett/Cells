package mygdx.cells;
import com.aparapi.*;
import com.aparapi.internal.kernel.KernelManager;
public class CellsKernel extends Kernel{
    
	//public byte[] birth;
	//public byte[] survive;
    //int x;
    //int y;
    @Override 
    public void run(){
     //   x = getGlobalId(0);
      //  y = getGlobalId(1);
        KernelManager.setKernelManager(KernelManager.instance());
    }

    //public int applyRule(int neighbors, int cell) {
	//	if (birth[neighbors] == 1) {return 1;}
		
	//	if (cell == 1) {
	//		if (survive[neighbors] == 1) {return 1;}
	//	}
	//	return 0;
	//}
}
