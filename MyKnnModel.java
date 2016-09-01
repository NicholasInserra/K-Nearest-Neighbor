import java.util.List;
import java.util.Arrays;
import java.util.*;

public class MyKnnModel implements java.io.Serializable {
  
  float[][] trainData = new float[1][1];
  int[] classIndex = {};
  private int theK = 0;
  private int theP = 2;
  
  public MyKnnModel() {}
  
  public MyKnnModel(float[][] set, int[] i, int myK, int myP)
  {
    trainData = set.clone();
    classIndex = i.clone();
    theK = myK;
    theP = myP;
  }
  
  public int[] getClassIndex() 
    {
        return classIndex;
    }
  
  public float [] getTrain(int x)
  {
         return trainData[x];
  }
  
  //returns the number of samples
    public int numbSamples()
  {
         return trainData.length;
  }
 
 //geter for K number of neihgbers
    public int getK()
  {
         return theK;
  }
  
  //sets kf for number of neighbors
    public void setK(int k)
  {
        this.theK = k;
  }
  
  //get P number of attribbutes
        public int getP()
  {
         return theP;
  }
  //set P number of attributes
    public void setP(int p)
  {
        this.theP = p;
  }
}
