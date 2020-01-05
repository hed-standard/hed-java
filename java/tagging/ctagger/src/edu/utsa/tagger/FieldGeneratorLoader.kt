package edu.utsa.tagger

import edu.utsa.tagger.gui.FieldGenerator
import tornadofx.launch

class FieldGeneratorLoader{
    constructor(jsonString: String) {
        launch<FieldGenerator>(jsonString)
    }
}
fun main(args: Array<String>) {
    FieldGeneratorLoader("{\"newFieldName\": \"trial_type\", \"eventFields\": {\"type\": [\"square\", \"rt\"], \"position\": [\"1\", \"2\"], \"test\": [\"square\", \"rt\"], \"test1\": [\"1\", \"2\"]}}")
}

