package org.apache.hadoop.yarn.server.nodemanager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.server.nodemanager.ContainerExecutor.Signal;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.Container;

public interface IntExecutor {
	  public void init() throws IOException;

	  public int launchContainer(Container container,
	                             Path nmPrivateContainerScriptPath, Path nmPrivateTokensPath,
	                             String userName, String appId, Path containerWorkDir,
	                             List<String> localDirs, List<String> logDirs) throws IOException;

	  public void writeLaunchEnv(OutputStream out, Map<String, String> environment, 
			  Map<Path, List<String>> resources, List<String> command) throws IOException;


	  public boolean signalContainer(Container container, String user, String pid, Signal signal)
	    throws IOException;

	  public boolean isContainerProcessAlive(Container container, String user, String pid)
	    throws IOException;


}
