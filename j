ciPipeline(buildPrefix: 'demo-build') {
    stage('build') {
        executeInContainer(containerName: 'build-container', containerScript: 'make build')
    }
}
