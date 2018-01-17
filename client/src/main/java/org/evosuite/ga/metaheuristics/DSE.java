package org.evosuite.ga.metaheuristics;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DSE<T extends Chromosome> extends GeneticAlgorithm<T> {

	private static final Logger logger = LoggerFactory.getLogger(DSE.class);
	private TestSuiteChromosome testSuiteChromosome;
	
	public DSE(ChromosomeFactory<T> factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void evolve() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializePopulation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void generateSolution() {
		// TODO Auto-generated method stub
		
	}
	
	public void setTestSuiteChromosome(TestSuiteChromosome test) {
		this.testSuiteChromosome = test;
	}
	
	public TestSuiteChromosome getTestSuiteChromosome() {
		return this.testSuiteChromosome;
	}

}
