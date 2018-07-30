def call(Map parameters = [:]) {

    msgHeader = fedMsgHeader(branch: 'newBranch')

    print msgHeader()
}