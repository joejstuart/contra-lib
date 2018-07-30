def call(Map parameters = [:]) {

    def msgHeader = fedMsgHeader(branch: 'newBranch')

    print msgHeader()
}