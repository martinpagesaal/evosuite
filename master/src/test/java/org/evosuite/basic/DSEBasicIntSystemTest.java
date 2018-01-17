package org.evosuite.basic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.Strategy;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.DSE;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.strategy.TestGenerationStrategy;
import org.evosuite.testsuite.TestSuiteChromosome;
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
		
		Properties.TIMEOUT = 500000;
		
		
		Properties.LOG_LEVEL = "debug";
		Properties.PRINT_TO_SYSTEM = true;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        PrintStream outStream = new PrintStream(byteStream);
        System.setOut(outStream);

	}

	@Test
	public void testDSENumbers() {
		
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		ByteArrayOutputStream err = new ByteArrayOutputStream();
		System.setErr(new PrintStream(err));
			
			
		EvoSuite evosuite = new EvoSuite();

		String targetClass = BasicInt.class.getCanonicalName();
		
		
		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		DSE<?> dse = getDSEFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) dse.getPopulation().get(0);
		System.out.println("EvolvedTestSuite:\n" + best);

		int goals = TestGenerationStrategy.getFitnessFactories().get(0).getCoverageGoals().size(); // assuming
																									// single
																									// fitness
																									// function
		Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
		
		
		Logger logger = LoggerFactory.getLogger(DSEBasicIntSystemTest.class);
		
		final String warnMsg = "this should go to std out";
		final String errMsg = "this should go to std err";

		logger.warn(warnMsg);
		logger.error(errMsg);

		String printedOut = out.toString();
		String printedErr = err.toString();

		Assert.assertTrue("Content of std out is: " + printedOut,
		                  printedOut.contains(warnMsg));
		Assert.assertTrue("Content of std err is: " + printedErr,
		                  printedErr.contains(errMsg));
		Assert.assertTrue("Content of std out is: " + printedOut,
		                  !printedOut.contains(errMsg));
		Assert.assertTrue("Content of std err is: " + printedErr,
		                  !printedErr.contains(warnMsg));
	}
}
