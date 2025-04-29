plugins {
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.blossom) apply false
    alias(egt.plugins.multiversionRoot)
    id("gg.essential.loom") version "1.7.23" apply false
}

preprocess {
    // val fabric12001 = createNode("1.20.1-fabric", 12001, "yarn")
    // val fabric12006 = createNode("1.20.6-fabric", 12006, "yarn")
    // val fabric12100 = createNode("1.21-fabric", 12100, "yarn")
    //val fabric12104 = createNode("1.21.4-fabric", 12104, "yarn")
    val fabric12105 = createNode("1.21.5-fabric", 12105, "yarn")

    // val neoforge12006 = createNode("1.20.6-neoforge", 12006, "mcp")
    // val neoforge12100 = createNode("1.21-neoforge", 12100, "mcp")
    //val neoforge12104 = createNode("1.21.4-neoforge", 12104, "mcp")
    val neoforge12105 = createNode("1.21.5-neoforge", 12105, "mcp")

    // val forge12001 = createNode("1.20.1-forge", 12001, "mcp")
    // val forge12100 = createNode("1.21-forge", 12100, "mcp")
    //val forge12104 = createNode("1.21.4-forge", 12104, "mcp")
    val forge12105 = createNode("1.21.5-forge", 12105, "mcp")

//    fabric12006.link(fabric12001)
//    fabric12100.link(fabric12006)

//    neoforge12006.link(fabric12006, file("versions/forge-neoforge"))
//    neoforge12100.link(forge12100, file("versions/forge-neoforge"))
    //neoforge12104.link(forge12104, file("versions/forge-neoforge"))
    neoforge12105.link(forge12105, file("versions/forge-neoforge"))

//    forge12001.link(fabric12001)
//    forge12100.link(fabric12100)
    //forge12104.link(fabric12104)
    forge12105.link(fabric12105)
}