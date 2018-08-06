def call() {

    def j = readJSON text: libraryResource('test.json')

    print j
}

