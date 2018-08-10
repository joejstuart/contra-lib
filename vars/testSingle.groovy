import org.contralib.Utils


def call(Map parameters = [:]) {

    Map test = ['pipeline': ['test': 'one']]

    parameters.addNested(test)

    println parameters
}
