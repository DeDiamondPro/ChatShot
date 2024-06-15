plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.blossom) apply false
    alias(egt.plugins.multiversionRoot)
}

preprocess {
    val fabric12001 = createNode("1.20.1-fabric", 12001, "yarn")
    val fabric12100 = createNode("1.21-fabric", 12100, "yarn")

    val neoforge12100 = createNode("1.21-neoforge", 12100, "mcp")

    val forge12001 = createNode("1.20.1-forge", 12001, "mcp")
    val forge12100 = createNode("1.21-forge", 12100, "mcp")

    fabric12100.link(fabric12001)

    neoforge12100.link(fabric12100)

    forge12001.link(fabric12001)
    forge12100.link(fabric12100)
}