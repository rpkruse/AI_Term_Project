package ryankruse;


import ch.idsia.agents.Agent;
import ch.idsia.agents.learning.SmallMLPAgent;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.EA;
import ch.idsia.evolution.Evolvable;

public class Genetic implements EA{

	public static boolean finishedFirst = false;
	
	private final Evolvable[] population;
	private final float[] fitness;
	private final int numElite;
	private final Task task;
	private final int evaluationRepetitions = 1;
	private final double mutateChance = .25f;
	private final double crossOverChance = .45f;
	
	public Genetic(Task task, Evolvable initial, int populationSize){
	    this.population = new Evolvable[populationSize];
	    
	    for (int i = 0; i < population.length; i++){
	        population[i] = initial.getNewInstance();
	    }
	    
	    this.fitness = new float[populationSize];
	    this.numElite = populationSize / 3; //was 2
	    this.task = task;
	    
	    finishedFirst = true;
	}

	public Genetic(Task task, Evolvable[] init, float[] fitness, int populationSize){
		this.population = new Evolvable[populationSize];
		
		for(int i=0; i < populationSize; i++){
			if(i < init.length){
				population[i] = init[i]; //Do we want to mutate each time???
			}else{
				int index = (int) Math.random() * init.length;
				population[i] = init[index].getNewInstance();
			}
		}
		
		
		this.fitness = new float[populationSize];
		for(int i=0; i < fitness.length; i++){
			this.fitness[i] = fitness[i];
		}
		
		this.numElite = populationSize / 3; //was 2
		this.task = task;
	}
	
	public void mutateSome(){
		double rand; 
		for(int i=0; i<population.length; i++){
			rand = Math.random();
			
			if(rand <= crossOverChance){
				population[i] = mutateAndCross();
			}
			
			if(rand <= mutateChance){
				Evolvable e = population[i];
				e.mutate();
			}
		}
	}
	
	public void nextGeneration(){		
		int crossOverCount = 0;
		//Do the best ones first
	    for (int i = 0; i < numElite; i++){
	        evaluate(i);
	    }
	    
	    double rand;
	    for (int i = numElite; i < population.length; i++){
	    	rand = Math.random();
	    	if(rand <= crossOverChance){
				population[i] = mutateAndCross();
				crossOverCount++;
	    	}else{
		    	population[i] = population[i - numElite].copy();
	    	}
	    	
	    	population[i].mutate();
	        evaluate(i);
	    }
	    
	    shuffle();
	    sortPopulationByFitness();
	    System.out.println("Crossed over: " + crossOverCount + " / " + (this.population.length - numElite) + " times");
	}
	
	public SmallMLPAgent mutateAndCross(){
		int index1 = (int) (Math.random() * numElite);
		int index2 = (int) (Math.random() * numElite);
		
		while(index1 == index2)
			index1 = (int) (Math.random() * numElite);
		
		Evolvable father = population[index1].getNewInstance();
		Evolvable mother = population[index2].getNewInstance();
		
		SmallMLPAgent child =  new SmallMLPAgent();
		child.crossOver(((SmallMLPAgent) father).getMLP(), ((SmallMLPAgent) mother).getMLP());
		
		return child;
	}

	private void evaluate(int which){
	    fitness[which] = 0;
	    for (int i = 0; i < evaluationRepetitions; i++){
	        population[which].reset();
	        fitness[which] += task.evaluate((Agent) population[which]);
	    }
	    
	    fitness[which] = fitness[which] / evaluationRepetitions;
	}

	private void shuffle(){
	    for (int i = 0; i < population.length; i++){
	        swap(i, (int) (Math.random() * population.length));
	    }
	}

	private void sortPopulationByFitness(){
	    for (int i = 0; i < population.length; i++){
	        for (int j = i + 1; j < population.length; j++){
	            if (fitness[i] < fitness[j])
	            	swap(i, j);
	        }
	    }
	}

	private void swap(int i, int j){
	    float fSave = fitness[i];
	    fitness[i] = fitness[j];
	    fitness[j] = fSave;
	    
	    Evolvable eSave = population[i];
	    population[i] = population[j];
	    population[j] = eSave;
	}

	public Evolvable[] getBests(){ return new Evolvable[]{population[0]};}

	public float[] getBestFitnesses(){ return new float[]{fitness[0]};}

	public Evolvable[] getRandomBest(){
		int index = (int)Math.random() * this.numElite;
		return new Evolvable[]{this.population[index]};
	}
	
	public Evolvable[] getNBest(int n){
		Evolvable[] best = new Evolvable[n];
		
		for(int i=0; i < n; i++){
			best[i] = this.population[i];
		}
		
		return best;
	}
	
	public float[] getNFitness(int n){
		float[] best = new float[n];
		
		for(int i=0; i < n; i++){
			best[i] = this.fitness[i];
		}
		
		return best;
	}
}
