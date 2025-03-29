rootProject.name = "craft-engine"
include(":shared")
include(":core")
include(":bukkit")
include(":bukkit:legacy")
include(":bukkit:compatibility")
include(":bukkit:loader")
include(":server-mod")
include(":client-mod")
pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.20"
    }
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
    }
}
