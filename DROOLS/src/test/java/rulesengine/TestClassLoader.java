package rulesengine;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.verifier.Verifier;
import org.drools.verifier.VerifierError;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.drools.verifier.data.VerifierReport;
import org.drools.verifier.report.components.MissingRange;
import org.drools.verifier.report.components.Severity;
import org.drools.verifier.report.components.VerifierMessageBase;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.ranges.RangeException;

public class TestClassLoader {

	
	@Test
	public void testChangeRequest(){
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream s = cl.getResourceAsStream("rules.drl");
		InputStream s2 = cl.getResourceAsStream("drools/somedomain/box02/model.jar");
		URL modelJarURL = cl.getResource("drools/somedomain/box02/model.jar");
		URLClassLoader customCC = new URLClassLoader(new URL[] {modelJarURL});
		KnowledgeBuilderConfiguration buildConfig =KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(null, customCC);
		KnowledgeBaseConfiguration baseConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration(null, customCC);
		
		KnowledgeBase base = KnowledgeBaseFactory.newKnowledgeBase(baseConfig);
		
		KnowledgeAgentConfiguration aConfig = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
		KnowledgeAgent agent = KnowledgeAgentFactory.newKnowledgeAgent("test", base, aConfig, buildConfig);
		agent.applyChangeSet(new ClassPathResource("change-set.xml", getClass()));
		base = agent.getKnowledgeBase();
		
		Assert.assertNotNull(base);
	}
	
	
	@Test
	public void testRulesVerifier(){
		VerifierBuilder vbuild = VerifierBuilderFactory.newVerifierBuilder();
		Verifier verifier = vbuild.newVerifier();
		
		verifier.addResourcesToVerify(new ClassPathResource("Rules_05.drl", getClass()), ResourceType.DRL);
		
		if(verifier.hasErrors()){
			List<VerifierError> errs = verifier.getErrors();
			for(VerifierError err:errs){
				System.err.println(err.getMessage());
			}
			throw new RuntimeException("rules with errors");
		}
		verifier.fireAnalysis();
		
		VerifierReport result= verifier.getResult();
		
		Collection<VerifierMessageBase> noteMsgs = result.getBySeverity(Severity.NOTE);
		for(VerifierMessageBase msg : noteMsgs){
			System.out.println("Note :" + msg.getMessage() + " type :" + msg.getImpactedRules() + " on :" + msg.getFaulty());
		}
		
		Collection<VerifierMessageBase> errMsgs = result.getBySeverity(Severity.ERROR);
		for(VerifierMessageBase msg : errMsgs){
			System.out.println("Error:" + msg.getMessage() + " type :" + msg.getImpactedRules() + " on :" + msg.getFaulty());
		}
		
		Collection<VerifierMessageBase> warnMsgs = result.getBySeverity(Severity.WARNING);
		for(VerifierMessageBase msg : warnMsgs){
			System.out.println("Warning :" + msg.getMessage() + " type :" + msg.getImpactedRules() + " on :" + msg.getFaulty());
		}
		
		Collection<MissingRange> rangeCauses = result.getRangeCheckCauses();
		for(MissingRange range : rangeCauses){
			System.out.println(range);
		}
		
		verifier.dispose();
	}


}
