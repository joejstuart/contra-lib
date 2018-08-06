def call() {

    def j = readJSON text: libraryResource('test.json')


    def result = [:]
    for (def m : j.entrySet()) {
        result[m.key] = m.value
    }

    println result

    def y = readYaml text: libraryResource('test.yaml')
    
}

