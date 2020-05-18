package preachit

import ee.design.gen.go.DesignGoGenerator
import ee.lang.integ.dPath

fun main() {
    generateGo()
}

fun generateGo() {
    val generator = DesignGoGenerator(PreachIt, true)
    generator.generate(dPath, generator.generatorFactory.goEventDriven())
}

