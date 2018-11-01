/**
 * 
 */
package rulesengine;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.base.MapGlobalResolver;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.KnowledgeBuilderOption;
import org.drools.builder.conf.KnowledgeBuilderOptionsConfiguration;
import org.drools.command.assertion.AssertEquals;
import org.drools.conf.AssertBehaviorOption;
import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import drools.somedomain.box01.Server;

/**
 * @author sony
 *
 */
public class Old_Test_Ch02_Cases {
	
	private PoolingDataSource poolDS;
	private Environment env ;
	private KnowledgeBase base ;
	StatefulKnowledgeSession session ;
	private Logger log = Logger.getLogger(getClass());
	
	@Test
	public void testPersistence(){
		log.info("starting persistence test...");
		Assert.assertNotNull(session);
		
		try {
			UserTransaction ut = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
			ut.begin();
			Server debian = new Server("debian", new Date(), 4, 1024, 1024, 5);
			session.insert(debian);
			session.fireAllRules();
			ut.commit();
			
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("done!");		
	}
	
	@Test
	public void testDuplicates(){
		KnowledgeBaseConfiguration c = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		c.setOption(AssertBehaviorOption.EQUALITY);
		KnowledgeBase b = KnowledgeBaseFactory.newKnowledgeBase(c);
		StatefulKnowledgeSession sess = b.newStatefulKnowledgeSession();

		Server d1 = new Server("d1",new Date(), 5,2048,2048,5);
		sess.insert(d1);
		int current = sess.getObjects().size();
		log.info("Before second insert total facts :" + sess.getObjects().size());
		Server d2 = new Server("d1",new Date(), 5,2048,2048,5);
		sess.insert(d2);
		log.info("Total facts :" + sess.getObjects().size());
		
		Assert.assertEquals(current, sess.getObjects().size());
	}
	

	@Before
	public void setup(){
		InputStream in = getClass().getResourceAsStream("persistence.xml");
		
		poolDS = new PoolingDataSource();
		poolDS.setUniqueName("jdbc/testDatasource");
		poolDS.setMaxPoolSize(5);
		poolDS.setAllowLocalTransactions(true);
		
		poolDS.setClassName("org.h2.jdbcx.JdbcDataSource");
		poolDS.setMaxPoolSize(3);
		poolDS.getDriverProperties().put("user", "sa");
		poolDS.getDriverProperties().put("password", "sa");
		poolDS.getDriverProperties().put("URL", "jdbc:h2:mem:");
		
		poolDS.init();
		
		env = KnowledgeBaseFactory.newEnvironment();
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("drools.somedomain.persistence.jpa");
		
		env.set(EnvironmentName.ENTITY_MANAGER_FACTORY,emf);
		env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());
		env.set(EnvironmentName.GLOBALS, new MapGlobalResolver());
		
				
		KnowledgeBuilder build = KnowledgeBuilderFactory.newKnowledgeBuilder();
		build.add(new ClassPathResource("Rule_01.drl", Resource.class) , ResourceType.DRL);
		
		System.out.println("Starting to load Rules for persistence.... ");
		
		if(build.hasErrors()){
			KnowledgeBuilderErrors errs = build.getErrors();
			for(KnowledgeBuilderError err : errs){
				System.err.println(err);
			}
		}
		
		
		base = build.newKnowledgeBase();
		base.addKnowledgePackages(build.getKnowledgePackages());
		session = JPAKnowledgeService.newStatefulKnowledgeSession(base, null, env);
		System.out.println("Session created :" + session.getId() +  " instace :" + session);
		
	}
	
	@After
	public void tearDown(){
		poolDS.close();
		session.dispose();
	}
}
