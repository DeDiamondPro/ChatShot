plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.9-neoforge" /* [SC] DO NOT EDIT */

stonecutter tasks {
    val ordering = versionComparator.thenComparingInt {
        if (it.metadata.project.endsWith("fabric")) 2
        else if (it.metadata.project.endsWith("neoforge")) 1
        else 0
    }
    order("publishModrinth", ordering)
    order("publishCurseforge", ordering)
}