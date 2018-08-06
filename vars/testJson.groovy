def call() {

    def j = readJSON text: libraryResource('test.json')


    def result = [:]
    for (def m : j.entrySet()) {
        result[m.key] = m.value
    }

    println result

    def y = readYaml text: libraryResource('test.yaml')

    def result1 = [:]
    for (def m : y.entrySet()) {
        result1[m.key] = m.value
    }

    println result1

    
}

