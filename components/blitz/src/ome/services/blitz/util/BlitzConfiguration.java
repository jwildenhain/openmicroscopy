/*   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ome.security.SecuritySystem;
import ome.services.blitz.fire.PermissionsVerifierI;
import ome.services.blitz.fire.SessionManagerI;
import ome.services.util.Executor;
import omero.util.ObjectFactoryRegistrar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.ResourceUtils;

import Glacier2.PermissionsVerifier;
import Glacier2.SessionManager;
import Ice.Util;

/**
 * Factory bean which creates an {@lik Ice.Communicator} instance as well as the
 * proper {@link Ice.ObjectAdapter} and adds initial, well-known servants.
 * 
 * @author Josh Moore
 * @since 3.0-Beta3.1
 */
public class BlitzConfiguration {

    private final static String CONFIG_KEY = "--Ice.Config=";

    private final Log logger = LogFactory.getLog(getClass());

    private final Ice.Communicator communicator;

    private final Ice.ObjectAdapter blitzAdapter;

    private final SessionManager blitzManager;

    private final PermissionsVerifier blitzVerifier;

    private final Ice.InitializationData id;

    /**
     * Single constructor which builds all Ice instances needed for the server
     * runtime based on arguments provided. Once the constructor is finished,
     * none of the default create* methods can safely be called, since
     * {@link #throwIfInitialized()} is called first.
     * 
     * If any of the methods other than {@link #createCommunicator()} throws an
     * exception, then {@link #destroy()} will be called to properly shut down
     * the {@link Ice.Communicator} instance. Therefore {@link #destroy()}
     * should be careful to check for nulls.
     */
    public BlitzConfiguration(
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor)
            throws RuntimeException {
        this(createId(), sessionManager, securitySystem, executor);
    }

    /**
     * Like
     * {@link #BlitzConfiguration(ome.services.sessions.SessionManager, SecuritySystem, Executor)}
     * but allows properties to be specified via an
     * {@link Ice.InitializationData} instance.
     * 
     * @param id
     * @param sessionManager
     * @param securitySystem
     * @param executor
     * @throws RuntimeException
     */
    public BlitzConfiguration(Ice.InitializationData id,
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor)
            throws RuntimeException {

        logger.info("Initializing Ice.Communicator");
        
        this.id = id;
        communicator = createCommunicator();

        if (communicator == null) {
            throw new RuntimeException("No communicator cannot continue.");
        }

        try {
            registerObjectFactory();
            blitzAdapter = createAdapter();
            blitzManager = createAndRegisterManager(sessionManager,
                    securitySystem, executor);
            blitzVerifier = createAndRegisterVerifier(sessionManager);
            blitzAdapter.activate();
        } catch (RuntimeException e) {
            destroy();
            throw e;
        }
    }

    /**
     * If this configuration is finished and {@link #communicator} is not-null,
     * throw a {@link IllegalStateException}
     */
    protected final void throwIfInitialized(Object instance) {
        if (instance != null) {
            throw new IllegalStateException(
                    "Configuration has already taken place.");
        }
    }

    protected Ice.Communicator createCommunicator() {
        throwIfInitialized(communicator);

        Ice.Communicator ic;

        String ICE_CONFIG = System.getProperty("ICE_CONFIG");
        if (ICE_CONFIG != null) {
            // HORRIBLE HACK. Here we are short cutting the logic below
            // since it is complicated and needs to be reduced. This works in
            // tandem with the code in Main.main() which takes command line
            // arguments.
            id.properties.load(ICE_CONFIG);
            ic = Ice.Util.initialize(id);

        } else {
            throw new IllegalStateException("No ICE_CONFIG property found.");
        }

        return ic;
    }

    protected Ice.Communicator createCommunicator(String configFile,
            String[] arguments) {

        throwIfInitialized(communicator);

        if (configFile == null) {
            throw new IllegalArgumentException("No config file given.");
        }

        configFile = resolveConfigFile(configFile);

        if (logger.isInfoEnabled()) {
            logger.info("Reading config file:" + configFile);
        }

        Ice.Communicator ic = null;
        Ice.InitializationData id = new Ice.InitializationData();

        if (arguments == null) {
            id.properties = Util.createProperties(new String[] {});
        } else {
            for (int i = 0; i < arguments.length; i++) {
                String s = arguments[i];
                if (s != null && s.startsWith(CONFIG_KEY)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "Overriding args setting %s with %s", s,
                                configFile));
                    }
                    arguments[i] = CONFIG_KEY + configFile;
                }
            }
            id.properties = Util.createProperties(arguments);
        }

        ic = Util.initialize(id);

        return ic;
    }

    /**
     * Resolve the given config file to a concrete location, possibly a temp
     * file if the location was prevously stored in a jar. Null will not be
     * returned, but an exception may be thrown if the path is invalid.
     */
    protected String resolveConfigFile(String configFile) {

        // Doing clean up for possible jar-contained config files.
        // FIXME need better copy. and perhaps ice can read from
        // stream.
        try {
            URL file = ResourceUtils.getURL(configFile);
            if (ResourceUtils.isJarURL(file)) {
                URL jar = ResourceUtils.extractJarFileURL(file);
                // http://java.sun.com/developer/TechTips/txtarchive/2003/Jan03_JohnZ.txt
                JarFile jarFile = new JarFile(jar.getPath());
                JarEntry entry = jarFile.getJarEntry("ice.config");// FIXME
                InputStream is = jarFile.getInputStream(entry);

                // FIXME Hack.
                File tmp = File.createTempFile("omero.", ".tmp");
                tmp.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tmp);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                is.close();
                fos.close();

                configFile = tmp.getAbsolutePath();
            } else {
                configFile = file.getPath();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error resolving config file: "
                    + configFile, e);
        }
        return configFile;
    }

    protected void registerObjectFactory() {
        ObjectFactoryRegistrar.registerObjectFactory(communicator,
                ObjectFactoryRegistrar.INSTANCE);
    }

    /**
     * Creates an adapter with the name "BlitzAdapter", which must be properly
     * configured via --Ice.Config or ICE_CONFIG or similar.
     */
    protected Ice.ObjectAdapter createAdapter() {

        throwIfInitialized(blitzAdapter);

        Ice.ObjectAdapter adapter;
        try {
            adapter = communicator.createObjectAdapter("BlitzAdapter");
        } catch (Exception e) {
            throw new FatalBeanException(
                    "Could not find Ice config for object adapter [ BlitzAdapter ]");
        }

        return adapter;
    }

    protected SessionManagerI createAndRegisterManager(
            ome.services.sessions.SessionManager sessionManager,
            SecuritySystem securitySystem, Executor executor) {

        throwIfInitialized(blitzManager);

        SessionManagerI manager = new SessionManagerI(blitzAdapter,
                securitySystem, sessionManager, executor);
        this.blitzAdapter.add(manager, Ice.Util
                .stringToIdentity("BlitzManager"));
        return manager;
    }

    protected PermissionsVerifier createAndRegisterVerifier(
            ome.services.sessions.SessionManager sessionManager) {

        throwIfInitialized(blitzVerifier);

        PermissionsVerifierI verifier = new PermissionsVerifierI(sessionManager);
        this.blitzAdapter.add(verifier, Ice.Util
                .stringToIdentity("BlitzVerifier"));
        return verifier;
    }

    public void destroy() {
        logger.debug(String.format("Destroying Ice.Communicator (%s)",
                communicator));
        logger.info("Shutting down Ice.Communicator");
        if (blitzAdapter != null) {
            logger.debug(String.format("Deactivating BlitzAdapter (%s)",
                    blitzAdapter));
            blitzAdapter.deactivate();
        }
        communicator.destroy();
    }

    // Getters
    // =========================================================================

    public Ice.Communicator getCommunicator() {
        if (communicator == null) {
            throw new IllegalStateException("Communicator is null");
        }
        return communicator;
    }

    public Ice.ObjectAdapter getBlitzAdapter() {
        if (blitzAdapter == null) {
            throw new IllegalStateException("Adapter is null");
        }
        return blitzAdapter;
    }

    public SessionManager getBlitzManager() {
        if (blitzManager == null) {
            throw new IllegalStateException("Manager is null");
        }
        return blitzManager;
    }

    public PermissionsVerifier getBlitzVerifier() {
        if (blitzVerifier == null) {
            throw new IllegalStateException("Verifier is null");
        }
        return blitzVerifier;
    }

    // Helpers

    private static Ice.InitializationData createId() {
        Ice.InitializationData iData = new Ice.InitializationData();
        iData.properties = Ice.Util.createProperties();
        return iData;
    }

}
