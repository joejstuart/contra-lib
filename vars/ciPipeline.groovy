/**
 * requires: buildPrefix
 * Example Usage:
 *
 * ciPipeline(buildPrefix: 'fedora-pipeline') {
 *     stage('run-job') {
 *         handlePipelineStep {
 *             runCode()
 *         }
 *     }
 * }
 *
 * @param parameters
 * @param body
 * @return
 */
import org.contralib.ciMetrics


def call(Map parameters, Closure body) {
    def buildPrefix = parameters.get('buildPrefix')
    def packageName = parameters.get('package_name')
    def umbMessage = parameters.get('umbMessage')
    def decorateBuild = parameters.get('decorateBuild')
    def archiveArtifacts = parameters.get('archiveArtifacts')
    def timeoutValue = parameters.get('timeout', 30)
    def sendMetrics = parameters.get('sendMetrics', true)


    def cimetrics = ciMetrics.metricsInstance
    cimetrics.prefix = buildPrefix


    timeout(time: timeoutValue, unit: 'MINUTES') {

        try {
            if (umbMessage) {
                sendMessageWithAudit(umbMessage())
            }

            body()
        } catch (e) {
            // Set build result
            currentBuild.result = "FAILURE"

            echo e.toString()

            throw e
        } finally {
            currentBuild.result = currentBuild.result ?: 'SUCCESS'


            if (archiveArtifacts) {
                archiveArtifacts()
            }

            if (sendMetrics) {
                pipelineMetrics(buildPrefix: buildPrefix, package_name: packageName)
            }

            if (umbMessage) {
                runtimeMsg = ['pipeline': ['runtime': currentBuild.getDuration()]]
                sendMessageWithAudit(umbMessage(runtimeMsg))
            }

            if (decorateBuild) {
                decorateBuild()
            } else {
                currentBuild.displayName = "Build #${env.BUILD_NUMBER}"
                currentBuild.description = currentBuild.result
            }

        }
    }

}
