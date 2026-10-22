val fmlLoaderVersion = rootProject.property("fml_loader_version") as String
val patchVersion = "0.1.0"

version = patchVersion
base {
    archivesName.set("synia-debugify")
}

dependencies {
    // Early FML SPI only — this jar is NOT a NeoForge mod (see Connector plugin loading rules).
    compileOnly("net.neoforged.fancymodloader:loader:$fmlLoaderVersion")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-api:2.0.16")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.16")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Synia Debugify Patch",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "Synia",
            "Automatic-Module-Name" to "org.synia.debugify",
        )
    }
}
