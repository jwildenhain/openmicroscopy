/*
 * org.openmicroscopy.shoola.agents.measurement.util.workflow.WorkflowModel 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.measurement.util.workflow;




//Java imports
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

//Third-party libraries

//Application-internal dependencies
import pojos.WorkflowData;

/** 
 * This is the model for thecreaetWorkflow dialog, manipulating the 
 * WorkflowData elements.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class WorkflowModel
{
	/** This is the list of the current workflows. */
	List<WorkflowData> workflowList;
	
	/** The model for the JList box. */
	DefaultListModel listModel;
	
	/**
	 * Instantiate the workflowModel, with the workflows in worklflowList.
	 * @param workflowList See above.
	 */
	public WorkflowModel(List<WorkflowData> workflowList)
	{
		init(workflowList);
	}
	
	/**
	 * Intialise the variables in WorkflowModel.
	 * @param workflowList The workflows in the workflow list.
	 */
	private void init(List<WorkflowData> workflowList)
	{
		this.workflowList = workflowList;
		listModel = new DefaultListModel();
		for(WorkflowData workflow : this.workflowList)
			listModel.addElement(workflow.getNameSpace());
	}
	
	/** 
	 * Add a new item to the workflowModel. 
	 * @param workflow See above.
	 */
	public void addItem(WorkflowData workflow)
	{
		workflowList.add(workflow);
		listModel.addElement(workflow.getNameSpace());
	}

	/**
	 * Remove the workflow from the list. 
	 * @param workflow See above.
	 */
	public void removeItem(WorkflowData workflow)
	{
		workflowList.remove(workflow);
		listModel.removeElement(workflow.getNameSpace());
	}
	
	/**
	 * Get the workflow at position <code>index</code>.
	 * @param index See above.
	 * @return See above.
	 */
	public WorkflowData getItem(int index)
	{
		return workflowList.get(index);
	}
	
	/**
	 * Get the index for the workflow toFind.
	 * @param toFind See above.
	 * @return See above.
	 */
	public int getIndex(WorkflowData toFind)
	{
		int index = -1;
		for(WorkflowData workflow : workflowList)
		{
			index = index+1;
			if(workflow==toFind)
				return index;
		}
		return index;
	}
	
	/**
	 * Return the model as a default list.
	 * @return See above.
	 */
	public DefaultListModel getListModel()
	{
		return listModel;
	}
	
	/**
	 * Get the workflows as a list. 
	 * @return See above.
	 */
	public List<WorkflowData> getWorkflowList()
	{
		return workflowList;
	}
	
	

}
