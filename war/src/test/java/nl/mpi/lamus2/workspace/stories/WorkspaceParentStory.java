package nl.mpi.lamus2.workspace.stories;

import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.HTML;
import static org.jbehave.core.reporters.Format.XML;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.SilentStepMonitor;

public class WorkspaceParentStory extends JUnitStory {

//  private final CrossReference xref = new CrossReference();
    //
//        public InventoryStory() {
//            configuredEmbedder().embedderControls().doGenerateViewAfterStories(true)
//            .doIgnoreFailureInStories(true).doIgnoreFailureInView(true)
//            .useThreads(2).useStoryTimeoutInSecs(60);
//            // Uncomment to set meta filter, which can also be set via Ant or Maven
//            // configuredEmbedder().useMetaFilters(Arrays.asList("+theme parametrisation"));
//        }
    //
//        @Override
//        public Configuration configuration() {
//            Class<? extends Embeddable> embeddableClass = this.getClass();
//            Properties viewResources = new Properties();
//            viewResources.put("decorateNonHtml", "true");
//            // Start from default ParameterConverters instance
//            ParameterConverters parameterConverters = new ParameterConverters();
//            // factory to allow parameter conversion and loading from external
//            // resources (used by StoryParser too)
//            ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(),
//                    new LoadFromClasspath(embeddableClass), parameterConverters);
//            // add custom converters
//            parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
//                    new ExamplesTableConverter(examplesTableFactory));
    //
//            return new MostUsefulConfiguration()
//                    .useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false))
//                    .useStoryLoader(new LoadFromClasspath(embeddableClass))
//                    .useStoryParser(new RegexStoryParser(examplesTableFactory))
//                    .useStoryPathResolver(new UnderscoredCamelCaseResolver())
//                    .useStoryReporterBuilder(
//                            new StoryReporterBuilder()
//                                    .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
//                                    .withDefaultFormats().withPathResolver(new ResolveToPackagedName())
//                                    .withViewResources(viewResources).withFormats(CONSOLE, TXT, HTML, XML)
//                                    .withFailureTrace(true).withFailureTraceCompression(true).withCrossReference(xref))
//                    .useParameterConverters(parameterConverters)
//                    // use '%' instead of '$' to identify parameters
//                    .useStepPatternParser(new RegexPrefixCapturingPatternParser("%")) 
//                    .useStepMonitor(xref.getStepMonitor());
//        }
    //
//        @Override
//        public InjectableStepsFactory stepsFactory() {
//            return new InstanceStepsFactory(configuration(), new InventorySteps());
//        }
    	
        public WorkspaceParentStory() {
        	 
            // start with default configuration, overriding only the elements that are needed
            StoryPathResolver storyPathResolver = new UnderscoredCamelCaseResolver(".story");
//            storyPathResolver.resolve(storyClass);
//            URL storyURL = null;
//            try {
//    			storyURL = new URL("file://" + System.getProperty("user.dir")
//    					+ "/src/test/java/bdd/org/inventory/stories");
//    		} catch (MalformedURLException e) {
//    			e.printStackTrace();
//    		}
            Properties viewProperties = new Properties();
            viewProperties.put("decorateNonHtml", "true");
            URL codeLocation = CodeLocations.codeLocationFromClass(this.getClass());
            Configuration configuration = new MostUsefulConfiguration()
                    .useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false))
                    .useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader()))
//                    .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
//                    .useStoryLoader(new LoadFromRelativeFile(storyURL))
                    .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withCodeLocation(codeLocation)
                        .withDefaultFormats()
                        .withViewResources(viewProperties)
                        .withFormats(CONSOLE, TXT, HTML, XML)
                        .withFailureTrace(true)
                        .withFailureTraceCompression(false))
                    .useStoryPathResolver(storyPathResolver)
                    .useStepMonitor(new SilentStepMonitor())
//                    .useStepPatternParser(new RegexPrefixCapturingPatternParser("%"))
                    .usePendingStepStrategy(new FailingUponPendingStep());
                     
            useConfiguration(configuration);
            addSteps(createSteps(configuration));
             
            configuredEmbedder().embedderControls().doGenerateViewAfterStories(true)
            .doIgnoreFailureInStories(true).doIgnoreFailureInView(false);
//            .doVerboseFailures(true);
            //.doIgnoreFailureInReports(false);
     
        }
     
        protected List<CandidateSteps> createSteps(Configuration configuration) {
            return new InstanceStepsFactory(configuration,
                    new WorkspaceSteps()).createCandidateSteps();
        }
        
//        @Override
//        public Configuration configuration() {
//            return new MostUsefulConfiguration()
//                // where to find the stories
//                .useStoryLoader(new LoadFromClasspath(this.getClass()))  
//                // CONSOLE and TXT reporting
//                .useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats()
//                		.withFormats(CONSOLE, TXT, HTML, XML));
//        }
    //
//            // Here we specify the steps classes
//            @Override
//            public List<CandidateSteps> candidateSteps() {        
//                // varargs, can have more that one steps classes
//                return new InstanceStepsFactory(configuration(), new InventorySteps())
//                .createCandidateSteps();
//            }

    
}