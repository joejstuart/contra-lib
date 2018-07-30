def call(Map parameters = [:]) {
    msgHeader = fedMsgHeader(namespace: 'testNameSpace')

    print msgHeader()
}