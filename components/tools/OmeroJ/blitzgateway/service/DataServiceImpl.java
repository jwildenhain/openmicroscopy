/*
 * blitzgateway.service.DataService 
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
package blitzgateway.service;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//Third-party libraries

//Application-internal dependencies
import omero.RBool;
import omero.RType;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectI;

import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;

import blitzgateway.service.gateway.IPojoGateway;
import blitzgateway.service.gateway.IQueryGateway;
import blitzgateway.service.gateway.ITypeGateway;
import blitzgateway.service.gateway.IUpdateGateway;
import blitzgateway.util.OMEROClass;
import blitzgateway.util.ServiceUtilities;

/** 
 * 
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
class DataServiceImpl
	implements DataService
{	
	/** The IPojoGateway */
	IPojoGateway 	pojoGateway;
	
	/** The IQuerygateway. */
	IQueryGateway 	iQueryGateway;

	/** The ITypegateway. */
	ITypeGateway 	iTypeGateway;
	
	/** The IUpdate. */
	IUpdateGateway iUpdateGateway;
	
	/**
	 * Constructor for the DataService Implementation.
	 * @param gateway blitz gateway object.
	 */
	DataServiceImpl(IPojoGateway pojo, IQueryGateway query, ITypeGateway type, IUpdateGateway update)
	{
		pojoGateway = pojo;
		iQueryGateway = query;
		iTypeGateway = type;
		iUpdateGateway = update;
	}
	
	/**
	 * implementation of the dataservice getImages method 
	 * {@link DataService#getImages(List, boolean).
	 */
	public List<Image> getImages(OMEROClass nodeType, List<Long> nodeIds)
		throws DSOutOfServiceException, DSAccessException
	{
		HashMap<String, RType> map = new HashMap<String, RType>();
		return ServiceUtilities.collectionCast(Image.class,
			pojoGateway.getImages(convertPojos(nodeType), nodeIds, map));
	}
	
	/**
	 * implementation of the dataservice getDatasets method 
	 * {@link DataService#getDatasets(List, boolean).
	 */
	public List<Dataset> getDatasets(List<Long> ids, boolean getLeaves)
		throws DSOutOfServiceException, DSAccessException
	{
		HashMap<String, RType> map = new HashMap<String, RType>();
		if(getLeaves)
			map.put(omero.constants.POJOLEAVES.value, new RBool(true));
		return ServiceUtilities.collectionCast(Dataset.class, 
			pojoGateway.loadContainerHierarchy(
				convertPojos(OMEROClass.Dataset), ids,	map));
	}

	/**
	 * implementation of the dataservice getProjects method 
	 * {@link DataService#getProjects(boolean).
	 */
	public List<Project> getProjects(List<Long> ids, boolean getLeaves) 
	throws DSOutOfServiceException, DSAccessException
	{
		HashMap<String, RType> map = new HashMap<String, RType>();
		if(getLeaves)
			map.put(omero.constants.POJOLEAVES.value, new RBool(true));
		return ServiceUtilities.collectionCast(Project.class, 
			pojoGateway.loadContainerHierarchy(
				convertPojos(OMEROClass.Project), ids, map));
	}

	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	private String convertPojos(OMEROClass nodeType)
	{
		return nodeType.toString();
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelsFromImage(java.util.List)
	 */
	public List<Pixels> getPixelsFromImage(long imageId) 
				throws DSOutOfServiceException, DSAccessException
	{
		
		String queryStr = new String("select p from Pixels as p left outer " +
			"join fetch p.pixelsType as pt left outer join fetch " +
			"p.pixelsDimensions where p.image = " + imageId + " order by relatedto");
		return ServiceUtilities.collectionCast(Pixels.class, 
			iQueryGateway.findAllByQuery(queryStr));
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelTypes(String)
	 */
	public List<PixelsType> getPixelTypes() 
	throws DSOutOfServiceException, DSAccessException
	{
		List<IObject> list = iTypeGateway.allEnumerations("PixelsType"); 
		return ServiceUtilities.collectionCast(PixelsType.class, list);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelType(java.lang.String)
	 */
	public PixelsType getPixelType(String type) throws DSOutOfServiceException,
			DSAccessException
	{
		return (PixelsType)iQueryGateway.findByString("PixelsType", "value",
			type);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#findAllByQuery(java.lang.String)
	 */
	public Object findAllByQuery(String myQuery) throws DSOutOfServiceException,
			DSAccessException
	{
		return iQueryGateway.findAllByQuery(myQuery);
	}
	
	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#findByQuery(java.lang.String)
	 */
	public Object findByQuery(String myQuery) throws DSOutOfServiceException,
			DSAccessException
	{
		return iQueryGateway.findByQuery(myQuery);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#attachImageToDataset(omero.model.Dataset, omero.model.Image)
	 */
	public void attachImageToDataset(Dataset dataset, Image image)
			throws DSOutOfServiceException, DSAccessException
	{
		DatasetImageLink link = new DatasetImageLinkI();
		link.parent = dataset;
		link.child = image;
		dataset.addDatasetImageLink(link, true);
		iUpdateGateway.saveObject(link);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getImageFromDatasetByName(java.lang.Long, java.lang.String)
	 */
	public List<Image> getImageFromDatasetByName(Long datasetId,
			String imageName) throws DSOutOfServiceException, DSAccessException
	{
		String datasetQuery = "select i from Image i left outer join fetch i.datasetLinks" +  
		                " dil left outer join fetch dil.parent d where d.id=" + datasetId +
		                " and  i.name like '%"+ imageName+ "%' order by i.name";	
		List<Image> imageList = (List<Image>) findAllByQuery(datasetQuery);
		return imageList;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getImagesFromDataset(long)
	 */
	public List<Image> getImagesFromDataset(Dataset dataset)
			throws DSOutOfServiceException, DSAccessException
	{
		List<Image> images = new ArrayList<Image>();
		Iterator<DatasetImageLink> iterator = ((DatasetI)dataset).iterateImageLinks(); 
		while(iterator.hasNext())
		    images.add(iterator.next().child);
		return images;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getDatasetsFromProject(long)
	 */
	public List<Dataset> getDatasetsFromProject(Project project)
	throws DSOutOfServiceException, DSAccessException
	{
		List<Dataset> datasets = new ArrayList<Dataset>();
		Iterator<ProjectDatasetLink> iterator = ((ProjectI)project).iterateDatasetLinks();
		while(iterator.hasNext())
			datasets.add(iterator.next().child);
		return datasets;
	}

	
	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getImagesFromProject(long)
	 */
	public List<Image> getImagesFromProject(Project project)
			throws DSOutOfServiceException, DSAccessException
	{
		List<Image> images = new ArrayList<Image>();
		List<Dataset> datasets = getDatasetsFromProject(project);
		for(Dataset dataset : datasets)
		{
			List<Image> datasetImages = getImagesFromDataset(dataset);
			for(Image image : datasetImages)
				images.add(image);
		}
		return images;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelsFromImageList(java.util.List)
	 */
	public List<Pixels> getPixelsFromImageList(List<Image> images)
	{
		List<Pixels> pixelsList = new ArrayList<Pixels>();
		for(Image image : images)
			for(Pixels pixels : image.pixels)
				pixelsList.add(pixels);
		return pixelsList;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelsImageMap(java.util.List)
	 */
	public Map<Long, Pixels> getPixelsImageMap(List<Image> images)
	{
		Map<Long, Pixels> pixelsList = new TreeMap<Long, Pixels>();
		for(Image image : images)
			for(Pixels pixels : image.pixels)
				pixelsList.put(image.id.val, pixels);
		return pixelsList;
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelsFromDataset(omero.model.Dataset)
	 */
	public List<Pixels> getPixelsFromDataset(Dataset dataset)
			throws DSOutOfServiceException, DSAccessException
	{
		List<Image> images = getImagesFromDataset(dataset);
		return getPixelsFromImageList(images);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getPixelsFromProject(omero.model.Project)
	 */
	public List<Pixels> getPixelsFromProject(Project project)
			throws DSOutOfServiceException, DSAccessException
	{
		List<Image> images = getImagesFromProject(project);
		return getPixelsFromImageList(images);
	}

	/* (non-Javadoc)
	 * @see blitzgateway.service.DataService#getImageByName(java.lang.String)
	 */
	public List<Image> getImageByName(String imageName)
			throws DSOutOfServiceException, DSAccessException
	{
		String datasetQuery = "select i from Image i left outer join fetch i.datasetLinks" +  
        " dil left outer join fetch dil.parent d where i.name like '%"+ 
        imageName+ "%' order by i.name";	
		List<Image> imageList = (List<Image>) findAllByQuery(datasetQuery);
		return imageList;
	}
	
	
	

}


