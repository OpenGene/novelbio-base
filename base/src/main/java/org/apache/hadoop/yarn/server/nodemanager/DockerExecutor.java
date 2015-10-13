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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.CommonConfigurationKeys;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Shell;
import org.apache.hadoop.util.Shell.ExitCodeException;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.api.ApplicationConstants;
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
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * This executor will launch a docker container and run the task inside the container.
 */
public class DockerExecutor implements IntExecutor {

  private static final Log LOG = LogFactory
      .getLog(DockerExecutor.class);
  public static final String DOCKER_CONTAINER_EXECUTOR_SCRIPT = "docker_container_executor";
  public static final String DOCKER_CONTAINER_EXECUTOR_SESSION_SCRIPT = "docker_container_executor_session";

  /** docker 相关的挂载代码 */
  public static final String DOCKER_CONTAINER_MOUNT = "docker_container_mount";
  public static final String USE_DOCKER_EXECUTOR = "docker_executor";

  // This validates that the image is a proper docker image and would not crash docker.
  public static final String DOCKER_IMAGE_PATTERN = "^(([\\w\\.-]+)(:\\d+)*\\/)?[\\w\\.:-]+$";

  private final Pattern dockerImagePattern;
  ContainerExecutorImpt containerExecutorImpt;
  
  public DockerExecutor(ContainerExecutorImpt containerExecutorImpt) {
	  this.containerExecutorImpt = containerExecutorImpt;
	  this.dockerImagePattern = Pattern.compile(DOCKER_IMAGE_PATTERN);
  }

  public void init() throws IOException {
    String auth = containerExecutorImpt.getConf().get(CommonConfigurationKeys.HADOOP_SECURITY_AUTHENTICATION);
    if (auth != null && !auth.equals("simple")) {
      throw new IllegalStateException("DockerContainerExecutor only works with simple authentication mode");
    }
    String dockerExecutor = containerExecutorImpt.getConf().get(YarnConfiguration.NM_DOCKER_CONTAINER_EXECUTOR_EXEC_NAME,
      YarnConfiguration.NM_DEFAULT_DOCKER_CONTAINER_EXECUTOR_EXEC_NAME);
    if (!new File(dockerExecutor).exists()) {
      throw new IllegalStateException("Invalid docker exec path: " + dockerExecutor);
    }
  }

  public int launchContainer(Container container,
                             Path nmPrivateContainerScriptPath, Path nmPrivateTokensPath,
                             String userName, String appId, Path containerWorkDir,
                             List<String> localDirs, List<String> logDirs) throws IOException {
    String containerImageName = container.getLaunchContext().getEnvironment()
        .get(YarnConfiguration.NM_DOCKER_CONTAINER_EXECUTOR_IMAGE_NAME);
    if (LOG.isDebugEnabled()) {
      LOG.debug("containerImageName from launchContext: " + containerImageName);
    }
    Preconditions.checkArgument(!Strings.isNullOrEmpty(containerImageName), "Container image must not be null");
    containerImageName = containerImageName.replaceAll("['\"]", "");

    Preconditions.checkArgument(saneDockerImage(containerImageName), "Image: " + containerImageName + " is not a proper docker image");
    String dockerExecutor = containerExecutorImpt.getConf().get(YarnConfiguration.NM_DOCKER_CONTAINER_EXECUTOR_EXEC_NAME,
        YarnConfiguration.NM_DEFAULT_DOCKER_CONTAINER_EXECUTOR_EXEC_NAME);

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
      Path userdir = new Path(usersdir, userName);
      Path appCacheDir = new Path(userdir, ContainerLocalizer.APPCACHE);
      Path appDir = new Path(appCacheDir, appIdStr);
      Path containerDir = new Path(appDir, containerIdStr);
      containerExecutorImpt.createDir(containerDir, dirPerm, true, userName);
    }

    // Create the container log-dirs on all disks
    containerExecutorImpt.createContainerLogDirs(appIdStr, containerIdStr, logDirs, userName);

    Path tmpDir = new Path(containerWorkDir,
        YarnConfiguration.DEFAULT_CONTAINER_TEMP_DIR);
    containerExecutorImpt.createDir(tmpDir, dirPerm, false, userName);

    // copy launch script to work dir
    Path launchDst =
        new Path(containerWorkDir, ContainerLaunch.CONTAINER_SCRIPT);
    containerExecutorImpt.lfs.util().copy(nmPrivateContainerScriptPath, launchDst);

    // copy container tokens to work dir
    Path tokenDst =
        new Path(containerWorkDir, ContainerLaunch.FINAL_CONTAINER_TOKENS_FILE);
    containerExecutorImpt.lfs.util().copy(nmPrivateTokensPath, tokenDst);

    String cpuMemLimit = getCpuMem(container);

    String localDirMount = toMount(localDirs);
    String logDirMount = toMount(logDirs);
    
   String mountCustomer = getMountPath2IsRw(container);
    
    String containerWorkDirMount = toMount(Collections.singletonList(containerWorkDir.toUri().getPath()));
    StringBuilder commands = new StringBuilder();
    String commandStr = commands.append(dockerExecutor)
        .append(" ")
        .append("run")
        .append(" ")
        .append("--rm --net=host")
        .append(" ")
        .append(" --name " + containerIdStr)
        .append(cpuMemLimit)
        .append(localDirMount)
        .append(logDirMount)
        .append(containerWorkDirMount)
        .append(mountCustomer)
        .append(" ")
        .append(containerImageName)
        .toString();
    String dockerPidScript = "";// "`" + dockerExecutor + " inspect --format {{.State.Pid}} " + containerIdStr + "`";
    // Create new local launch wrapper script
    LocalWrapperScriptBuilder sb =
      new UnixLocalWrapperScriptBuilder(containerWorkDir, commandStr, dockerPidScript);
    Path pidFile = containerExecutorImpt.getPidFilePath(containerId);
    if (pidFile != null) {
      sb.writeLocalWrapperScript(launchDst, pidFile);
    } else {
      LOG.info("Container " + containerIdStr
          + " was marked as inactive. Returning terminated error");
      return ExitCode.TERMINATED.getExitCode();
    }
    
    ShellCommandExecutor shExec = null;
    try {
    	containerExecutorImpt.lfs.setPermission(launchDst,
          ContainerExecutor.TASK_LAUNCH_SCRIPT_PERMISSION);
    	containerExecutorImpt.lfs.setPermission(sb.getWrapperScriptPath(),
          ContainerExecutor.TASK_LAUNCH_SCRIPT_PERMISSION);

      // Setup command to run
      String[] command = containerExecutorImpt.getRunCommand(sb.getWrapperScriptPath().toString(),
        containerIdStr, userName, pidFile, containerExecutorImpt.getConf());
//      if (LOG.isDebugEnabled()) {
//        LOG.debug("launchContainer: " + commandStr + " " + Joiner.on(" ").join(command));
//      }
      LOG.info("launchContainer: " + commandStr + " " + Joiner.on(" ").join(command));
      shExec = new ShellCommandExecutor(
          command,
          new File(containerWorkDir.toUri().getPath()),
          container.getLaunchContext().getEnvironment());      // sanitized env
      if (containerExecutorImpt.isContainerActive(containerId)) {
        shExec.execute();
      } else {
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
            + containerId + " and exit code: " + exitCode, e);
        containerExecutorImpt.logOutput(shExec.getOutput());
        String diagnostics = "Exception from container-launch: \n"
            + StringUtils.stringifyException(e) + "\n" + shExec.getOutput();
        container.handle(new ContainerDiagnosticsUpdateEvent(containerId,
            diagnostics));
      } else {
        container.handle(new ContainerDiagnosticsUpdateEvent(containerId,
            "Container killed on request. Exit code is " + exitCode));
      }
      return exitCode;
    } finally {
      if (shExec != null) {
        shExec.close();
      }
    }
    return 0;
  }
  
  private String getMountPath2IsRw(Container container) {
	  StringBuilder builder = new StringBuilder();
	  String mountInfo = container.getLaunchContext().getEnvironment().get(DOCKER_CONTAINER_MOUNT);
	  if (mountInfo == null) mountInfo = "";
	  
	  String[] ss = mountInfo.trim().split(";");
	  for (String pathFrom_to_rw : ss) {
		  if (pathFrom_to_rw.equals("")) continue;
		  builder.append(" -v " + pathFrom_to_rw.trim() );
	  }
	  return builder.toString();
  }
  
  private String getCpuMem(Container container) {
	  Resource resource = container.getResource();
	  int cpu = resource.getVirtualCores() *10;
	  if (cpu == 0) cpu = 5;
	  int mem = resource.getMemory();
	 return " -c " + cpu + " -m " + mem + "m ";
  }

  public void writeLaunchEnv(OutputStream out, Map<String, String> environment, Map<Path, List<String>> resources, List<String> command) throws IOException {
    ContainerLaunch.ShellScriptBuilder sb = ContainerLaunch.ShellScriptBuilder.create();

    Set<String> exclusionSet = new HashSet<String>();
    exclusionSet.add(YarnConfiguration.NM_DOCKER_CONTAINER_EXECUTOR_IMAGE_NAME);
    exclusionSet.add(DOCKER_CONTAINER_MOUNT);
    exclusionSet.add(USE_DOCKER_EXECUTOR);
    exclusionSet.add(ApplicationConstants.Environment.HADOOP_YARN_HOME.name());
    exclusionSet.add(ApplicationConstants.Environment.HADOOP_COMMON_HOME.name());
    exclusionSet.add(ApplicationConstants.Environment.HADOOP_HDFS_HOME.name());
    exclusionSet.add(ApplicationConstants.Environment.HADOOP_CONF_DIR.name());
    exclusionSet.add(ApplicationConstants.Environment.JAVA_HOME.name());

    if (environment != null) {
      for (Map.Entry<String,String> env : environment.entrySet()) {
        if (!exclusionSet.contains(env.getKey())) {
          sb.env(env.getKey().toString(), env.getValue().toString());
        }
      }
    }
    if (resources != null) {
      for (Map.Entry<Path,List<String>> entry : resources.entrySet()) {
        for (String linkName : entry.getValue()) {
          sb.symlink(entry.getKey(), new Path(linkName));
        }
      }
    }

    sb.command(command);

    PrintStream pout = null;
    PrintStream ps = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      pout = new PrintStream(out, false, "UTF-8");
      if (LOG.isDebugEnabled()) {
        ps = new PrintStream(baos, false, "UTF-8");
        sb.write(ps);
      }
      sb.write(pout);

    } finally {
      if (out != null) {
        out.close();
      }
      if (ps != null) {
        ps.close();
      }
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Script: " + baos.toString("UTF-8"));
    }
  }

  private boolean saneDockerImage(String containerImageName) {
    return dockerImagePattern.matcher(containerImageName).matches();
  }

  public boolean signalContainer(Container container, String user, String pid, Signal signal)
    throws IOException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Sending signal " + signal.getValue() + " to container " + container.getContainerId().toString()
        + " as user " + user);
    }
    if (!containerIsAliveForContainerId(container.getContainerId())) {
      return false;
    }
    try {
      killContainer(container, signal);
    } catch (IOException e) {
      if (!containerIsAliveForContainerId(container.getContainerId())) {
        return false;
      }
      throw e;
    }
    return true;
  }

  public boolean isContainerProcessAlive(Container container, String user, String pid)
    throws IOException {
    return containerIsAliveForContainerId(container.getContainerId());
  }

  /**
   * Returns true if the process with the specified pid is alive.
   *
   * @param pid String pid
   * @return boolean true if the process is alive
   */
  private static boolean containerIsAliveForContainerId(ContainerId containerId) throws IOException {
	  return containerIsAliveForContainerId(containerId.toString());
  }
  @VisibleForTesting
  public static boolean containerIsAliveForContainerId(String containerId) throws IOException {
    try {
		ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor(new String[]{"docker","top", containerId});
		shellCommandExecutor.execute();
		return true;
    } catch (Shell.ExitCodeException e) {
    	return false;
    }
  }
  
  public static String getPid(String containerId) throws IOException {
	  ShellCommandExecutor shellCommandExecutor = new ShellCommandExecutor(new String[]{"docker","top", containerId});
	  try {
		  shellCommandExecutor.execute();
	  } catch (Exception e) {
		  return null;
	  }

	  String outString = shellCommandExecutor.getOutput();
	  String pid = null;
	  for (String content : outString.split("\n")) {
		  if (content.startsWith("UID")) {
			  continue;
		  }
		  String[] ss = content.split(" ");
		  for (int i = 1; i < ss.length; i++) {
			  String info = ss[i].trim();
			  if (info != null && !info.trim().equals("")) {
				  pid = info;
				  break;
			  }
		  }
	  }
	  return pid;
  }
  
  @VisibleForTesting
  @Deprecated
  public static boolean containerIsAlive(String pid) throws IOException {
    try {
      new ShellCommandExecutor(Shell.getCheckProcessIsAliveCommand(pid))
        .execute();
      // successful execution means process is alive
      return true;
    }
    catch (Shell.ExitCodeException e) {
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
  private void killContainer(Container container, Signal signal) throws IOException {
	  ShellCommandExecutor shellExe = null;
	  if (signal == Signal.KILL) {
		  shellExe = new ShellCommandExecutor(new String[]{"docker","kill", container.getContainerId().toString()});
	  } else if (signal == Signal.TERM) {
		  shellExe = new ShellCommandExecutor(new String[]{"docker","kill", container.getContainerId().toString()});
	} else if (signal == Signal.QUIT) {
		 shellExe = new ShellCommandExecutor(new String[]{"docker","kill", container.getContainerId().toString()});
	} else {
		return;
	}
	  try {
		  shellExe.execute();
	  } catch (ExitCodeException e) {
		  if (!e.getMessage().contains("No such container")) {
		        LOG.error("kill container " + container.getContainerId() + " error", e);
		  }
	  }
  }

  /**
   * Converts a directory list to a docker mount string
   * @param dirs
   * @return a string of mounts for docker
   */
  private String toMount(List<String> dirs) {
    StringBuilder builder = new StringBuilder();
    for (String dir : dirs) {
      builder.append(" -v " + dir + ":" + dir);
    }
    return builder.toString();
  }

  private abstract class LocalWrapperScriptBuilder {

    private final Path wrapperScriptPath;

    public Path getWrapperScriptPath() {
      return wrapperScriptPath;
    }

    public void writeLocalWrapperScript(Path launchDst, Path pidFile) throws IOException {
      DataOutputStream out = null;
      PrintStream pout = null;

      try {
        out = containerExecutorImpt.lfs.create(wrapperScriptPath, EnumSet.of(CREATE, OVERWRITE));
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
          Shell.appendScriptExtension(DOCKER_CONTAINER_EXECUTOR_SCRIPT));
    }
  }

  private final class UnixLocalWrapperScriptBuilder
      extends LocalWrapperScriptBuilder {
    private final Path sessionScriptPath;
    private final String dockerCommand;
    private final String dockerPidScript;

    public UnixLocalWrapperScriptBuilder(Path containerWorkDir, String dockerCommand, String dockerPidScript) {
      super(containerWorkDir);
      this.dockerCommand = dockerCommand;
      this.dockerPidScript = dockerPidScript;
      this.sessionScriptPath = new Path(containerWorkDir,
        Shell.appendScriptExtension(DOCKER_CONTAINER_EXECUTOR_SESSION_SCRIPT));
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
      pout.println("#!/usr/bin/env bash");
      pout.println("bash \"" + sessionScriptPath.toString() + "\"");
      pout.println("rc=$?");
      pout.println("echo $rc > \"" + tmpFile + "\"");
      pout.println("mv -f \"" + tmpFile + "\" \"" + exitCodeFile + "\"");
      pout.println("exit $rc");
    }

    private void writeSessionScript(Path launchDst, Path pidFile)
      throws IOException {
      DataOutputStream out = null;
      PrintStream pout = null;
      try {
        out = containerExecutorImpt.lfs.create(sessionScriptPath, EnumSet.of(CREATE, OVERWRITE));
        pout = new PrintStream(out, false, "UTF-8");
        // We need to do a move as writing to a file is not atomic
        // Process reading a file being written to may get garbled data
        // hence write pid to tmp file first followed by a mv
        pout.println("#!/usr/bin/env bash");
        pout.println();
        pout.println("echo "+ dockerPidScript +" > " + pidFile.toString() + ".tmp");
        pout.println("/bin/mv -f " + pidFile.toString() + ".tmp " + pidFile);
        pout.println(dockerCommand + " bash \"" +
          launchDst.toUri().getPath().toString() + "\"");
      } finally {
        IOUtils.cleanup(LOG, pout, out);
      }
      containerExecutorImpt.lfs.setPermission(sessionScriptPath,
        ContainerExecutor.TASK_LAUNCH_SCRIPT_PERMISSION);
    }
  }

}