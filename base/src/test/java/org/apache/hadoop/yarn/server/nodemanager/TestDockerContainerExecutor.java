package org.apache.hadoop.yarn.server.nodemanager;

import java.io.IOException;

public class TestDockerContainerExecutor {
	public static void main(String[] args) throws IOException {
	    System.out.println(DockerContainerExecutor.containerIsAliveForContainerId("ss"));
    }
}
