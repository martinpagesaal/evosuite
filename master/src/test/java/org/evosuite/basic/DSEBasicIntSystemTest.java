package org.evosuite.basic;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.PrintStream;
import java.util.List;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.DSE;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.examples.with.different.packagename.dse.BasicInt;

public class DSEBasicIntSystemTest extends SystemTestBase {

	@Before
	public void setUpProperties() {
		Properties.RESET_STATIC_FIELDS = true;
		Properties.RESET_STATIC_FIELD_GETS = true;
		Properties.SANDBOX = true;
		Properties.JUNIT_CHECK = true;
		Properties.JUNIT_TESTS = true;
		Properties.PURE_INSPECTORS = true;
		Properties.CLIENT_ON_THREAD = true;
		Properties.STRATEGY = Strategy.DSE;
		Properties.ALGORITHM = Algorithm.DSE;
		Properties.CRITERION = new Criterion[] { Criterion.BRANCH };
		Properties.TIMEOUT = 5000000;
		Properties.CONCOLIC_TIMEOUT= 5000000;
		Properties.LOG_LEVEL = "debug";
		Properties.PRIMITIVE_POOL = 0.0;
		LoggingUtils.changeLogbackFile(LoggingUtils.getLogbackFileName());
	}

	@Test
	public void testDSENumbers() {
		
		EvoSuite evosuite = new EvoSuite();

		String targetClass = BasicInt.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		LoggingUtils.getEvoLogger().info("\n\nResult Info\n" + ((List<List<TestGenerationResult>>)result).get(0).get(0));
		DSE<?> dse = (DSE<?>)getGAFromResult(result);
		TestSuiteChromosome best = dse.getTestSuiteChromosome();
		LoggingUtils.getEvoLogger().info("EvolvedTestSuite:\n" + best);
		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming single fitness function
		
		LoggingUtils.getEvoLogger().info("\n\nDone Test suite : Goal =" + goals);
//		Assert.assertEquals("Wrong number of goals: ", 3, goals);
//		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		
	}
}
