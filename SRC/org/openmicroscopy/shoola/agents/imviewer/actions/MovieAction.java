/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.MovieAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.imviewer.actions;


//Java imports
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Brings up the widget to play a movie.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class MovieAction
    extends ViewerAction
    implements PropertyChangeListener
{

    /** The name of the action. */
    private static final String NAME = "Movie";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "Bring up the movie player.";

    /** The movie player. */
    private MoviePlayerDialog   moviePlayer;
    
    /** Discards the movie player. */
    private void discard()
    {
        if (moviePlayer == null) return;
        moviePlayer.setVisible(false);
        moviePlayer.dispose();
        moviePlayer = null;
    }
    
    /**
     * Disposes and closes the movie player when the {@link ImViewer} is
     * discarded.
     * @see ViewerAction#onStateChange(ChangeEvent)
     */
    protected void onStateChange(ChangeEvent e)
    {
    	switch (model.getState()) {
			case ImViewer.DISCARDED:
				discard();
				break;
			case ImViewer.RENDERING_CONTROL_LOADED:
			case ImViewer.READY:
				int max = Math.max(model.getMaxZ(), model.getMaxT());
				setEnabled(max != 0);
				break;
			}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public MovieAction(ImViewer model)
    {
        super(model, NAME);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager icons = IconManager.getInstance();
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.MOVIE));
        model.addPropertyChangeListener(ImViewer.ICONIFIED_PROPERTY, this);
    }
    
    /** 
     * Brings the movie player on screen.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (moviePlayer == null) 
            moviePlayer = new MoviePlayerDialog(model.getUI(), model);
        UIUtilities.setLocationRelativeToAndShow(model.getUI(), moviePlayer);  
    }

    /**
     * Reacts to {@link ImViewer#ICONIFIED_PROPERTY}.
     * 
     * @param evt The event to handle.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (((Boolean) evt.getNewValue()).booleanValue()) discard();
    }
    
}
