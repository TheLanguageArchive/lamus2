/*
 * Copyright (C) 2012 Max Planck Institute for Psycholinguistics
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.mpi.lamus.workspace.stories;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.failures.PassingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryPathResolver;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStory;
import static org.jbehave.core.reporters.Format.*;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WorkspaceStoriesConfig.class}, loader = AnnotationConfigContextLoader.class)
public class WorkspaceParentStory extends JUnitStory {

    @Autowired
    private ApplicationContext context;
    
//    public WorkspaceParentStory() {

    private void temporarilyNotUsedMethod() {
    
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
        Configuration configuration = new MostUsefulConfiguration().useStoryControls(new StoryControls().doDryRun(false).doSkipScenariosAfterFailure(false)).useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader())) //                    .useStoryLoader(new LoadFromClasspath(storyClass.getClassLoader()))
                //                    .useStoryLoader(new LoadFromRelativeFile(storyURL))
                .useStoryReporterBuilder(new StoryReporterBuilder().withCodeLocation(codeLocation).withDefaultFormats().withViewResources(viewProperties).withFormats(CONSOLE, TXT, HTML, XML).withFailureTrace(true).withFailureTraceCompression(false)).useStoryPathResolver(storyPathResolver).useStepMonitor(new SilentStepMonitor());
//                    .useStepPatternParser(new RegexPrefixCapturingPatternParser("%"))
//                .usePendingStepStrategy(new FailingUponPendingStep());
//                    .usePendingStepStrategy(new PassingUponPendingStep());

        useConfiguration(configuration);
        addSteps(createSteps(configuration));

        configuredEmbedder().embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(false).doIgnoreFailureInView(false);
//            .doVerboseFailures(true);
        //.doIgnoreFailureInReports(false);

    }

    protected List<CandidateSteps> createSteps(Configuration configuration) {
        return new InstanceStepsFactory(configuration,
                new WorkspaceSteps()).createCandidateSteps();
    }
    
    
//    @After
//    public void cleanData() {
//        jdbc.execute("truncate schema PUBLIC and commit");
//    }
    
    
    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration();
    }
    
    @Override
    public List<CandidateSteps> candidateSteps() {
        return new SpringStepsFactory(configuration(), context).createCandidateSteps();
    }
}
