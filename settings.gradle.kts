rootProject.name = "craft-engine"
include(":shared")
include(":core")
include(":bukkit")
include(":bukkit:legacy")
include(":bukkit-loader")
include(":server-mod")
pluginManagement {
    plugins {
        kotlin("jvm") version "2.0.20"
    }
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

