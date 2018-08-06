def call() {

    def j = readJSON text: libraryResource('test.json')

    j['test'] = j['test']

    print j

    def y = readYaml text: libraryResource('test.yaml')

    
}

