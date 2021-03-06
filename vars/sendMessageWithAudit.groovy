import org.centos.contra.pipeline.Utils

/**
 * Example Usage:
 * msgProperties = ['msgTopic': 'stageName', 'msgProps': 'stage=stage1', 'msgContent': 'pipelineResult=success']
 * sendMessageWithAudit(msgProperties)
 *
 * @param parameters
 * @return
 */

def call(Map parameters = [:]) {
    def msgTopic = parameters.get('msgTopic')
    def msgProps = parameters.get('msgProps', '')
    def msgContent = parameters.get('msgContent', '')
    def msgAuditFile = parameters.get('msgAuditFile', 'auditfile.json')
    def msgRetryCount = parameters.get('msgRetryCount', 3)

    def utils = new Utils()

    def auditContent = null
    try {
        // Get contents of auditFile
        auditContent = readJSON file: msgAuditFile
    } catch(e) {
        // If could not read audit file, create it
        utils.initializeAuditFile(msgAuditFile)
        auditContent = readJSON file: msgAuditFile
    }

    // Send message and get handle on SendResult
    def sendResult = utils.sendMessage(msgTopic, msgProps, msgContent)

    String id = sendResult.getMessageId()
    String msg = sendResult.getMessageContent()

    auditContent[id] = msg

    // write to auditFile and archive
    writeJSON pretty: 4, file: msgAuditFile, json: auditContent

    archiveArtifacts allowEmptyArchive: false, artifacts: msgAuditFile

    utils.trackMessage(id, msgRetryCount)
}
