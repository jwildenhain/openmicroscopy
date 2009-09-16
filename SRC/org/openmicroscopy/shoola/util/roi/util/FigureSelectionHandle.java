/*
* org.openmicroscopy.shoola.util.roi.util.FigureSelectionHandle
*
 *------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.roi.util;


//Java imports
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.security.acl.Owner;

//Third-party libraries
import org.jhotdraw.draw.AbstractHandle;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.BoundsOutlineHandle;
import org.jhotdraw.draw.Figure;
import static org.jhotdraw.draw.AttributeKeys.TRANSFORM;

//Application-internal dependencies

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
 * @since 3.0-Beta4
 */
public class FigureSelectionHandle 
	extends BoundsOutlineHandle
{
	
	public FigureSelectionHandle(Figure owner) 
	{
		super(owner);
	}

	@Override public void draw(Graphics2D g) 
	{
		Shape bounds = getOwner().getBounds();
		Figure f = getOwner();
		Color colour = (Color)f.getAttribute(AttributeKeys.STROKE_COLOR);
		double width = (Double)f.getAttribute(AttributeKeys.STROKE_WIDTH);
		f.setAttribute(AttributeKeys.STROKE_COLOR, colour.brighter());
		f.setAttribute(AttributeKeys.STROKE_WIDTH, width+1);
		if(f.isVisible())
			f.draw(g);
		f.setAttribute(AttributeKeys.STROKE_COLOR, colour);
		f.setAttribute(AttributeKeys.STROKE_WIDTH, width);
		
	}
}