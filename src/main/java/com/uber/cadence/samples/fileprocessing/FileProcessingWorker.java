/*
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.uber.cadence.samples.fileprocessing;

import static com.uber.cadence.samples.common.SampleConstants.DOMAIN;

import com.uber.cadence.worker.Worker;
import java.lang.management.ManagementFactory;

/**
 * This is the process that hosts all workflows and activities in this sample. Run multiple
 * instances of the worker in different windows. Then start a workflow by running the
 * FileProcessingStarter. Note that all activities always execute on the same worker. But each time
 * they might end up on a different worker as the first activity is dispatched to the common task
 * list.
 */
public class FileProcessingWorker {

  static final String TASK_LIST = "FileProcessing";

  public static void main(String[] args) {

    String hostSpecifiTaskList = ManagementFactory.getRuntimeMXBean().getName();

    // Get worker to poll the common task list.
    Worker.Factory factory = new Worker.Factory(DOMAIN);
    final Worker workerForCommonTaskList = factory.newWorker(TASK_LIST);
    workerForCommonTaskList.registerWorkflowImplementationTypes(FileProcessingWorkflowImpl.class);
    StoreActivitiesImpl storeActivityImpl = new StoreActivitiesImpl(hostSpecifiTaskList);
    workerForCommonTaskList.registerActivitiesImplementations(storeActivityImpl);

    // Get worker to poll the host-specific task list.
    final Worker workerForHostSpecificTaskList = factory.newWorker(hostSpecifiTaskList);
    workerForHostSpecificTaskList.registerActivitiesImplementations(storeActivityImpl);

    // Start all workers created by this factory.
    factory.start();
    System.out.println("Worker started for task list: " + TASK_LIST);
    System.out.println("Worker Started for activity task List: " + hostSpecifiTaskList);
  }
}
