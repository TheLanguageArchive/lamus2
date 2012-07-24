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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.jbehave.core.Embeddable;
import org.jbehave.core.InjectableEmbedder;
import org.jbehave.core.annotations.Configure;
import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.annotations.UsingSteps;
import org.jbehave.core.annotations.spring.UsingSpring;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStory;
import org.jbehave.core.junit.spring.SpringAnnotatedEmbedderRunner;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.CrossReference;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.jbehave.core.steps.ParameterConverters.ExamplesTableConverter;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML_TEMPLATE;
import static org.jbehave.core.reporters.Format.TXT;
import static org.jbehave.core.reporters.Format.XML_TEMPLATE;
import static org.jbehave.core.reporters.Format.STATS;

/**
 *
 * @author Guilherme Silva <guilherme.silva@mpi.nl>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WorkspaceStoriesConfig.class}, loader = AnnotationConfigContextLoader.class)
//@RunWith(SpringAnnotatedEmbedderRunner.class)
//@Configure()
//@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = true, ignoreFailureInView = true)
//@UsingSpring(resources = { "nl/mpi/lamus/workspace/stories/OldCreateWorkspace-context.xml", "nl/mpi/lamus/workspace/stories/jbehave_configuration.xml" })
//@UsingSteps(instances = { WorkspaceSteps.class })
@ActiveProfiles("acceptance")
public class CreateWorkspace /*extends InjectableEmbedder {*/ extends JUnitStory {
 
    private final CrossReference xref = new CrossReference();
    
    @Autowired
//    private WorkspaceSteps steps;
    private ApplicationContext context;
    
    @Override
    public Configuration configuration() {
        Class<? extends Embeddable> embeddableClass = this.getClass();
        Properties viewResources = new Properties();
        viewResources.put("decorateNonHtml", "true");
        viewResources.put("reports", "ftl/jbehave-reports-with-totals.ftl");
        // Start from default ParameterConverters instance
        ParameterConverters parameterConverters = new ParameterConverters();
        // factory to allow parameter conversion and loading from external resources (used by StoryParser too)
        ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(new LocalizedKeywords(), new LoadFromClasspath(embeddableClass), parameterConverters, new TableTransformers());
        // add custom converters
        parameterConverters.addConverters(new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                new ExamplesTableConverter(examplesTableFactory));
        return new MostUsefulConfiguration()
            .useStoryLoader(new LoadFromClasspath(embeddableClass))
            .useStoryParser(new RegexStoryParser(examplesTableFactory)) 
            .useStoryReporterBuilder(new StoryReporterBuilder()
                .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                .withDefaultFormats()
                .withViewResources(viewResources)
                .withFormats(CONSOLE, TXT, HTML_TEMPLATE, XML_TEMPLATE, STATS)
                .withFailureTrace(true)
                .withFailureTraceCompression(true)                
                .withCrossReference(xref)) 
            .useParameterConverters(parameterConverters)                     
            // use '%' instead of '$' to identify parameters
//            .useStepPatternParser(new RegexPrefixCapturingPatternParser(
//                            "%")) 
            .useStepMonitor(xref.getStepMonitor());   
    }
        
    @Override
    public List<CandidateSteps> candidateSteps() {
        return new SpringStepsFactory(configuration(), context).createCandidateSteps();
    }
    
    
//    @Test
//    public void run() {
//        injectedEmbedder().runStoriesAsPaths(storyPaths());
//    }
// 
//    protected List<String> storyPaths() {
//        String searchInDirectory = CodeLocations.codeLocationFromPath("src/test/resources").getFile();
//        
//        StoryFinder lala = new StoryFinder();
//        List<String> storyList = Arrays.asList("**/*.story");
//        List<String> finalList = lala.findPaths(searchInDirectory, storyList, null);
//        
//        return new StoryFinder().findPaths(searchInDirectory, Arrays.asList("**/*.story"), null);
//    }
    
}
