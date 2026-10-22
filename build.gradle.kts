plugins {
    java
}

allprojects {
    group = providers.gradleProperty("group").get()
    version = providers.gradleProperty("version").get()

    repositories {
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://libraries.minecraft.net") {
            name = "Minecraft"
        }
        maven("https://maven.su5ed.dev/releases") {
            name = "Sinytra"
            content {
                includeGroupAndSubgroups("org.sinytra")
            }
        }
        maven("https://maven.sinytra.org") {
            name = "SinytraAlt"
            content {
                includeGroupAndSubgroups("org.sinytra")
            }
        }
    }
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
