/*
 * Created on Feb 10, 2005
 */
package org.ome.tests.srv;

import java.net.URISyntaxException;
import java.rmi.RemoteException;

import org.ome.interfaces.FollowGroupService;
import org.ome.model.IFollowGroup;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;
;

/**
 * @author josh
 */
public class FollowGroupServiceTest extends BaseServiceTestCase {

	FollowGroupService fgs;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(FollowGroupServiceTest.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fgs = (FollowGroupService) SpringTestHarness.ctx.getBean("followGroupService");
	}
	
	public void testGetFollowGroup() throws RemoteException, URISyntaxException{
		IFollowGroup fg = fgs.getFollowGroup(new LSID(Vocabulary.NS+"summaryFollowGroup"));
		System.out.println(fg);
	}
	
	public void testNullPointerException() throws RemoteException, URISyntaxException{
		boolean npe = false;
		try {
		IFollowGroup fg = fgs.getFollowGroup(new LSID(Vocabulary.NS+"NONEXISTANTFollowGroup"));
		} catch (NullPointerException e	) {
			npe = true;
		}
		assertTrue(npe);
	}
	
}
