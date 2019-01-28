package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.evosuite.Properties;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.IntegerVariable;
import org.evosuite.symbolic.expr.fp.RealVariable;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.SolverResult;
import org.evosuite.symbolic.vm.ConstraintFactory;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.localsearch.DSETestGenerator;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a DSE algorithm *as* a subclass of genetic algorithm.
 * 
 * @author jgaleotti
 *
 * @param <T>
 */
public class DSEAlgorithm extends GeneticAlgorithm<TestSuiteChromosome> {

  private static final Logger logger = LoggerFactory.getLogger(DSEAlgorithm.class);

  /**
   * A cache of previous results from the constraint solver
   */
  private final Map<Set<Constraint<?>>, SolverResult> queryCache = new HashMap<Set<Constraint<?>>, SolverResult>();

  /**
   * Applies DSE test generation on a static non-private method until a stopping condition is met or
   * all queries have been explored.
   * 
   * @param staticEntryMethod
   * 
   */
  private void generateTestCasesAndAppendToBestIndividual(Method staticEntryMethod) {

    List<TestCase> generatedTests = this.initializeDSETestCaseGeneration(staticEntryMethod);

    boolean initialFitnessReached = this.checkInitialFitness();

    if(initialFitnessReached) {
      return;
    }

    HashSet<Set<Constraint<?>>> pathConditions = new HashSet<Set<Constraint<?>>>();

    for (int currentTestIndex = 0; currentTestIndex < generatedTests.size(); currentTestIndex++) {

      TestCase currentTestCase = generatedTests.get(currentTestIndex);

      if (this.isFinished()) {
        this.logExitCondition(generatedTests, staticEntryMethod);
        return;
      }

      final PathCondition pathCondition = this.processPathCondition(currentTestCase, pathConditions);

      for (int i = pathCondition.size() - 1; i >= 0; i--) {

//        LoggingUtils.getEvoLogger().info("negating index " + i + " of path condition");

        List<Constraint<?>> query = DSETestGenerator.buildQuery(pathCondition, i);

        Set<Constraint<?>> constraintSet = canonicalize(query);

        boolean skipIteration = this.checkSkippingIterationCondition(constraintSet, pathConditions);

        if(skipIteration) {
          continue;
        }

        if (this.isFinished()) {
          this.logExitCondition(generatedTests, staticEntryMethod);
          return;
        }

        SolverResult result = this.processQuery(query, constraintSet);

        this.processResult(result, currentTestCase, generatedTests);
      }
    }

    this.logExitCondition(generatedTests, staticEntryMethod);
    return;
  }

  /**
   * Initializes basic generated test from static method to be used in DSE iteration
   *
   * @param staticEntryMethod
   *
   */
  private List<TestCase> initializeDSETestCaseGeneration(Method staticEntryMethod) {
    double fitnessBeforeAddingDefaultTest = this.getBestIndividual().getFitness();
    LoggingUtils.getEvoLogger().info("Fitness before adding default test case:" + fitnessBeforeAddingDefaultTest);

    TestCase testCaseWithDefaultValues = buildTestCaseWithDefaultValues(staticEntryMethod);

    getBestIndividual().addTest(testCaseWithDefaultValues);

    List<TestCase> generatedTests = new ArrayList<TestCase>();
    generatedTests.add(testCaseWithDefaultValues);

    LoggingUtils.getEvoLogger().info("\n\n\nCreated new default test case with default values:\n" + testCaseWithDefaultValues.toCode());

    calculateFitnessAndSortPopulation();
    return generatedTests;
  }

  /**
   * Check if initial Fitness is 0 , in this case DSE should not be done, since Fitness was reached.
   *
   */
  private boolean checkInitialFitness() {
    double fitnessAfterAddingDefaultTest = this.getBestIndividual().getFitness();
    LoggingUtils.getEvoLogger().info("Fitness after adding default test case: " + fitnessAfterAddingDefaultTest);

    if (fitnessAfterAddingDefaultTest == 0) {
      LoggingUtils.getEvoLogger().info("No more DSE test generation since fitness is 0");
      return true;
    }

    return false;
  }

  /**
   * Logs exit condition of DSE generation.
   *
   * @param generatedTests
   * @param staticEntryMethod
   *
   */
  private void logExitCondition(List<TestCase> generatedTests, Method staticEntryMethod) {
    LoggingUtils.getEvoLogger().info("DSE test generation met a stopping condition. Exiting with "
            + generatedTests.size() + " generated test cases for method "
            + staticEntryMethod.getName());
  }

  /**
   * Generates path condition for current Test Case.
   *
   * @param currentTestCase
   * @param pathConditions
   *
   */
  private PathCondition processPathCondition(TestCase currentTestCase, HashSet<Set<Constraint<?>>> pathConditions) {

    LoggingUtils.getEvoLogger().info("\n\nStarting concolic execution of test case: \n" + currentTestCase.toCode());

    TestCase clonedTestCase = currentTestCase.clone();

    final PathCondition pathCondition = ConcolicExecution.executeConcolic((DefaultTestCase) clonedTestCase);
    LoggingUtils.getEvoLogger().info("\nPath condition collected with : " + pathCondition.size() + " branches");

    Set<Constraint<?>> constraintsSet = canonicalize(pathCondition.getConstraints());
    pathConditions.add(constraintsSet);
    LoggingUtils.getEvoLogger().info("Number of stored path condition: " + pathConditions.size()+"\n\n");


    return pathCondition;
  }

  /**
   * Verifies if this iteration needs to run or not depending on all Skipping condition
   *
   * @param constraintSet
   * @param pathConditions
   *
   */
  private boolean checkSkippingIterationCondition(Set<Constraint<?>> constraintSet, HashSet<Set<Constraint<?>>> pathConditions) {
    if (queryCache.containsKey(constraintSet)) {
//      LoggingUtils.getEvoLogger().info("skipping solving of current query since it is in the query cache");
      return true;
    }

    if (isSubSetOf(constraintSet, queryCache.keySet())) {
//      LoggingUtils.getEvoLogger().info("skipping solving of current query because it is satisfiable and solved by previous path condition");
      return true;
    }

    if (pathConditions.contains(constraintSet)) {
//      LoggingUtils.getEvoLogger().info("skipping solving of current query because of existing path condition");
      return true;
    }

    if (isSubSetOf(constraintSet, pathConditions)) {
//      LoggingUtils.getEvoLogger().info("skipping solving of current query because it is satisfiable and solved by previous path condition");
      return true;
    }

    return false;
  }

  /**
   * Process Query and returns a Sat, Unsat, Undefined result.
   *
   * @param query
   * @param constraintSet
   *
   */
  private SolverResult processQuery(List<Constraint<?>> query, Set<Constraint<?>> constraintSet) {
//    LoggingUtils.getEvoLogger().info("Solving query with  " + query.size() + " constraints");

    List<Constraint<?>> varBounds = createVarBounds(query);
    query.addAll(varBounds);

    SolverResult result = DSETestGenerator.solve(query);

    queryCache.put(constraintSet, result);
//    LoggingUtils.getEvoLogger().info("Number of stored entries in query cache : " + queryCache.keySet().size());

    return result;
  }

  /**
   * With current result from Query process it evaluates what to do depending on Sat, Unsat or undefined.
   *
   * @param result
   * @param currentTestCase
   * @param generatedTests
   *
   */
  private void processResult(SolverResult result, TestCase currentTestCase, List<TestCase> generatedTests) {
    if (result == null) {
//      LoggingUtils.getEvoLogger().info("Solver outcome is null (probably failure/unknown/timeout)");
    } else if (result.isSAT()) {
//      LoggingUtils.getEvoLogger().info("query is SAT (solution found)");
      Map<String, Object> solution = result.getModel();
//      LoggingUtils.getEvoLogger().info("solver found solution " + solution.toString());

      TestCase newTest = DSETestGenerator.updateTest(currentTestCase, solution);
//      LoggingUtils.getEvoLogger().info("Created new test case from SAT solution:" + newTest.toCode());
      generatedTests.add(newTest);

      double fitnessBeforeAddingNewTest = this.getBestIndividual().getFitness();
//      LoggingUtils.getEvoLogger().info("Fitness before adding new test" + fitnessBeforeAddingNewTest);

      getBestIndividual().addTest(newTest);

      calculateFitness(getBestIndividual());

      double fitnessAfterAddingNewTest = this.getBestIndividual().getFitness();
//      LoggingUtils.getEvoLogger().info("Fitness after adding new test " + fitnessAfterAddingNewTest);

      this.notifyIteration();

      if (fitnessAfterAddingNewTest == 0) {
//        LoggingUtils.getEvoLogger().info("No more DSE test generation since fitness is 0");
        return;
      }

    } else {
      assert (result.isUNSAT());
//      LoggingUtils.getEvoLogger().info("query is UNSAT (no solution found)");
    }
  }

  protected static HashSet<Constraint<?>> canonicalize(List<Constraint<?>> query) {
    return new HashSet<Constraint<?>>(query);
  }

  private static List<Constraint<?>> createVarBounds(List<Constraint<?>> query) {

    Set<Variable<?>> variables = new HashSet<Variable<?>>();
    for (Constraint<?> constraint : query) {
      variables.addAll(constraint.getVariables());
    }

    List<Constraint<?>> boundsForVariables = new ArrayList<Constraint<?>>();
    for (Variable<?> variable : variables) {
      if (variable instanceof IntegerVariable) {
        IntegerVariable integerVariable = (IntegerVariable) variable;
        Long minValue = integerVariable.getMinValue();
        Long maxValue = integerVariable.getMaxValue();
        if (maxValue == Long.MAX_VALUE && minValue == Long.MIN_VALUE) {
          // skip constraints for Long variables
          continue;
        }
        IntegerConstant minValueExpr = ExpressionFactory.buildNewIntegerConstant(minValue);
        IntegerConstant maxValueExpr = ExpressionFactory.buildNewIntegerConstant(maxValue);
        IntegerConstraint minValueConstraint = ConstraintFactory.gte(integerVariable, minValueExpr);
        IntegerConstraint maxValueConstraint = ConstraintFactory.lte(integerVariable, maxValueExpr);
        boundsForVariables.add(minValueConstraint);
        boundsForVariables.add(maxValueConstraint);

      } else if (variable instanceof RealVariable) {
        // skip
      } else if (variable instanceof StringVariable) {
        // skip
      } else {
        throw new UnsupportedOperationException(
            "Unknown variable type " + variable.getClass().getName());
      }
    }

    return boundsForVariables;
  }

  /**
   * Returns true if the constraints in the query are a subset of any of the constraints in the set
   * of queries
   * 
   * @param query
   * @param queries
   * @return
   */
  private static boolean isSubSetOf(Set<Constraint<?>> query,
      Collection<Set<Constraint<?>>> queries) {
    for (Set<Constraint<?>> pathCondition : queries) {
      if (pathCondition.containsAll(query)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Builds a default test case for a static target method
   * 
   * @param targetStaticMethod
   * @return
   */
  private static DefaultTestCase buildTestCaseWithDefaultValues(Method targetStaticMethod) {
    TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

    Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);
    Class<?>[] argumentClasses = targetStaticMethod.getParameterTypes();

    ArrayList<VariableReference> arguments = new ArrayList<VariableReference>();
    for (int i = 0; i < argumentTypes.length; i++) {

      Type argumentType = argumentTypes[i];
      Class<?> argumentClass = argumentClasses[i];

      switch (argumentType.getSort()) {
        case Type.BOOLEAN: {
          VariableReference booleanVariable = testCaseBuilder.appendBooleanPrimitive(false);
          arguments.add(booleanVariable);
          break;
        }
        case Type.BYTE: {
          VariableReference byteVariable = testCaseBuilder.appendBytePrimitive((byte) 0);
          arguments.add(byteVariable);
          break;
        }
        case Type.CHAR: {
          VariableReference charVariable = testCaseBuilder.appendCharPrimitive((char) 0);
          arguments.add(charVariable);
          break;
        }
        case Type.SHORT: {
          VariableReference shortVariable = testCaseBuilder.appendShortPrimitive((short) 0);
          arguments.add(shortVariable);
          break;
        }
        case Type.INT: {
          VariableReference intVariable = testCaseBuilder.appendIntPrimitive(0);
          arguments.add(intVariable);
          break;
        }
        case Type.LONG: {
          VariableReference longVariable = testCaseBuilder.appendLongPrimitive(0L);
          arguments.add(longVariable);
          break;
        }
        case Type.FLOAT: {
          VariableReference floatVariable = testCaseBuilder.appendFloatPrimitive((float) 0.0);
          arguments.add(floatVariable);
          break;
        }
        case Type.DOUBLE: {
          VariableReference doubleVariable = testCaseBuilder.appendDoublePrimitive(0.0);
          arguments.add(doubleVariable);
          break;
        }
        case Type.ARRAY: {
          ArrayList<VariableReference> arrayArguments = testCaseBuilder.appendExtendedArrayStmt(argumentClass, 0);
          arguments.addAll(arrayArguments);
//            VariableReference arrayVariable = testCaseBuilder.appendArrayStmt(argumentClass, 0);
//            arguments.add(arrayVariable);
          break;
        }
        case Type.OBJECT: {
          if (argumentClass.equals(String.class)) {
            VariableReference stringVariable = testCaseBuilder.appendStringPrimitive("");
            arguments.add(stringVariable);
          } else {
            VariableReference objectVariable = testCaseBuilder.appendNull(argumentClass);
            arguments.add(objectVariable);
          }
          break;
        }
        default: {
          throw new UnsupportedOperationException();
        }
      }
    }

    testCaseBuilder.appendMethod(null, targetStaticMethod, arguments.toArray(new VariableReference[] {}));
    DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();

    return testCase;
  }

  /**
   * Creates a DSE algorithm for test generation.
   */
  public DSEAlgorithm() {
    super(null);
  }

  /**
   * 
   */
  private static final long serialVersionUID = 964984026539409121L;

  /**
   * This algorithm does not evolve populations
   */
  @Override
  protected void evolve() {
    // skip
  }

  /**
   * The population is initialized with an empty test suite
   */
  @Override
  public void initializePopulation() {
    TestSuiteChromosome individual = new TestSuiteChromosome();
    population.clear();
    population.add(individual);
    calculateFitness(individual);
  }

  /**
   * Returns a set with the static methods of a class
   * 
   * @param targetClass a class instance
   * @return
   */
  private static List<Method> getTargetStaticMethods(Class<?> targetClass) {
    Method[] declaredMethods = targetClass.getDeclaredMethods();
    List<Method> targetStaticMethods = new LinkedList<Method>();
    for (Method m : declaredMethods) {

      if (!Modifier.isStatic(m.getModifiers())) {
        continue;
      }

      if (Modifier.isPrivate(m.getModifiers())) {
        continue;
      }

      if (m.getName().equals(ClassResetter.STATIC_RESET)) {
        continue;
      }

      targetStaticMethods.add(m);
    }
    return targetStaticMethods;
  }

  /**
   * Applies the DSE test generation using the initial population as the initial test cases
   */
  @Override
  public void generateSolution() {
    this.notifySearchStarted();
    this.initializePopulation();

    final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();

    List<Method> targetStaticMethods = getTargetStaticMethods(targetClass);
    Collections.sort(targetStaticMethods, new MethodComparator());
    LoggingUtils.getEvoLogger().info("Found " + targetStaticMethods.size() + " as entry points for DSE");

    for (Method entryMethod : targetStaticMethods) {

      if (this.isFinished()) {
        LoggingUtils.getEvoLogger().info("A stoping condition was met. No more tests can be generated using DSE.");
        break;
      }

      if (getBestIndividual().getFitness() == 0) {
        LoggingUtils.getEvoLogger().info("Best individual reached zero fitness");
        break;
      }

      LoggingUtils.getEvoLogger().info("Generating tests for entry method" + entryMethod.getName());
      int testCaseCount = getBestIndividual().getTests().size();
      generateTestCasesAndAppendToBestIndividual(entryMethod);
      int numOfGeneratedTestCases = getBestIndividual().getTests().size() - testCaseCount;
      LoggingUtils.getEvoLogger().info(numOfGeneratedTestCases + " tests were generated for entry method "
          + entryMethod.getName());

    }

    this.updateFitnessFunctionsAndValues();
    this.notifySearchFinished();
  }

}
