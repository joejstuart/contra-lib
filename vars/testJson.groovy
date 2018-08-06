def call() {

    def j = readJSON text: libraryResource('test.json')

    for (def m : j.entrySet()) {
        println m.key
        println m.value
    }

    def y = readYaml text: libraryResource('test.yaml')
    
}

