/****************************************************************
   PROGRAM:   K Nearest Neighbor Algorithm Plugin for BioCat
   AUTHOR:    Nick Inserra
   Z ID:      z1749082
   
   FUNCTION:  Plugin for BioCat interface.  Performs the K
              Nearest Neighbor Algorithm, on image data.  Data
              may be extracted or selected by various methods before.
              Trains a model, and clssifies test cases based on that.
              
   INPUT:     Comes from image data through BioCat interface.
              
              *trainingpatterns  Data for Training
              *trainingtargets   Targets for the training pattern
              *testingpatterns   Pattern data to be classified
              *prob              Storage for probability result


   OUTPUT:    location and type of output, i.e.  a report
              containing a detail record for each city processed
              containing city id, Celsius temperature, Fahrenheit
              temperature and wind chill temperature.

   NOTES:     any relevant information that would be of
              additional help to someone looking at the program.
****************************************************************/

import Jama.*;

import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import annotool.classify.SavableClassifier;
import java.io.PrintStream;
import java.util.List;
import java.util.Arrays;



public class MyKNN implements SavableClassifier
{
  MyKnnModel trainedModel = new MyKnnModel();
  public static int myK = 1;
  public static int myP = 2;
  
  public MyKNN() {}
  
  /*
     * Sets algorithm parameters from para
    *
    * @param   para  Each element of para holds a parameter name
    *                for its key and a its value is that of the parameter.
    *                The parameters should be the same as those in the
    *                algorithms.xml file.
    */
 public void setParameters(java.util.HashMap<String, String> para)
     {
       String Number_Neighbors = "Number_Neighbors";
       
       
       //get number of nearest neihgbors from parameters
       if(para.containsKey(Number_Neighbors))
        {
         myK = Integer.parseInt(para.get(Number_Neighbors));
        }
       if(para.containsKey("pNumber"))
            {
             myP = Integer.parseInt(para.get("pNumber"));
            }
          
     }
     
         /**
     * Trains and returns an internal model using a training set.
     * 
     * @param   trainingpatterns  Pattern data to train the algorithm
     * @param   trainingtargets   Targets for the training pattern
     * @return                    Model created by the classifier
     */
     
     public MyKnnModel trainingOnly(float[][] trainingpatterns, int[] trainingtargets)
     {
       
       int traininglength = trainingpatterns.length;
       int dimension = trainingpatterns[0].length;
       int numClasses = trainingtargets.length;
       
       MyKnnModel trainedModel = new MyKnnModel(trainingpatterns, trainingtargets, myK, myP);
       //MyKNN classifier = new MyKNN();
       
       return trainedModel;
     }

      
 
   /**
    * Gets the internal model from the classifier
    * 
    * @return  Model created by the classifier.
    */
  public MyKnnModel getModel()
  {
    return trainedModel;
  }
  
     /**
    * Classifies the patterns using the input parameters.
    * 
    * @param   trainingpatterns  Pattern data to train the algorithm
    * @param   trainingtargets   Targets for the training pattern
    * @param   testingpatterns   Pattern data to be classified
    * @param   predictions       Storage for the resulting prediction
    * @param   prob              Storage for probability result
    * @throws  Exception         Optional, generic exception to be thrown
    */
   public void classify(float[][] trainingpatterns, int[] trainingtargets, float[][] testingpatterns, int[] predictions, double[] prob) throws Exception
   {
     trainedModel = (MyKnnModel) trainingOnly(trainingpatterns, trainingtargets);
     
     int[] results = classifyUsingModel(trainedModel, testingpatterns, prob);
   
     for (int j=0; j < predictions.length; j++)
       predictions[j] = results[j];
     
     
   }
   
   
   /**
    * Returns whether or not the algorithm uses probability estimations.
    * 
    * @return  <code>True</code> if the algorithm uses probability 
    *          estimations, <code>False</code> if not
    */
   public boolean doesSupportProbability()
   {
     return true;
   }


 
 
   /**
    * Sets an internal model to be used by the classifier
    * 
    * @param   model      Model to be used by the classifier
    * @throws  Exception  Exception thrown if model is incompatible
    */
 public void setModel(java.lang.Object model) throws Exception
 {
   
 }
 
 
   /**
    * Classifies the internal model using one testing pattern
    * 
    * @param   model            Model to be used by the classifier
    * @param   testingPattern   Pattern data to be classified
    * @param   prob             Storage for probability result
    * @return                   The prediction result
    * @throws  Exception        Exception thrown if model is incompatible
    */
 public int[] classifyUsingModel(Object model, float[][] testingPatterns, double[] prob) throws Exception
 { 
   int numToTest = testingPatterns.length;
   int kNeighbors = trainedModel.getK();
   double pNum = trainedModel.getP();
   int [] closestNindex = new int[kNeighbors];
   int [] classResults =  new int[kNeighbors];
   
   trainedModel = (MyKnnModel) model;
   int numbTrainedSamples = trainedModel.numbSamples();
   
   List<Integer> testList = new ArrayList<Integer>();
   
   int [] test = new int[numbTrainedSamples];
   test = trainedModel.getClassIndex();
   float [] distResult = new float[numbTrainedSamples];
   
   for (int i = 0; i < numToTest; i++) //for each to be test sample
   {                                                  
     for (int j = 0; j < numbTrainedSamples; j++)   //test against each trained sample
     {
       distResult[j] = calculateDistance(testingPatterns[i], trainedModel.getTrain(j), pNum);  
       
       if (j == numbTrainedSamples -1) //once you have done all closest and add to testlist
         {
           closestNindex = minNIndex(distResult, kNeighbors);    //get index of closest points
           
           for (int r = 0; r < kNeighbors; r++)
           {  classResults[r] = test[closestNindex[r]];   }    //get class for the nearest neihgbors
           
           testList.add(getPopClass(classResults)); //add most common class
           Arrays.fill(closestNindex, 0);    //empty array
           Arrays.fill(classResults, 0);    //empty array
         }
     }
   }
   

   int[] results = new int[testingPatterns.length];
   
     for (int i = 0; i < testingPatterns.length; i++)
   {
     results[i] = testList.get(i);
   }
   return results;
 }
 
 
  public int classifyUsingModel(Object model, float[] testingPatterns, double[] prob) throws Exception
 {
   return 1;
 }
 
 
     //loads the model
     
 public MyKnnModel loadModel(String model_file_name) throws java.io.IOException
 {
   return trainedModel;
 }

 public void saveModel(Object trainedModel, String model_file_name) throws java.io.IOException
 {
 }
 
 
 //calculate eucliaden distance
 public static float calculateDistance(float[] array1, float[] array2, double p)
    {
        float Sum = 0;
        for(int i=0;i<array1.length;i++) {
           Sum = Sum + (float)Math.pow((array1[i]-array2[i]),(double)p);
        }
        return (float)Math.pow(Sum,1.0/p);
    }
 
 //find the min index for each
public static int[] minNIndex(float[] array, int number) 
{
    float[] min = new float[number];
    int[] minIndex = new int[number];
    Arrays.fill(min, 10000);
    Arrays.fill(minIndex, -1);

    top: for(int i = 0; i < array.length; i++) {
        for(int j = 0; j < number; j++) {
            if(array[i] < min[j]) {
                for(int x = number - 1; x > j; x--) {
                    minIndex[x] = minIndex[x-1]; min[x] = min[x-1];
                }
                minIndex[j] = i; min[j] = array[i];
                continue top;
            }
        }
    }
    return minIndex;
}

// Finds the most popular class and returns the index

public int getPopClass(int[] a)
{
  int count = 1, tempCount;
  int popular = a[0];
  int temp = 0;
  
  //for each one
  for (int i = 0; i < (a.length - 1); i++)
  {
    temp = a[i];
    tempCount = 0;
    
    //for the entire array
    for (int j = 1; j < a.length; j++)
    {
      if (temp == a[j])     
        tempCount++;
    }
    if (tempCount > count)     //if its the most popular 
    {
      popular = temp;         //sotres index
      count = tempCount;      //sotres max count
    }
  }
  return popular;
} 


}
