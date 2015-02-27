package com.novelbio.base.curator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.novelbio.PathDetailService;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.dbInfo.model.project.EnumTaskState;
import com.novelbio.dbInfo.model.project.EnumTaskType;
import com.novelbio.dbInfo.model.project.NBCTask;
import com.novelbio.web.service.task.EnumTaskAssignType;
import com.novelbio.web.service.task.TaskAssignInt;
import com.novelbio.web.service.task.TaskFactory;

public class QueueTask extends SimpleDistributedQueue<NBCTask> implements TaskAssignInt, Runnable, Watcher {
	private static final Logger logger = Logger.getLogger(QueueTask.class);
	
	TaskRunBranch taskRunBranch;
	ComputerTask computer;
	InterProcessMutex curatorLock;
	/** 用来通知客户端提取task的path */
	String pathNotify;
	long thisTime = 0;
	
	private QueueTask(CuratorFramework client, String path, String pathNotify, ComputerTask computer, TaskRunBranch taskRunBranch) {
		super(client, path);

		this.computer = computer;
		this.taskRunBranch = taskRunBranch;
		this.curatorLock = new InterProcessMutex(client, path + "TaskQueueLock");
		this.pathNotify = pathNotify;
        try {
			if (client.checkExists().forPath(pathNotify) == null) {
				client.create().creatingParentsIfNeeded().forPath(pathNotify);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 添加task，并将taskid做为节点名，可以方便被 {@link SimpleDistributedQueue#getLsNodeName()}
	 * 所获取<br>
	 * 同时在pathNotify里面写上已经运行的task数量
	 */
	public String offer(NBCTask taskInfo) {
		String result = super.offerNoSequential(taskInfo, taskInfo.getTaskId());
		Long taskNum = 0L;
		try {
			taskNum = (Long)serializeKryo.read(client.getData().forPath(pathNotify));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (taskNum < 0) {
			taskNum = 0L;
		}
		taskNum++;
		byte[] byteTaskNum = serializeKryo.write(taskNum);
		try {
			client.setData().forPath(pathNotify, byteTaskNum);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 把任务从等待队列中移除,如果没找到任务,返回false
	 * @param taskId
	 * @return
	 */
	public boolean removeTaskFromQueue(String taskId) {
		return super.removeTaskNoSequential(taskId);
	}
    
	/**
	 * <b>排队和等待什么的就写在这里吧</b>
	 * <p>
	 * 
	 * 给定computer信息，返回当前比较合适的可以执行的任务 没有就返回null
	 * 
	 * @param computer
	 * @return
	 */
	public List<NBCTask> getTaskCanBeRun(ComputerTask computer) {
		List<NBCTask> lsNbcTasks = new ArrayList<>();
		try {
			ensurePath.ensure(client.getZookeeperClient());
		} catch (Exception e) {
			return null;
		}

		List<String> lsNodes;
		try {
			lsNodes = client.getChildren().forPath(path);
		} catch (Exception e) {
			return null;
		}
		Collections.sort(lsNodes);

		for (String node : lsNodes) {
			String thisPath = ZKPaths.makePath(path, node);
			if (!node.startsWith(PREFIX)) {
				logger.warn("Foreign node in queue path: " + node);
				try {
					client.delete().forPath(thisPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}

			try {
				byte[] bytes = client.getData().forPath(thisPath);
				NBCTask task = (NBCTask) serializeKryo.read(bytes);
				if (computer.isCanRunTask(task)) {
					client.delete().forPath(thisPath);
					lsNbcTasks.add(task);
					computer.addTaskInfo(task);
				} else {
					logger.info(
							computer.getName() + " cannot run task: " + task.getTaskId() + "\t"
							+ "client_cpu:" + computer.getCpuCoreFreeInDB() + " client_mem:" + computer.getMemoryFreeDB() + " client_io:" + computer.getIo()
							+ " task_cpu:" + task.getTaskCPUs() + " task_mem:" + task.getTaskMemory() + " task_io:" + task.getTaskIO() + " task_is_test:" + task.isTestTask() + " is_just_get_test_task:" + computer.isJustGetTestTask() );
				}
			} catch (KeeperException.NoNodeException ignore) {
				// Another client removed the node first, try next
			} catch (Exception e) {
				logger.error(computer.getNameIP() + " " + thisPath, e);
			}
		}
		return lsNbcTasks;
	}
	
	/**
	 * 得到队列中的所有任务信息
	 * @return
	 */
	public HashSet<NBCTask> getAllTaskInfo() {
		try {
			ensurePath.ensure(client.getZookeeperClient());
		} catch (Exception e) {
			return null;
		}

		List<String> lsNodes;
		try {
			lsNodes = client.getChildren().forPath(path);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		HashSet<NBCTask> setTaskInfos = new LinkedHashSet<>();
		for (String node : lsNodes) {
			String thisPath = ZKPaths.makePath(path, node);
			if (!node.startsWith(PREFIX)) {
				logger.warn("Foreign node in queue path: " + node);
				try {
					client.delete().forPath(thisPath);
				} catch (Exception e) {
					e.printStackTrace();
				}
				continue;
			}

			try {
				byte[] bytes = client.getData().forPath(thisPath);
				NBCTask task = (NBCTask) serializeKryo.read(bytes);
				setTaskInfos.add(task);
			} catch (KeeperException.NoNodeException ignore) {
				// Another client removed the node first, try next
			} catch (Exception e) {
				
			}
		}
		return setTaskInfos;
	}
    
	/**
	 * 有锁的机制，并且会查找本queue队列，run队列和finish队列，如果三个队列中有一个存在了同id的taskinfo，就会返回false表示不需要添加
	 * 并保存入数据库
	 */
	@Override
	public EnumTaskAssignType assign_And_Save_Task(NBCTask taskInfo) {
		EnumTaskAssignType taskAssignType = EnumTaskAssignType.Add_Fail;
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		
		synchronized (curatorLock) {
			try {
				curatorLock.acquire();
				Set<String> setTaskIDs = new HashSet<>();
				setTaskIDs.addAll(getLsNodeName());
				setTaskIDs.addAll(taskRunBranch.getLsTaskIdRunAll());
				setTaskIDs.addAll(taskRunBranch.getLsTaskFinishIdAll());
				if (setTaskIDs.contains(taskInfo.getTaskId())) {
					taskAssignType = EnumTaskAssignType.Exist_Duplicate;
				} else {
					if(taskInfo.getTaskType() == EnumTaskType.Other) {
						taskInfo.save();
						taskAssignType = EnumTaskAssignType.Add_Sucess;
					} else {
						TaskFactory.fillTaskInfo(taskInfo);
						taskInfo.setTaskState(EnumTaskState.等待中);
						taskInfo.save();
						taskAssignType = assignTaskToQueue(taskInfo);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					curatorLock.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		

		logger.info("add task consume time: " + dateUtil.getElapseTime());

		return taskAssignType;
	}
	
	/** 将task添加入queue，返回是否添加成功 */
	private EnumTaskAssignType assignTaskToQueue(NBCTask taskInfo) {
		return offer(taskInfo) != null ?
				EnumTaskAssignType.Add_Sucess : EnumTaskAssignType.Add_Fail ;
	}
	
	/** 
	 * 内部实现分布式锁<p>
	 * 删除队列中的重复taskID的项目，如果一个项目在队列中和运行中都存在，则删除队列中的项目
	 * ，考虑添加入schedule<br>
	 * <b>已经跑起来的重复task就不删除了</b>
	 */
	public boolean removeDuplicatTask() {
		boolean isSucess = false;
		CuratorLock lock = new CuratorLock(client, path+"sharelock_remove_task");
		try {
			if(lock.acquire()) {
				List<String> lsTaskInfosRun = TaskRunBranch.getInstance().getLsTaskIdRunAll();
				Set<String> setTaskID = new HashSet<>(lsTaskInfosRun);
				List<String> lsNodes = client.getChildren().forPath(path);
				Collections.sort(lsNodes);

				for (String node : lsNodes) {
					String thisPath = ZKPaths.makePath(path, node);
					if (!node.startsWith(PREFIX)) {
						logger.warn("Foreign node in queue path: " + node);
						try {
							client.delete().forPath(thisPath);
						} catch (Exception e) { }
						continue;
					}

					try {
						byte[] bytes = client.getData().forPath(thisPath);
						NBCTask task = (NBCTask) serializeKryo.read(bytes);
						if (setTaskID.contains(task.getTaskId())) {
							client.delete().forPath(thisPath);
						} else {
							setTaskID.add(task.getTaskId());
						}
					} catch (KeeperException.NoNodeException ignore) { }
				}
				
			}
			isSucess = true;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			lock.release();
		}
		return isSucess;
	}
	
	static class QueueTaskHolder {
		static QueueTask queueTask = new QueueTask(PathDetailService.getClient(), PathDetailService.getZnodeTaskQueueNoSep(),
				PathDetailService.getZnodeTaskNotifyNoSep(),
				ComputerTask.getInstance(), TaskRunBranch.getInstance());
	}
	
	public static QueueTask getInstance() {
		return QueueTaskHolder.queueTask;
	}
	
	@Override
	public void run() {
		getTaskToRun();
	}
	@Override
	public void process(WatchedEvent watchedEvent) {
		if (watchedEvent.getState() == KeeperState.Disconnected || watchedEvent.getState() == KeeperState.Expired 
				|| watchedEvent.getState() == KeeperState.AuthFailed) {
			return;
		}
		getTaskToRun();
	}
	
	public synchronized void getTaskToRun() {
		if (DateUtil.getNowTimeLong() - thisTime > 60*1000 && TaskFactory.isEmpty()) {
			computer.initial();
			thisTime = DateUtil.getNowTimeLong();
		}
		logger.info(computer.getNameIP() + " start get task");

		if (computer.isCanDoTask() && client.getZookeeperClient().isConnected() && !isEmpty()) {
			List<NBCTask> lsTasks = getTaskCanBeRun(computer);
			logger.info(computer.getNameIP() + " computer cpu free database:" + computer.getCpuCoreFreeInDB() + " free real:"+ computer.getCpuCoreFreeReal());
			logger.info(computer.getNameIP() + " computer memory free database:" + computer.getMemoryFreeDB() + "free real:" + computer.getMemoryFreeReal());
			logger.info(computer.getNameIP() + " computer io free:" + computer.getIo());
			
			for (NBCTask nbcTask : lsTasks) {
				if (nbcTask != null && !taskRunBranch.addAndCreateTaskInfo(computer, nbcTask)) {
					logger.info(computer.getNameIP() + " task run error, set to queue: " + nbcTask.getTaskId() + " " + nbcTask.getTaskType().toString());
					offer(nbcTask);
					continue;//任务没有添加成功则返回
				}
				logger.info(computer.getNameIP() + " run task:" + nbcTask.getTaskId() + " " + nbcTask.getTaskType().toString());
			}
		}
		try {
			client.checkExists().usingWatcher(this).forPath(pathNotify);
		} catch (Exception e) {
			logger.error(e);
		}
	}

}


