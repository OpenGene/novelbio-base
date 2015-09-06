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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.UnsupportedFileSystemException;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.yarn.api.records.ContainerId;
import org.apache.hadoop.yarn.factory.providers.RecordFactoryProvider;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.container.Container;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.launcher.ContainerLaunch;
import org.apache.hadoop.yarn.server.nodemanager.containermanager.localizer.ContainerLocalizer;
import org.apache.hadoop.yarn.server.nodemanager.util.ProcessIdFileReader;

public class ContainerExecutorImpt extends ContainerExecutor {
	private static final Log LOG = LogFactory.getLog(ContainerExecutorImpt.class);
	protected final FileContext lfs;
	DefaultExecutor def;
	DockerExecutor doc;

	public ContainerExecutorImpt() {
		try {
			this.lfs = FileContext.getLocalFSFileContext();
		} catch (UnsupportedFileSystemException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void init() throws IOException {
		def = new DefaultExecutor(this);
		doc = new DockerExecutor(this);
		LOG.info("configure: " + getConf());
		def.init();
		doc.init();
	}

	  /**
	   * Get the process-identifier for the container
	   * 
	   * @param containerID
	   * @return the processid of the container if it has already launched,
	   *         otherwise return null
	   */
	  public String getProcessId(ContainerId containerID) {
		  String pid = null;
		  Path pidFile = pidFiles.get(containerID);
		  if (pidFile == null) {
	  	   // This container isn't even launched yet.
			  return pid;
		  }
		  try {
			  pid = ProcessIdFileReader.getProcessId(pidFile);
		  } catch (IOException e) {
			  LOG.error("Got exception reading pid from pid-file " + pidFile, e);
		  }
		  if (pid == null) {
			  try {
				  pid = DockerExecutor.getPid(containerID.toString());
			  } catch (Exception e) {
				  LOG.error("Got exception reading pid from docker " + containerID.getContainerId(), e);
            }
		  }
		  return pid;
	  }
	  
	  /**
	   * Recover an already existing container. This is a blocking call and returns
	   * only when the container exits.  Note that the container must have been
	   * activated prior to this call.
	   * @param user the user of the container
	   * @param containerId The ID of the container to reacquire
	   * @return The exit code of the pre-existing container
	   * @throws IOException
	   * @throws InterruptedException 
	   */
	  public int reacquireContainer(Container container)
	      throws IOException, InterruptedException {
		  String user = container.getUser();
		  ContainerId containerId = container.getContainerId();
				  
	    Path pidPath = getPidFilePath(containerId);
	    if (pidPath == null) {
	      LOG.warn(containerId + " is not active, returning terminated error");
	      return ExitCode.TERMINATED.getExitCode();
	    }

	    String pid = null;
	    pid = ProcessIdFileReader.getProcessId(pidPath);
	    LOG.info("Reacquiring " + containerId + " with pid " + pid);
	    while(isContainerProcessAlive(container, user, pid)) {
	      Thread.sleep(1000);
	    }

	    // wait for exit code file to appear
	    String exitCodeFile = ContainerLaunch.getExitCodeFile(pidPath.toString());
	    File file = new File(exitCodeFile);
	    final int sleepMsec = 100;
	    int msecLeft = 2000;
	    while (!file.exists() && msecLeft >= 0) {
	      if (!isContainerActive(containerId)) {
	        LOG.info(containerId + " was deactivated");
	        return ExitCode.TERMINATED.getExitCode();
	      }
	      
	      Thread.sleep(sleepMsec);
	      
	      msecLeft -= sleepMsec;
	    }
	    if (msecLeft < 0) {
	      throw new IOException("Timeout while waiting for exit code from "
	          + containerId);
	    }

	    try {
	      return Integer.parseInt(FileUtils.readFileToString(file).trim());
	    } catch (NumberFormatException e) {
	      throw new IOException("Error parsing exit code from pid " + pid, e);
	    }
	  }
	  
	public void writeLaunchEnv(OutputStream out, Map<String, String> environment, Map<Path, List<String>> resources,
	        List<String> command) throws IOException {
		LOG.debug("write launchEnv");
		getExecutorByEnvironment(environment).writeLaunchEnv(out, environment, resources, command);
	}

	@Override
	public int launchContainer(Container container, Path nmPrivateContainerScriptPath, Path nmPrivateTokensPath, String user, String appId,
	        Path containerWorkDir, List<String> localDirs, List<String> logDirs) throws IOException {
		LOG.debug("launchContainer " + container.getContainerId().toString());
		return getExecutorByContainer(container).launchContainer(container, nmPrivateContainerScriptPath, nmPrivateTokensPath, user, appId,
		        containerWorkDir, localDirs, logDirs);
	}

	@Override
	public boolean signalContainer(Container container, String user, String pid, Signal signal) throws IOException {
		LOG.debug("signalContainer " + container.getContainerId().toString());
		return getExecutorByContainer(container).signalContainer(container, user, pid, signal);
	}

	@Override
	public boolean isContainerProcessAlive(Container container, String user, String pid) throws IOException {
		boolean isAlive = getExecutorByContainer(container).isContainerProcessAlive(container, user, pid);
		LOG.debug("isContainerProcessAlive " + container.getContainerId().toString() + " " + isAlive);
		return isAlive;
	}

	private IntExecutor getExecutorByContainer(Container container) {
		if (isDockerExecutor(container.getLaunchContext().getEnvironment())) {
			LOG.info("container " + container.toString() + "Use docker container executor");
			return doc;
		} else {
			LOG.info("container " + container.toString() + " Use default container executor");
			return def;
		}
	}
	private IntExecutor getExecutorByEnvironment(Map<String, String> environment) {
		if (isDockerExecutor(environment)) {
			LOG.info("Use docker container executor by read from environment");
			return doc;
		} else {
			LOG.info("Use docker container executor by read from environment");
			return def;
		}
	}
	
	private static boolean isDockerExecutor(Map<String, String> environment) {
		String isUseDocker = environment.get(DockerExecutor.USE_DOCKER_EXECUTOR);
		if (isUseDocker != null && (isUseDocker.toLowerCase().equals("true") || isUseDocker.toLowerCase().equals("t"))) {
			return true;
		}
		return false;
	}
	public static boolean isDockerExecutor(Container container) {
		String isUseDocker = container.getLaunchContext().getEnvironment().get(DockerExecutor.USE_DOCKER_EXECUTOR);
		if (isUseDocker != null && (isUseDocker.toLowerCase().equals("true") || isUseDocker.toLowerCase().equals("t"))) {
			return true;
		}
		return false;
	}
	@Override
	public void deleteAsUser(String user, Path subDir, Path... baseDirs) throws IOException, InterruptedException {
		if (baseDirs == null || baseDirs.length == 0) {
			LOG.info("Deleting absolute path : " + subDir);
			if (!lfs.delete(subDir, true)) {
				// Maybe retry
				LOG.warn("delete returned false for path: [" + subDir + "]");
			}
			return;
		}
		for (Path baseDir : baseDirs) {
			Path del = subDir == null ? baseDir : new Path(baseDir, subDir);
			LOG.info("Deleting path : " + del);
			if (!lfs.delete(del, true)) {
				LOG.warn("delete returned false for path: [" + del + "]");
			}
		}
	}

	public void startLocalizer(Path nmPrivateContainerTokensPath, InetSocketAddress nmAddr, String user, String appId, String locId,
	        LocalDirsHandlerService dirsHandler) throws IOException, InterruptedException {

		List<String> localDirs = dirsHandler.getLocalDirs();
		List<String> logDirs = dirsHandler.getLogDirs();

		createUserLocalDirs(localDirs, user);
		createUserCacheDirs(localDirs, user);
		createAppDirs(localDirs, user, appId);
		createAppLogDirs(appId, logDirs, user);

		// randomly choose the local directory
		Path appStorageDir = getWorkingDir(localDirs, user, appId);

		String tokenFn = String.format(ContainerLocalizer.TOKEN_FILE_NAME_FMT, locId);
		Path tokenDst = new Path(appStorageDir, tokenFn);
		copyFile(nmPrivateContainerTokensPath, tokenDst, user);
		LOG.info("Copying from " + nmPrivateContainerTokensPath + " to " + tokenDst);

		FileContext localizerFc = FileContext.getFileContext(lfs.getDefaultFileSystem(), getConf());
		localizerFc.setUMask(lfs.getUMask());
		localizerFc.setWorkingDirectory(appStorageDir);
		LOG.info("Localizer CWD set to " + appStorageDir + " = " + localizerFc.getWorkingDirectory());
		ContainerLocalizer localizer = new ContainerLocalizer(localizerFc, user, appId, locId, getPaths(localDirs),
		        RecordFactoryProvider.getRecordFactory(getConf()));
		// TODO: DO it over RPC for maintaining similarity?
		localizer.runLocalization(nmAddr);
	}

	protected void copyFile(Path src, Path dst, String owner) throws IOException {
		lfs.util().copy(src, dst);
	}

	protected void setScriptExecutable(Path script, String owner) throws IOException {
		lfs.setPermission(script, ContainerExecutor.TASK_LAUNCH_SCRIPT_PERMISSION);
	}

	/**
	 * Permissions for user dir. $local.dir/usercache/$user
	 */
	static final short USER_PERM = (short) 0750;
	/**
	 * Permissions for user appcache dir. $local.dir/usercache/$user/appcache
	 */
	static final short APPCACHE_PERM = (short) 0710;
	/**
	 * Permissions for user filecache dir. $local.dir/usercache/$user/filecache
	 */
	static final short FILECACHE_PERM = (short) 0710;
	/**
	 * Permissions for user app dir. $local.dir/usercache/$user/appcache/$appId
	 */
	static final short APPDIR_PERM = (short) 0710;
	/**
	 * Permissions for user log dir. $logdir/$user/$appId
	 */
	static final short LOGDIR_PERM = (short) 0710;

	private long getDiskFreeSpace(Path base) throws IOException {
		return lfs.getFsStatus(base).getRemaining();
	}

	private Path getApplicationDir(Path base, String user, String appId) {
		return new Path(getAppcacheDir(base, user), appId);
	}

	private Path getUserCacheDir(Path base, String user) {
		return new Path(new Path(base, ContainerLocalizer.USERCACHE), user);
	}

	private Path getAppcacheDir(Path base, String user) {
		return new Path(getUserCacheDir(base, user), ContainerLocalizer.APPCACHE);
	}

	private Path getFileCacheDir(Path base, String user) {
		return new Path(getUserCacheDir(base, user), ContainerLocalizer.FILECACHE);
	}

	protected Path getWorkingDir(List<String> localDirs, String user, String appId) throws IOException {
		Path appStorageDir = null;
		long totalAvailable = 0L;
		long[] availableOnDisk = new long[localDirs.size()];
		int i = 0;
		// randomly choose the app directory
		// the chance of picking a directory is proportional to
		// the available space on the directory.
		// firstly calculate the sum of all available space on these directories
		for (String localDir : localDirs) {
			Path curBase = getApplicationDir(new Path(localDir), user, appId);
			long space = 0L;
			try {
				space = getDiskFreeSpace(curBase);
			} catch (IOException e) {
				LOG.warn("Unable to get Free Space for " + curBase.toString(), e);
			}
			availableOnDisk[i++] = space;
			totalAvailable += space;
		}

		// throw an IOException if totalAvailable is 0.
		if (totalAvailable <= 0L) {
			throw new IOException("Not able to find a working directory for " + user);
		}

		// make probability to pick a directory proportional to
		// the available space on the directory.
		long randomPosition = RandomUtils.nextLong() % totalAvailable;
		int dir = 0;
		// skip zero available space directory,
		// because totalAvailable is greater than 0 and randomPosition
		// is less than totalAvailable, we can find a valid directory
		// with nonzero available space.
		while (availableOnDisk[dir] == 0L) {
			dir++;
		}
		while (randomPosition > availableOnDisk[dir]) {
			randomPosition -= availableOnDisk[dir++];
		}
		appStorageDir = getApplicationDir(new Path(localDirs.get(dir)), user, appId);

		return appStorageDir;
	}

	/**
	 * Initialize the local directories for a particular user.
	 * <ul>
	 * .mkdir
	 * <li>$local.dir/usercache/$user</li>
	 * </ul>
	 */
	void createUserLocalDirs(List<String> localDirs, String user) throws IOException {
		boolean userDirStatus = false;
		FsPermission userperms = new FsPermission(USER_PERM);
		for (String localDir : localDirs) {
			// create $local.dir/usercache/$user and its immediate parent
			try {
				createDir(getUserCacheDir(new Path(localDir), user), userperms, true, user);
			} catch (IOException e) {
				LOG.warn("Unable to create the user directory : " + localDir, e);
				continue;
			}
			userDirStatus = true;
		}
		if (!userDirStatus) {
			throw new IOException("Not able to initialize user directories " + "in any of the configured local directories for user " + user);
		}
	}

	protected void createDir(Path dirPath, FsPermission perms, boolean createParent, String user) throws IOException {
		lfs.mkdir(dirPath, perms, createParent);
		if (!perms.equals(perms.applyUMask(lfs.getUMask()))) {
			lfs.setPermission(dirPath, perms);
		}
	}

	/**
	 * Initialize the local cache directories for a particular user.
	 * <ul>
	 * <li>$local.dir/usercache/$user</li>
	 * <li>$local.dir/usercache/$user/appcache</li>
	 * <li>$local.dir/usercache/$user/filecache</li>
	 * </ul>
	 */
	void createUserCacheDirs(List<String> localDirs, String user) throws IOException {
		LOG.info("Initializing user " + user);

		boolean appcacheDirStatus = false;
		boolean distributedCacheDirStatus = false;
		FsPermission appCachePerms = new FsPermission(APPCACHE_PERM);
		FsPermission fileperms = new FsPermission(FILECACHE_PERM);

		for (String localDir : localDirs) {
			// create $local.dir/usercache/$user/appcache
			Path localDirPath = new Path(localDir);
			final Path appDir = getAppcacheDir(localDirPath, user);
			try {
				createDir(appDir, appCachePerms, true, user);
				appcacheDirStatus = true;
			} catch (IOException e) {
				LOG.warn("Unable to create app cache directory : " + appDir, e);
			}
			// create $local.dir/usercache/$user/filecache
			final Path distDir = getFileCacheDir(localDirPath, user);
			try {
				createDir(distDir, fileperms, true, user);
				distributedCacheDirStatus = true;
			} catch (IOException e) {
				LOG.warn("Unable to create file cache directory : " + distDir, e);
			}
		}
		if (!appcacheDirStatus) {
			throw new IOException("Not able to initialize app-cache directories " + "in any of the configured local directories for user " + user);
		}
		if (!distributedCacheDirStatus) {
			throw new IOException("Not able to initialize distributed-cache directories " + "in any of the configured local directories for user "
			        + user);
		}
	}

	/**
	 * Initialize the local directories for a particular user.
	 * <ul>
	 * <li>$local.dir/usercache/$user/appcache/$appid</li>
	 * </ul>
	 * 
	 * @param localDirs
	 */
	void createAppDirs(List<String> localDirs, String user, String appId) throws IOException {
		boolean initAppDirStatus = false;
		FsPermission appperms = new FsPermission(APPDIR_PERM);
		for (String localDir : localDirs) {
			Path fullAppDir = getApplicationDir(new Path(localDir), user, appId);
			// create $local.dir/usercache/$user/appcache/$appId
			try {
				createDir(fullAppDir, appperms, true, user);
				initAppDirStatus = true;
			} catch (IOException e) {
				LOG.warn("Unable to create app directory " + fullAppDir.toString(), e);
			}
		}
		if (!initAppDirStatus) {
			throw new IOException("Not able to initialize app directories " + "in any of the configured local directories for app "
			        + appId.toString());
		}
	}

	/**
	 * Create application log directories on all disks.
	 */
	void createAppLogDirs(String appId, List<String> logDirs, String user) throws IOException {

		boolean appLogDirStatus = false;
		FsPermission appLogDirPerms = new FsPermission(LOGDIR_PERM);
		for (String rootLogDir : logDirs) {
			// create $log.dir/$appid
			Path appLogDir = new Path(rootLogDir, appId);
			try {
				createDir(appLogDir, appLogDirPerms, true, user);
			} catch (IOException e) {
				LOG.warn("Unable to create the app-log directory : " + appLogDir, e);
				continue;
			}
			appLogDirStatus = true;
		}
		if (!appLogDirStatus) {
			throw new IOException("Not able to initialize app-log directories " + "in any of the configured local directories for app " + appId);
		}
	}

	/**
	 * Create application log directories on all disks.
	 */
	void createContainerLogDirs(String appId, String containerId, List<String> logDirs, String user) throws IOException {

		boolean containerLogDirStatus = false;
		FsPermission containerLogDirPerms = new FsPermission(LOGDIR_PERM);
		for (String rootLogDir : logDirs) {
			// create $log.dir/$appid/$containerid
			Path appLogDir = new Path(rootLogDir, appId);
			Path containerLogDir = new Path(appLogDir, containerId);
			try {
				createDir(containerLogDir, containerLogDirPerms, true, user);
			} catch (IOException e) {
				LOG.warn("Unable to create the container-log directory : " + appLogDir, e);
				continue;
			}
			containerLogDirStatus = true;
		}
		if (!containerLogDirStatus) {
			throw new IOException("Not able to initialize container-log directories " + "in any of the configured local directories for container "
			        + containerId);
		}
	}

	/**
	 * @return the list of paths of given local directories
	 */
	private static List<Path> getPaths(List<String> dirs) {
		List<Path> paths = new ArrayList<Path>(dirs.size());
		for (int i = 0; i < dirs.size(); i++) {
			paths.add(new Path(dirs.get(i)));
		}
		return paths;
	}

}
