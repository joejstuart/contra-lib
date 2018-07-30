def call(Map parameters = [:]) {
    def msgHeader = fedMsgHeader(namespace: 'testNameSpace')

    print msgHeader()
}