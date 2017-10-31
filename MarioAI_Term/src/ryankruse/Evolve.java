package ryankruse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import ch.idsia.agents.Agent;
import ch.idsia.agents.MLPESLearningAgent;
import ch.idsia.agents.learning.LargeMLPAgent;
import ch.idsia.agents.learning.MediumMLPAgent;
import ch.idsia.agents.learning.SimpleMLPAgent;
import ch.idsia.agents.learning.SmallMLPAgent;
import ch.idsia.benchmark.mario.engine.GlobalOptions;
import ch.idsia.benchmark.tasks.BasicTask;
import ch.idsia.benchmark.tasks.GamePlayTask;
import ch.idsia.benchmark.tasks.ProgressTask;
import ch.idsia.benchmark.tasks.Task;
import ch.idsia.evolution.Evolvable;
import ch.idsia.evolution.ea.ES;
import ch.idsia.tools.MarioAIOptions;
import ch.idsia.utils.wox.serial.Easy;

public class Evolve {

	final static int generations = 200;
	final static int populationSize = 300;

	static Evolvable[] bestEntities;
	static float[] bestFitnesses;
	
	static int nFinishes = 0;
	static final int mustFinish = 5;
	
	public static void main(String[] args)
	{
		MarioAIOptions options = new MarioAIOptions(args);
	    List<Agent> bestAgents = new ArrayList<Agent>();
	    DecimalFormat df = new DecimalFormat("0000");
	    
	    Evolvable initial = new SmallMLPAgent();
	    SmallMLPAgent best = new SmallMLPAgent();
	    
	    Genetic es;
	    Task task;
	    double topResult = -1;
	    double lastFitness = -1;
	    int difficulty = 0;
	    int repeatCount = 0;
	    
	    
        while(difficulty < 11){
    	    System.out.println("New evolve difficulty: " + difficulty);

        	options.setLevelDifficulty(difficulty);

            options.setFPS(GlobalOptions.MaxFPS);
            options.setVisualization(false);
            
            //Setup the initial population per each level
            if(difficulty == 0 && topResult < 0){ //first level first run
                options.setAgent((Agent) best);
                task = new ProgressTask(options);
                es = new Genetic(task, best, populationSize);
            }else{ //nth level first run
            	System.out.println("Expanding on old gen");
            	options.setAgent((Agent) best);
                task = new ProgressTask(options);
                es = new Genetic(task, bestEntities, bestFitnesses, populationSize);
            }
            
	        for (int gen = 0; gen < generations; gen++){
	            es.nextGeneration(); //Doing a lot of crossover here...will make it super slow
	            double bestResult = es.getBestFitnesses()[0];
	            
	            System.out.println("Generation " + gen + " best " + bestResult);
	            
	            options.setVisualization(bestResult > lastFitness || bestResult > 4000);
	            
	            if(lastFitness == bestResult){
	            	repeatCount++;
	            }else{
	            	repeatCount = 0;
	            }
	            
	            if(repeatCount >= 20){//was 10
	            	System.out.println("MUTATING SOME!");
	            	es.mutateSome();
	            	repeatCount = 0;
	            }
	            
	            lastFitness = bestResult;
	            
	            //options.setVisualization(gen % 5 == 0 || bestResult > 4000);
	            
	            Agent a = (Agent) es.getBests()[0];
	            best = (SmallMLPAgent) a;
	            a.setName(((Agent) initial).getName() + df.format(gen));
	            bestAgents.add(a);
	            
	            //best = (SmallMLPAgent) es.getRandomBest()[0];
	            
	            double result = task.evaluate(a);
	            options.setVisualization(false);
	            
	            if(result > topResult){
	            	topResult = result;
	            	repeatCount = 0;
	            }else if(result == topResult){
	            	repeatCount++;
	            }
	            
	            
	            
	            if(result > 4000 && nFinishes >= mustFinish){ //we must complete the level at least 5 times to conin
	            	difficulty++;
	            	break;
	            }else if(result > 4000){
	            	nFinishes++;
	            }
	        }
	        //END OF LEVEL SO GET THE BEST STUFF
	        
	        bestEntities = es.getNBest(populationSize / 3);
	        bestFitnesses = es.getNFitness(populationSize / 3);
        }
	}
}
