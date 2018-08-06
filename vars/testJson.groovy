def call() {

    def j = readJSON text: libraryResource('test.json')

    print j['test']

    print j

    def y = readYaml text: libraryResource('test.yaml')

    
}

