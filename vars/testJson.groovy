def call() {

    def j = readJSON text: libraryResource('test.json')

    print j

    def y = readYaml text: libraryResource('test.yaml')

    print y
}

