# Jenkins shared library

### Function reference
#### deployOpenShiftTemplate
This function leverages the kubernetes plugin for Jenkins. It deploys a podTemplate and containers.
Parameters:
- ocContainers: A list of containers to deploy, specified by the image name in openshift.
- ocContainersWithProps: A Map with the container name as the key and values of the container tag, privileged (true/false)
and the command to run.
- openshift_namespace: The namespace openshift runs in. Defaults to continuous-infra.
- docker_repo_url: The url of the docker repository. Defaults to docker-registry.default.svc:5000.
- podName: The name of the pod that gets deployed. Defaults to generic-${UUID.randomUUID().toString()}.
- openshift_service_account: The openshift service account. Defaults to jenkins.
- jenkins_slave_image: The jnlp image to use. You must specify image:tag. Defaults to jenkins-continuous-infra-slave:stable.
```groovy
deployOpenShiftTemplate(containers: ['rpmbuild-container'], openshift_namespace: 'default')
```
#### ciPipeline
This function wraps the whole pipeline in a try/catch/finally block while accepting parameters to initialize and tear down
the pipeline.
Parameters:
- buildPrefix: A prefix to set that describes the build. This is mainly used for metrics.
- package_name: If building a package like an RPM set this to its name. e.g. 'vim'
- errorMsg: A fedMsgError to send on pipeline failure.
- completeMsg: A fedMsgComplete to send on pipeline completion.
- decorateBuild: A Closure that decorates the build such as the function decoratePRBuild()
- preBuild: A closure that contains pre build steps.
- postBuild: A Closure that contains any post build steps. e.g. ArtifactArchiver step.
- timeout: Set to time the pipeline out after timeout minutes. Defaults to 30.
- sendMetrics: Whether to send metrics to influxdb. true or false.
```groovy
ciPipeline(buildPrefix: 'myrpmbuilder', decorateBuild: {currentBuild.displayName: 'env.BUILD_ID'})
```
#### executeInContainer
This function executes a script in a container. It's wrapped by handlePipelineStep.
Parameters:
- containerName: The name of the container to use. This corresponds to the container name set in deployOpenShiftTemplate.
- containerScript: The shell command to run.
- stageVars: A Map containing the environment variables to pass to the container.
- loadProps: Properties to load from a previously run stage. This accepts a list of stage names and will load a properties file
from ${stageName}/job.props.
- credentials: Credentials to pass to the container as environment variables. This accepts a list of credentials loaded
from Jenkins
```groovy
executeInContainer(containerName: 'rpmbuild-container', containerScript: 'echo success', stageVars: ['var1': 'val1'],
                        credentials: credentials)
```

#### stageTrigger
Jenkins job to listen for changes to a container, build the image and tag it with the PR #.
```groovy
stageTrigger(containers: ['rpmbuild', image-compose'], scheduledJob: 'fedora-rawhide-build')
```

#### Metrics
Pipeline metrics are collected throughout the run of a pipeline when using the handlePipelineStep and ciPipeline libraries.
These two libraries will collect basic metrics that include:
- Total build time
- Stage run time
- Build number
- Project name
- Time spent in build queue

In addition to the default collected metrics, you can send custom metrics to Influxdb by calling the writeToInflux library.

```groovy
writeToInflux(customData: ['build_time': 100], customDataMap: ['mybuild': ['build_time': 100]])
```
After the pipeline finishes, the ciPipeline library will send all collected metrics to Influxdb.

#### Job DSL Support
Libraries to support the [Jenkins Job DSL](https://github.com/jenkinsci/job-dsl-plugin) are located in src/org/centos/contra/jobdsl.

The current supported libraries are MutliBranchJob and PipelineJob.
##### MultiBranchJob
This library supports creating a [Multi Branch Pipeline](https://plugins.jenkins.io/workflow-multibranch)
```groovy
import org.centos.contra.jobdsl.MultiBranchJob

def job = new MultiBranchJob(this, name)
job.addGitHub('contra-lib', 'openshift')
job.addComment("\\[test\\]")
job.addScriptPath('pipelineFiles/Jenkinsfile')
job.discardOldBranches()
```
##### PipelineJob
This library supports create a [Pipeline Job](https://jenkins.io/doc/book/pipeline/)
```groovy
import org.centos.contra.jobdsl.PipelineJob

def job = new PipelineJob(this, 'samplePipelineJob')
job.fedMsgTrigger('org.fedoraprojectb', 'fedora-fedmsg', ['check1': 'value1'])
job.addGit([branch: 'master', repoUrl: 'https://github.com/CentOS-PaaS-SIG/contra-env-sample-project.git'])
job.logRotate()

```

#### Example Usage:
```
package_name = env.CI_MESSAGE['name']
msgHeader = fedMsgHeader(branch: 'fed_repo', topic: '/fedMsgTopic', username: 'currentUser')
msgComplete = fedMsgComplete(header: header)
msgError = fedMsgError(header: header)

// containers to be deployed
containers = ['rpmbuild', 'singlehost-test']

// deploy an openshift pod template with containers
deployOpenShiftTemplate(containers: containers) {

    /* wrap the whole pipeline in a try/catch block
       this will also handle:
       - sending a complete and error fed msg
       - archving artifacts
       - sending pipeline metrics to influxdb
    */
    ciPipeline(buildPrefix: 'package-builds', completeMsg: msgComplete, errorMsg: msgError, package_name: package_name) {
    
        stage('koji-build') {
            // wrap the stage in extra debugging information.
            // plus time how long it takes to run the stage
               executeInContainer(containerName: 'koji-build-container, containerScript: 'verify-build.sh')
           
        }
        
        stage('package-tests') {
            // define variables that will be used by the container singlehost-test
            stageVars = [repo: 'myrepo', test-all: true]
                executeInContainer(containerName: 'singlehost-test, containerScript: 'package-tests.sh',
                stageVars: stageVars)

         }
    }
}
```
