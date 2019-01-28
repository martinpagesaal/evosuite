/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.dse;

import com.examples.with.different.packagename.dse.BooleanExample;
import com.examples.with.different.packagename.dse.ByteExample;
import com.examples.with.different.packagename.dse.ArrayLengthExample;
import com.examples.with.different.packagename.dse.CharExample;
import com.examples.with.different.packagename.dse.DoubleExample;
import com.examples.with.different.packagename.dse.FloatExample;
import com.examples.with.different.packagename.dse.LongExample;
import com.examples.with.different.packagename.dse.ShortExample;
import com.examples.with.different.packagename.dse.StringExample;
import com.examples.with.different.packagename.dse.ArrayExample;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.Properties.SolverType;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.Strategy;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

public class DSEBasicTypesSystemTest extends SystemTestBase  {

    @Before
    public void init() {
        Properties.VIRTUAL_FS = true;
        Properties.VIRTUAL_NET = true;
        Properties.LOG_LEVEL = "info";
        LoggingUtils.changeLogbackFile(LoggingUtils.getLogbackFileName());
        Properties.SEARCH_BUDGET = 50000;
        Properties.CONCOLIC_TIMEOUT = Integer.MAX_VALUE;
        Properties.RESET_STATIC_FIELD_GETS = true;

        String cvc4_path = System.getenv("cvc4_path");
        if (cvc4_path != null) {
            Properties.CVC4_PATH = cvc4_path;
        }

        Properties.DSE_SOLVER = SolverType.CVC4_SOLVER;
        Properties.DSE_CONSTRAINT_SOLVER_TIMEOUT_MILLIS = 60 * 3000;

        Properties.STOPPING_CONDITION = StoppingCondition.MAXTESTS;
        Properties.SEARCH_BUDGET = 300; // tests
        Properties.MINIMIZATION_TIMEOUT = 60 * 60 * 60;
        Properties.ASSERTION_TIMEOUT = 60 * 60 * 60;

        Properties.STRATEGY = Strategy.DSE;

        Properties.CRITERION = new Criterion[] { Criterion.BRANCH };

        Properties.MINIMIZE = true;
        Properties.ASSERTIONS = true;

        assumeTrue(Properties.CVC4_PATH != null);
    }

//    @Test
    public void testBoolean() {
        TestSuiteChromosome best = this.testTargetClass(BooleanExample.class.getCanonicalName());

        assertFalse(best.getTests().isEmpty());
        assertEquals(2, best.getNumOfCoveredGoals());
        assertEquals(1, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testByte() {
        TestSuiteChromosome best = this.testTargetClass(ByteExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testArrayLength() {
        TestSuiteChromosome best = this.testTargetClass(ArrayLengthExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(4, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testChar() {
        TestSuiteChromosome best = this.testTargetClass(CharExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testDouble() {
        TestSuiteChromosome best = this.testTargetClass(DoubleExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testFloat() {
        TestSuiteChromosome best = this.testTargetClass(FloatExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testLong() {
        TestSuiteChromosome best = this.testTargetClass(LongExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testShort() {
        TestSuiteChromosome best = this.testTargetClass(ShortExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(3, best.getNumOfCoveredGoals());
        assertEquals(0, best.getNumOfNotCoveredGoals());
    }

//    @Test
    public void testString() {
        TestSuiteChromosome best = this.testTargetClass(StringExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
        assertEquals(4, best.getNumOfCoveredGoals());
        assertEquals(1, best.getNumOfNotCoveredGoals());
    }

    @Test
    public void testArray() {
        TestSuiteChromosome best = this.testTargetClass(ArrayExample.class.getCanonicalName());
        assertFalse(best.getTests().isEmpty());
//        assertEquals(10, best.getNumOfCoveredGoals());
//        assertEquals(25, best.getNumOfNotCoveredGoals());
    }

    private TestSuiteChromosome testTargetClass(String targetClass) {
        EvoSuite evosuite = new EvoSuite();
        Properties.TARGET_CLASS = targetClass;

        String[] command = new String[] { "-generateSuite", "-class", targetClass };

        Object result = evosuite.parseCommandLine(command);
        GeneticAlgorithm<?> ga = getGAFromResult(result);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        LoggingUtils.getEvoLogger().info("EvolvedTestSuite:\n" + best);

            LoggingUtils.getEvoLogger().info("For Class: " + targetClass + ". (" + best.getNumOfCoveredGoals() + ", " + best.getNumOfNotCoveredGoals() + ") ");
        return best;
    }

}
