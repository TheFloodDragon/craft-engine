repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://r.irepo.space/maven/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // papi
    maven("https://maven.enginehub.org/repo/") // worldguard worldedit
    maven("https://repo.rapture.pw/repository/maven-releases/")  // slime world
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")  // slime world
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly(project(":core"))
    compileOnly("net.momirealms:sparrow-nbt:${rootProject.properties["sparrow_nbt_version"]}")
    // Platform
    compileOnly("dev.folia:folia-api:${rootProject.properties["paper_version"]}-R0.1-SNAPSHOT")
    // NeigeItems
    compileOnly("pers.neige.neigeitems:NeigeItems:1.21.42")
    // Placeholder
    compileOnly("me.clip:placeholderapi:${rootProject.properties["placeholder_api_version"]}")
    // WorldEdit
    compileOnly("com.sk89q.worldedit:worldedit-core:7.2.19")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.19")
    // SlimeWorld
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    dependsOn(tasks.clean)
}