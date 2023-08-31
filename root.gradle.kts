plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.blossom) apply false
    alias(egt.plugins.multiversionRoot)
}

preprocess {
    val fabric12001 = createNode("1.20.1-fabric", 12001, "yarn")
    val forge12001 = createNode("1.20.1-forge", 12001, "mcp")

    forge12001.link(fabric12001)
}