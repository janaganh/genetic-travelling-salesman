/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tsp;


import com.sun.jmx.snmp.BerDecoder;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import static tsp.Tsp.CITY_NUM;

//to do always start with the same city
/**
 *
 * @author user
 */
public class Tsp {
    
    static final int POP_SIZE = 500;
    static final int CITY_NUM = 26;
    static final int KILL_FACTOR_PERCENT = 20;    
    static final int MAX_CROSSOVER_GENES = 4;
    static final int MIN_CROSSOVER_GENES = 2;
    static final int CROSSOVER_NUM = 1;
    static final int MUTATION_FACTOR_PERCENT = 10;
    static final int GENERATIONS = 5000;
    
    static final int LAST_BEST_GEN = 3000;
    static final int MAX_GEN_LOOP = 2;
    
    static final int AVG_WORST_NODE_NUM = 100;
    static final int AVG_WORST_GENERATION_NUM = 100;
    
    
    private static ArrayList<String> availableGenes; 
    private static ArrayList<Integer> availablePositions; 
    
    private ArrayList<Individual> population = new ArrayList();
    
    private long avgWorstGeneration;
    private long avgWorstGenerationLastPoint;
    
    private int lastGenBestCase;
    private int lastGenBestCaseCount;
    private int actualGenerations;
    
    
    protected void isOptimum(){
        Collections.sort(population);
        Individual i = population.get(0);
        if (i.getDistance() == lastGenBestCase){
            lastGenBestCaseCount++;
        }
        else{
            lastGenBestCaseCount =0;
        }
        lastGenBestCase = i.getDistance();
    }
    
    public static int[][]  distanceMatrix 
            = {
                {0,633,257,91,412,150,80,134,259,505,353,324,70,211,268,246,121},
                {633,0,390,661,227,488,572,530,555,289,282,638,567,466,420,745,518},
                {257,390,0,228,169,112,196,154,372,262,110,437,191,74,53,472,142},
                {91,661,228,0,383,120,77,105,175,476,324,240,27,182,239,237,84},
                {412,227,169,383,0,267,351,309,338,196,61,421,346,243,199,528,297},
                {150,488,112,120,267,0,63,34,264,360,208,329,83,105,123,364,35},
                {80,572,196,77,351,63,0,29,232,444,292,297,47,150,207,332,29},
                {134,530,154,105,309,34,29,0,249,402,250,314,68,108,165,349,36},
                {259,555,372,175,338,264,232,249,0,495,352,95,189,326,383,202,236},
                {505,289,262,476,196,360,444,402,495,0,154,578,439,336,240,685,390},
                {353,282,110,324,61,208,292,250,352,154,0,435,287,184,140,542,238},
                {324,638,437,240,421,329,297,314,95,578,435,0,254,391,448,157,301},
                {70,567,191,27,346,83,47,68,189,439,287,254,0,145,202,289,55},
                {211,446,74,182,243,105,150,108,326,336,184,391,145,0,57,426,96},
                {268,420,53,239,199,123,207,165,383,240,140,448,202,57,0,483,153},
                {246,745,472,237,528,364,332,349,202,685,542,157,289,426,483,0,336},
                {121,518,142,84,297,35,29,36,236,390,238,301,55,96,153,336,0}
             };
   
    //current generataion number
    private int generation;
    
    static {
                  
        /*
        java.util.Random random = new java.util.Random();
        for(int i = 0 ;i < CITY_NUM; i++ ){
            for(int j = 0; j < CITY_NUM; j++){
                distanceMatrix[i][j] = random.nextInt(100);
            }
        }
        */
       //gene always starts from 0
       availableGenes = new ArrayList(); 
       availablePositions = new ArrayList(); 
       for(int i = 1 ;i < CITY_NUM; i++ ){
           availableGenes.add(String.valueOf(i));
           availablePositions.add(new Integer(i));
       }
        
    }
    
    protected static void loadData(){
        
        System.out.println("Loading Data");
        RandomAccessFile file = null;
        File f = null;
        try
        {
            String path = getPath()+"/cities26.txt";
            f = new File(path);
            if (!f.exists()){
                return;
            }
            distanceMatrix = new int[CITY_NUM][CITY_NUM];
            file = new RandomAccessFile(path, "r");
            String s= null;
            int i = 0,j=0;
            boolean dataFound = false;
            while( (s = file.readLine()) != null ){
                dataFound = false;
                String[] distances  = s.split(" ");
                j=0;
                for(int x = 0; x < distances.length ; x++){
                    if (!distances[x].trim().equals("")){
                        distanceMatrix[i][j] = Integer.parseInt(distances[x]);                        
                        dataFound = true;
                        j++;
                    }
                }
                if (dataFound){
                    i++;
                }
            }
            //print data
            /*
            for(int a = 0; a < CITY_NUM ; a++ ){
                for (int b = 0; b < CITY_NUM ; b++) {
                    System.out.print(distanceMatrix[a][b]);                    
                }
                System.out.println();
            }*/
        
        }catch(Exception ex){
            ex.printStackTrace();
        }
        finally{
            try {
                if (file != null)
                    file.close();
            }catch(Exception ex){}
        
        }
        
        
        
    }
    protected String[] createChromosome(){
        ArrayList<String> genes =  (ArrayList)availableGenes.clone();
        Collections.shuffle(genes, new Random(System.nanoTime()));
         //gene always starts from 0
        genes.add(0, "0");
        return genes.toArray(new String[CITY_NUM]);        
    }
    
    public void generate(boolean initFromFile) throws Exception {
        if (initFromFile){
            recover();
        }else{
            for(int i = 0; i< POP_SIZE ; i++)
            {            
                boolean individualCreated = false;
                while (!individualCreated) {                                                                      
                    Individual individual = new Individual();
                    individual.chromosome = this.createChromosome();
                    if (this.validateIndividual(individual)){
                        population.add(individual);
                        individualCreated = true;
                    }
                    else{
                        individual = null;
                    }
                }    
            }
            this.backup();
          }
        
     }        
    
    protected static  String getPath(){
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        return s;
    }
    protected void writeGraphData(){
      java.io.BufferedWriter writer = null;  
      try{          
          File f = new File(getPath()+"/graph.log"); 
          boolean writeHeader = false;
          if (!f.exists()){
              f.createNewFile();
              writeHeader = true;
          }
          writer = new BufferedWriter(new java.io.FileWriter(f,true));
          if (writeHeader){
            writer.write("##;##");
            writer.newLine();
            writer.write("@LiveGraph demo file.");
            writer.newLine();
            writer.write("Generation;Best;Average;Avg.worst-"+AVG_WORST_NODE_NUM+";Avg.worst-in-Generations "+AVG_WORST_GENERATION_NUM);
            writer.newLine();
            

          }  
          long best = 0,worst = 0, average = 0,avgWorstNum=0,avgWorstGen = 0;
          
          Collections.sort(this.population);
          best = this.population.get(0).getDistance();
          worst = this.population.get(this.population.size()-1).getDistance();
          int s = this.population.size();
          for ( int i = 0 ; i < s ; i++ ){
              Individual individual = this.population.get(i);
              int d = individual.getDistance();
              average += (long)d;
              if ( (s - AVG_WORST_NODE_NUM) < i){
                  avgWorstNum += d;
              }              
          }
          avgWorstNum = (long) (avgWorstNum/ AVG_WORST_NODE_NUM);          
          average = average/this.population.size();
          avgWorstGeneration += (long)worst; 
          boolean isGen = false;
          if ( (this.generation % AVG_WORST_GENERATION_NUM) == 0 && this.generation != 0){
              avgWorstGen = (long)(avgWorstGeneration/AVG_WORST_GENERATION_NUM);
              avgWorstGenerationLastPoint = avgWorstGen;
              avgWorstGeneration = 0;
              isGen = true;
          }
         if (isGen) 
            writer.write(String.valueOf(this.generation)+";"+String.valueOf(best)+";"+String.valueOf(average)+";"+String.valueOf(avgWorstNum)+";"+String.valueOf(avgWorstGen));           
         else
            writer.write(String.valueOf(this.generation)+";"+String.valueOf(best)+";"+String.valueOf(average)+";"+String.valueOf(avgWorstNum)+";"+String.valueOf(avgWorstGenerationLastPoint));            
         writer.newLine();          
         writer.close();
          
      }catch(Exception ex){
          ex.printStackTrace();
      }
      finally{
          try{
            if (writer != null)
                writer.close();
          }catch(Exception ex){}  
      }
    }
    protected void backup()
    {
       ByteArrayOutputStream bos = new ByteArrayOutputStream();
       ObjectOutputStream out = null;  
       RandomAccessFile backupFile = null;
        try 
        {
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            backupFile = new RandomAccessFile(s+"/tsp_pop.dmp","rw");
            out = new ObjectOutputStream(bos);   
            out.writeObject(this.population);
            byte[] objectBytes = bos.toByteArray();
            backupFile.seek(0);
            backupFile.write(objectBytes);
        } 
        catch(Exception ex){
            ex.printStackTrace();
        }
        finally {
          try {
            if (out != null) {
              out.close();
            }
            if (bos != null){
                bos.close(); 
            }    
            if (backupFile != null){
                backupFile.close();
            }
          } catch (Exception ex) {
            // ignore close exception
          }
       }
    }
    protected void recover() throws Exception {
        
        ByteArrayInputStream bis = null;
        ObjectInput in = null;
        try {
          Path currentRelativePath = Paths.get("");
          String s = currentRelativePath.toAbsolutePath().toString();          
          File f = new File(s+"/tsp_pop.dmp");
          if (f.exists()){
            bis = new ByteArrayInputStream(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
            in = new ObjectInputStream(bis);
            this.population = (ArrayList) in.readObject(); 
          }else{
            new Exception("File not exist!");
          } 
        }catch(Exception ex){
            ex.printStackTrace();
            throw ex;
        } 
        finally {
          try {
            bis.close();
          } catch (IOException ex) {
            // ignore close exception
          }
          try {
            if (in != null) {
              in.close();
            }
          } catch (IOException ex) {
    // ignore close exception
         }
       }
    }
    
    public boolean validateIndividual(Individual individual)
    { 
        if(population.contains(individual)){
            return false;
        }               
        return this.validGene(individual.chromosome);
    }
    
    protected boolean validGene(String[] chromosome){
        if (chromosome.length != CITY_NUM){
            return false;
        } 
        //check for duplicated gene sequence
        HashMap geneMap = new HashMap();
        for(int i = 0; i < chromosome.length; i++){
            if (geneMap.containsKey(chromosome[i])){
                return false;
            }
            geneMap.put(chromosome[i], null);
        }
        return true;
    }
    
    public int evaluate(Individual individual){
       return individual.getDistance();
    }
   
    protected void mutate(){
        int numToMutate = (int) (population.size() * KILL_FACTOR_PERCENT/100 ); 
        ArrayList<Individual> randomPopulation = (ArrayList)this.population.clone();
        Collections.shuffle(randomPopulation, new Random(System.nanoTime()));
        Individual individual = null;
        int index = -1;
        for(int i = 0; i < numToMutate;){
            if (individual == null){
                individual = randomPopulation.get(i);
                index = population.indexOf(individual);                
            }    
            individual = mutateIndividual(individual);
            if (this.validateIndividual(individual)){                
                i++;
                population.add(index, individual);
                individual = null;
            }
                                
        }    
    }
    
    protected Individual mutateIndividual(Individual individual)
    {               
        ArrayList<Integer> positions = (ArrayList)availablePositions.clone();
        Collections.shuffle(positions, new Random(System.nanoTime()));
        int position1 = positions.get(0);
        int position2 = positions.get(1);
        //swap
        individual = (Individual)individual.clone();
        String tmp = individual.chromosome[position2];
        individual.chromosome[position2] = individual.chromosome[position1];
        individual.chromosome[position1] = tmp;          
        return individual;
    }
    
    protected void breed(){
        int numToBreed = POP_SIZE - population.size();
        ArrayList<Individual> parents = (ArrayList)this.population.clone();
        Collections.shuffle(parents, new Random(System.nanoTime()));
        for(int i = 0; i < numToBreed ;){            
            Individual parent1 = parents.get(i);
            Individual parent2 = parents.get(i+1);
            Individual child =  crossover(parent1, parent2);
            if (this.validateIndividual(child)){                
                i+=2;
                population.add(child);
            }
            
        }              
    }
    
   
    protected Individual crossover(Individual parent1,Individual parent2)
    {
        int selectedParent   = new Random().nextInt(2);        
        Individual child = null,parent = null;
        if (selectedParent == 0){
            child = (Individual) parent1.clone();
            parent = parent2;
        }
        else{
            child = (Individual) parent2.clone();
            parent = parent1;
        }
       
        for (int x = 0; x < CROSSOVER_NUM ; x++) 
        {
            int selectedGenesNum = new Random().nextInt((MAX_CROSSOVER_GENES - MIN_CROSSOVER_GENES) + 1) 
                                  + MIN_CROSSOVER_GENES;
            if ((selectedGenesNum % 2) != 0  ){
                selectedGenesNum = (int)(selectedGenesNum/2)*2;
            }
            //crossover process
            ArrayList<String> genes = (ArrayList)availableGenes.clone();
            Collections.shuffle(genes,new Random(System.nanoTime()) );
            for (int i = 0; i < selectedGenesNum; i+=2 ){            

              //index mapping from parent
              String searchKey1 = genes.get(i).toString();
              String searchKey2 = genes.get(i+1).toString();

              int position1  = -1;
              int position2  = -1;

              for (int j = 0;j < parent.chromosome.length ; j++){
                  if (searchKey1.equals(parent.chromosome[j])){
                      position1 = j;
                  }
                  else if (searchKey2.equals(parent.chromosome[j])){
                      position2 = j;
                  }
              }
              //swap
              String tmp = child.chromosome[position2];
              child.chromosome[position2] = child.chromosome[position1];
              child.chromosome[position1] = tmp;          
            }
        }
        return child;
    }
    /*
    protected String[] createOffspringChromosome(String[] parent1Chromosome,String[] parent2Chromosome){
        
        String[] newChromosome = new String[CITY_NUM];
       
        for(int i = 0; i < CROSSOVER_POSITION; i++){           
            newChromosome[i] = parent1Chromosome[i];                                 
        }
        for(int i = CROSSOVER_POSITION; i < CITY_NUM; i++){           
            newChromosome[i] = parent2Chromosome[i-CROSSOVER_POSITION];            
        }
        return newChromosome;
    } 
    */
    public void kill(){
        Collections.sort(this.population);
        int size = this.population.size();
        int numToKill = (int) (size * KILL_FACTOR_PERCENT/100 ); 
        List list = this.population.subList(0,POP_SIZE - numToKill);
        this.population = null;
        this.population = new ArrayList<>();
        this.population.addAll(list);                       
    }
    
  
    /**
     * @param args the command line arguments
     */
    
    protected static void printPop(Tsp tsp,String msg){
        System.out.println(msg);
        Collections.sort(tsp.population);
        Individual best = tsp.population.get(0);
        /*for (Iterator it = tsp.population.iterator();it.hasNext();) {
            Individual i = (Individual)it.next();
            i.printChromosome();
        }*/        
        System.out.println("Best Solution: ");
        best.printChromosome();
    }
    
   
    
    public void run(boolean recover) throws Exception{
        //graph data
        
        
        File f = new File(getPath()+"/graph.log");
        if (f.exists()){
            f.delete();
        }
        generate(recover);
        writeGraphData();
        printPop(this,"Initial");
        while (this.generation < GENERATIONS) {            
            this.generation++;
            kill();            
            breed();            
            mutate();
            printPop(this,"Generation : " + this.actualGenerations);
            writeGraphData();
            isOptimum();
            this.actualGenerations++;
            if (this.lastGenBestCaseCount > LAST_BEST_GEN){
                int  loops =  (int)this.actualGenerations/GENERATIONS;
                System.out.println("Loops Count :"+loops);
                if ( loops > MAX_GEN_LOOP){
                    break;
                }
                f = new File(getPath()+"/graph.log");
                if (f.exists()){
                    f.delete();
                }
                this.population.clear();
                generate(false);
                writeGraphData();
                lastGenBestCase = 0;
                lastGenBestCaseCount = 0;
                this.generation = 0;
            }
        }    
    } 
    public static void main(String[] args) throws Exception {
            
        boolean recover = args.length > 0;
        //main       
        Tsp t = new Tsp();        
        loadData();
        t.run(false);
        
        // TODO code application logic here
        /*
        Tsp t = new Tsp();        
        t.generate(false);                
        printPop(t,"before kill population Generated");
        t.kill();
        printPop(t,"after kill population Generated");
        t.breed();
        printPop(t,"after breeding");
        t.mutate();
        printPop(t,"after mutating");
        */
        //check mutation
        /*
        Tsp t = new Tsp();
        Individual i1 = new Individual();        
        i1.chromosome = new String[]{"0","1","2","3","4"};        
        i1.printChromosome();
        Individual i2= t.mutateIndividual(i1);
        i2.printChromosome();
        */
        
        /*
        //crossover test
        Tsp t = new Tsp();
        Individual i1 = new Individual();
        Individual i2 = new Individual();
        i1.chromosome = new String[]{"0","1","2","3","4"};
        i2.chromosome = new String[]{"4","2","0","1","3"};
        Individual i3= t.crossover(i1, i2);
        i1.printChromosome();
        i2.printChromosome();
        i3.printChromosome();
        */
        /*
        //unique test
        Individual i1 = new Individual();
        Individual i2 = new Individual();
        i1.chromosome = new String[]{"0","1","2","3","4"};
        i2.chromosome = new String[]{"4","3","2","1","0"};
        if (i1.equals(i2)){
            System.out.println("equal");
        }
        else{
            System.out.println("not equal");
        }
        
        i1.chromosome = new String[]{"0","1","2","3","4"};
        i2.chromosome = new String[]{"0","1","2","3","4"};
        if (i1.equals(i2)){
            System.out.println("equal");
        }
        else{
            System.out.println("not equal");
        }
        
        i1.chromosome = new String[]{"4","1","2","3","0"};
        i2.chromosome = new String[]{"2","1","0","3","4"};
        if (i1.equals(i2)){
            System.out.println("equal");
        }
        else{
            System.out.println("not equal");
        }
        */
        
        //backup check
        /*
        Tsp t = new Tsp();
        t.generate(false);
        printPop(t, "generated");
        t.population = new ArrayList();
        t.generate(false);
        printPop(t, "without rover");
        t.population = new ArrayList();
        t.generate(recover);
        printPop(t, "with recover");
        */
       
        
    }
    
} 
   

  class Individual implements Comparable<Individual>, Serializable, Cloneable
  {
   
        public String chromosome[];

        public boolean equals(Object o)
        {
            boolean equvalent = false;
            if (o != null && o instanceof Individual)
            {
                Individual other = (Individual)o;
                String[] otherChromosome = other.chromosome;
                equvalent = true;
                
                for(int i = 0; i < chromosome.length ;i++){
                    if (!chromosome[i].equals(otherChromosome[i])){
                        equvalent = false;
                        break;
                    }                    
                }
                if (!equvalent){
                    equvalent = true;
                    for(int i = 0; i < chromosome.length ;i++){
                        if (!chromosome[i].equals(otherChromosome[Tsp.CITY_NUM-i-1])){
                            equvalent = false;
                            break;
                        }                    
                    }                    
                }                
            }    
            return equvalent;
        }

        public int getDistance(){
            int distance = 0;
            for(int i = 0; i < (Tsp.CITY_NUM-1) ;++i){            
                 distance += tsp.Tsp.distanceMatrix[Integer.parseInt(this.chromosome[i])][Integer.parseInt(this.chromosome[i+1])];
            } 
            distance += tsp.Tsp.distanceMatrix[Integer.parseInt(this.chromosome[this.chromosome.length - 1])][Integer.parseInt(this.chromosome[0])];
            return distance;
        }

        public int compareTo(Individual o){
            int selfDistance = this.getDistance();
            int otherDistance = o.getDistance();
            if (selfDistance > otherDistance){
                return 1;
            }
            else if (selfDistance == otherDistance){
                return 0;
            }       
            return -1;
        }


        public void printChromosome(){            
            for(int i = 0; i < chromosome.length ;i++){            
                System.out.print(chromosome[i]);
                System.out.print(",");
            }
            System.out.print(" - "+this.getDistance());
            System.out.println();
        }
        
         public Object clone() 
         {                         
             Individual i = new Individual();
             i.chromosome = (String[])this.chromosome.clone();
             return i;   
          }
    }


