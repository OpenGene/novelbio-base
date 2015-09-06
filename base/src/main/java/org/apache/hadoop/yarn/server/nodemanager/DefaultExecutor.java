/**
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.hadoop.yarn.server.nodemanager;

import static org.apache.hadoop.fs.CreateFlag.CREATE;
import static org.apache.hadoop.fs.CreateFlag.OVERWRITE;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.Shell.CommandExecutor;
import org.apache.hadoop.util.Shell.ExitCodeException;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.nodemanager.ContainerExecutor.ExitCode;
import org.apache.hadoop.yarn.server.nodemanager.ContainerExecutor.Signal;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.Container;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.ContainerDiagnosticsUpdateEvent;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.localizer.ContainerLocalizer;
import org.apache.hadoop.yarn.util.ConverterUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class DefaultExecutor implements IntExecutor {
  private static final Log LOG = LogFactory
      .getLog(DefaultExecutor.class);

  private static final int WIN_MAX_PATH = 260;

  ContainerExecutorImpt containerExecutor;
  public DefaultExecutor(	ContainerExecutorImpt containerExecutor) {
	  this.containerExecutor = containerExecutor;
  }
 
  public void init() throws IOException {
    // nothing to do or verify here
  }

  public int launchContainer(Container container,
      Path nmPrivateContainerScriptPath, Path nmPrivateTokensPath,
      String user, String appId, Path containerWorkDir,
      List<String> localDirs, List<String> logDirs) throws IOException {
    
    FsPermission dirPerm = new FsPermission(ContainerExecutorImpt.APPDIR_PERM);
    ContainerId containerId = container.getContainerId();

    // create container dirs on all disks
    String containerIdStr = ConverterUtils.toString(containerId);
    String appIdStr =
        ConverterUtils.toString(
            containerId.getApplicationAttemptId().
                getApplicationId());
    for (String sLocalDir : localDirs) {
      Path usersdir = new Path(sLocalDir, ContainerLocalizer.USERCACHE);
      Path userdir = new Path(usersdir, user);
      Path appCacheDir = new Path(userdir, ContainerLocalizer.APPCACHE);
      Path appDir = new Path(appCacheDir, appIdStr);
      Path containerDir = new Path(appDir, containerIdStr);
      containerExecutor.createDir(containerDir, dirPerm, true, user);
    }

    // Create the container log-dirs on all disks
    containerExecutor.createContainerLogDirs(appIdStr, containerIdStr, logDirs, user);

    Path tmpDir = new Path(containerWorkDir,
        YarnConfiguration.DEFAULT_CONTAINER_TEMP_DIR);
    containerExecutor.createDir(tmpDir, dirPerm, false, user);


    // copy container tokens to work dir
    Path tokenDst =
      new Path(containerWorkDir, ContainerLaunch.FINAL_CONTAINER_TOKENS_FILE);
    containerExecutor.copyFile(nmPrivateTokensPath, tokenDst, user);

    // copy launch script to work dir
    Path launchDst =
        new Path(containerWorkDir, ContainerLaunch.CONTAINER_SCRIPT);
    containerExecutor.copyFile(nmPrivateContainerScriptPath, launchDst, user);

    // Create new local launch wrapper script
    LocalWrapperScriptBuilder sb = getLocalWrapperScriptBuilder(
        containerIdStr, containerWorkDir); 

    // Fail fast if attempting to launch the wrapper script would fail due to
    // Windows path length limitation.
    if (Shell.WINDOWS &&
        sb.getWrapperScriptPath().toString().length() > WIN_MAX_PATH) {
      throw new IOException(String.format(
        "Cannot launch container using script at path %s, because it exceeds " +
        "the maximum supported path length of %d characters.  Consider " +
        "configuring shorter directories in %s.", sb.getWrapperScriptPath(),
        WIN_MAX_PATH, YarnConfiguration.NM_LOCAL_DIRS));
    }

    Path pidFile = containerExecutor.getPidFilePath(containerId);
    if (pidFile != null) {
      sb.writeLocalWrapperScript(launchDst, pidFile);
    } else {
      LOG.info("Container " + containerIdStr
          + " was marked as inactive. Returning terminated error");
      return ExitCode.TERMINATED.getExitCode();
    }
    
    // create log dir under app
    // fork script
    Shell.CommandExecutor shExec = null;
    try {
    	containerExecutor.setScriptExecutable(launchDst, user);
    	containerExecutor.setScriptExecutable(sb.getWrapperScriptPath(), user);

      shExec = buildCommandExecutor(sb.getWrapperScriptPath().toString(),
          containerIdStr, user, pidFile, container.getResource(),
          new File(containerWorkDir.toUri().getPath()),
          container.getLaunchContext().getEnvironment());
      
      if (containerExecutor.isContainerActive(containerId)) {
        shExec.execute();
      }
      else {
        LOG.info("Container " + containerIdStr +
            " was marked as inactive. Returning terminated error");
        return ExitCode.TERMINATED.getExitCode();
      }
    } catch (IOException e) {
      if (null == shExec) {
        return -1;
      }
      int exitCode = shExec.getExitCode();
      LOG.warn("Exit code from container " + containerId + " is : " + exitCode);
      // 143 (SIGTERM) and 137 (SIGKILL) exit codes means the container was
      // terminated/killed forcefully. In all other cases, log the
      // container-executor's output
      if (exitCode != ExitCode.FORCE_KILLED.getExitCode()
          && exitCode != ExitCode.TERMINATED.getExitCode()) {
        LOG.warn("Exception from container-launch with container ID: "
            + containerId + " and exit code: " + exitCode , e);

        StringBuilder builder = new StringBuilder();
        builder.append("Exception from container-launch.\n");
        builder.append("Container id: " + containerId + "\n");
        builder.append("Exit code: " + exitCode + "\n");
        if (!Optional.fromNullable(e.getMessage()).or("").isEmpty()) {
          builder.append("Exception message: " + e.getMessage() + "\n");
        }
        builder.append("Stack trace: "
            + StringUtils.stringifyException(e) + "\n");
        if (!shExec.getOutput().isEmpty()) {
          builder.append("Shell output: " + shExec.getOutput() + "\n");
        }
        String diagnostics = builder.toString();
        containerExecutor.logOutput(diagnostics);
        container.handle(new ContainerDiagnosticsUpdateEvent(containerId,
            diagnostics));
      } else {
        container.handle(new ContainerDiagnosticsUpdateEvent(containerId,
            "Container killed on request. Exit code is " + exitCode));
      }
      return exitCode;
    } finally {
      if (shExec != null) shExec.close();
    }
    return 0;
  }

  protected CommandExecutor buildCommandExecutor(String wrapperScriptPath, 
      String containerIdStr, String user, Path pidFile, Resource resource,
      File wordDir, Map<String, String> environment)
          throws IOException {
    
    String[] command = containerExecutor.getRunCommand(wrapperScriptPath,
        containerIdStr, user, pidFile, containerExecutor.getConf(), resource);

      LOG.info("launchContainer: " + Arrays.toString(command));
      return new ShellCommandExecutor(
          command,
          wordDir,
          environment); 
  }

  protected LocalWrapperScriptBuilder getLocalWrapperScriptBuilder(
      String containerIdStr, Path containerWorkDir) {
   return  Shell.WINDOWS ?
       new WindowsLocalWrapperScriptBuilder(containerIdStr, containerWorkDir) :
       new UnixLocalWrapperScriptBuilder(containerWorkDir);
  }

  protected abstract class LocalWrapperScriptBuilder {

    private final Path wrapperScriptPath;

    public Path getWrapperScriptPath() {
      return wrapperScriptPath;
    }

    public void writeLocalWrapperScript(Path launchDst, Path pidFile) throws IOException {
      DataOutputStream out = null;
      PrintStream pout = null;

      try {
        out = containerExecutor.lfs.create(wrapperScriptPath, EnumSet.of(CREATE, OVERWRITE));
        pout = new PrintStream(out, false, "UTF-8");
        writeLocalWrapperScript(launchDst, pidFile, pout);
      } finally {
        IOUtils.cleanup(LOG, pout, out);
      }
    }

    protected abstract void writeLocalWrapperScript(Path launchDst, Path pidFile,
        PrintStream pout);

    protected LocalWrapperScriptBuilder(Path containerWorkDir) {
      this.wrapperScriptPath = new Path(containerWorkDir,
        Shell.appendScriptExtension("default_container_executor"));
    }
  }

  private final class UnixLocalWrapperScriptBuilder
      extends LocalWrapperScriptBuilder {
    private final Path sessionScriptPath;

    public UnixLocalWrapperScriptBuilder(Path containerWorkDir) {
      super(containerWorkDir);
      this.sessionScriptPath = new Path(containerWorkDir,
          Shell.appendScriptExtension("default_container_executor_session"));
    }

    @Override
    public void writeLocalWrapperScript(Path launchDst, Path pidFile)
        throws IOException {
      writeSessionScript(launchDst, pidFile);
      super.writeLocalWrapperScript(launchDst, pidFile);
    }

    @Override
    public void writeLocalWrapperScript(Path launchDst, Path pidFile,
        PrintStream pout) {
      String exitCodeFile = ContainerLaunch.getExitCodeFile(
          pidFile.toString());
      String tmpFile = exitCodeFile + ".tmp";
      pout.println("#!/bin/bash");
      pout.println("/bin/bash \"" + sessionScriptPath.toString() + "\"");
      pout.println("rc=$?");
      pout.println("echo $rc > \"" + tmpFile + "\"");
      pout.println("/bin/mv -f \"" + tmpFile + "\" \"" + exitCodeFile + "\"");
      pout.println("exit $rc");
    }

    private void writeSessionScript(Path launchDst, Path pidFile)
        throws IOException {
      DataOutputStream out = null;
      PrintStream pout = null;
      try {
        out = containerExecutor.lfs.create(sessionScriptPath, EnumSet.of(CREATE, OVERWRITE));
        pout = new PrintStream(out, false, "UTF-8");
        // We need to do a move as writing to a file is not atomic
        // Process reading a file being written to may get garbled data
        // hence write pid to tmp file first followed by a mv
        pout.println("#!/bin/bash");
        pout.println();
        pout.println("echo $$ > " + pidFile.toString() + ".tmp");
        pout.println("/bin/mv -f " + pidFile.toString() + ".tmp " + pidFile);
        String exec = Shell.isSetsidAvailable? "exec setsid" : "exec";
        pout.println(exec + " /bin/bash \"" +
            launchDst.toUri().getPath().toString() + "\"");
      } finally {
        IOUtils.cleanup(LOG, pout, out);
      }
      containerExecutor.lfs.setPermission(sessionScriptPath,
          ContainerExecutor.TASK_LAUNCH_SCRIPT_PERMISSION);
    }
  }

  private final class WindowsLocalWrapperScriptBuilder
      extends LocalWrapperScriptBuilder {

    private final String containerIdStr;

    public WindowsLocalWrapperScriptBuilder(String containerIdStr,
        Path containerWorkDir) {

      super(containerWorkDir);
      this.containerIdStr = containerIdStr;
    }

    @Override
    public void writeLocalWrapperScript(Path launchDst, Path pidFile,
        PrintStream pout) {
      // TODO: exit code script for Windows

      // On Windows, the pid is the container ID, so that it can also serve as
      // the name of the job object created by winutils for task management.
      // Write to temp file followed by atomic move.
      String normalizedPidFile = new File(pidFile.toString()).getPath();
      pout.println("@echo " + containerIdStr + " > " + normalizedPidFile +
        ".tmp");
      pout.println("@move /Y " + normalizedPidFile + ".tmp " +
        normalizedPidFile);
      pout.println("@call " + launchDst.toString());
    }
  }

  public boolean signalContainer(Container container, String user, String pid, Signal signal)
      throws IOException {
    LOG.debug("Sending signal " + signal.getValue() + " to pid " + pid
        + " as user " + user);
    if (!containerIsAlive(pid)) {
      return false;
    }
    try {
      killContainer(pid, signal);
    } catch (IOException e) {
      if (!containerIsAlive(pid)) {
        return false;
      }
      throw e;
    }
    return true;
  }

  public boolean isContainerProcessAlive(Container container, String user, String pid)
      throws IOException {
	  if (pid == null) {
		  throw new IOException("Unable to determine pid for " + container.getContainerId());
	  }
	  return containerIsAlive(pid);
  }

  /**
   * Returns true if the process with the specified pid is alive.
   * 
   * @param pid String pid
   * @return boolean true if the process is alive
   */
  @VisibleForTesting
  public static boolean containerIsAlive(String pid) throws IOException {
    try {
      new ShellCommandExecutor(Shell.getCheckProcessIsAliveCommand(pid))
        .execute();
      // successful execution means process is alive
      return true;
    }
    catch (ExitCodeException e) {
      // failure (non-zero exit code) means process is not alive
      return false;
    }
  }

  /**
   * Send a specified signal to the specified pid
   *
   * @param pid the pid of the process [group] to signal.
   * @param signal signal to send
   * (for logging).
   */
  protected void killContainer(String pid, Signal signal) throws IOException {
    new ShellCommandExecutor(Shell.getSignalKillCommand(signal.getValue(), pid))
      .execute();
  }

  public void writeLaunchEnv(OutputStream out, Map<String, String> environment, 
 Map<Path, List<String>> resources, List<String> command) throws IOException {
		ContainerLaunch.ShellScriptBuilder sb = ContainerLaunch.ShellScriptBuilder.create();
		if (environment != null) {
			for (Map.Entry<String, String> env : environment.entrySet()) {
				sb.env(env.getKey().toString(), env.getValue().toString());
			}
		}
		if (resources != null) {
			for (Map.Entry<Path, List<String>> entry : resources.entrySet()) {
				for (String linkName : entry.getValue()) {
					sb.symlink(entry.getKey(), new Path(linkName));
				}
			}
		}

		sb.command(command);

		PrintStream pout = null;
		try {
			pout = new PrintStream(out, false, "UTF-8");
			sb.write(pout);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

}
