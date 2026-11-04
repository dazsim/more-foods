plugins {
    id("net.neoforged.moddev.legacyforge")
    id("neoforge-mutex")
}

// -Pversion=2.1.0-alpha.1 overrides mod.version for test jars
val modVersion = project.version.toString().takeIf { it != "unspecified" } ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-forge-$mc"

val requiredJava = JavaVersion.VERSION_17
fun prop(key: String) = runCatching { sc.properties.get<String>(key) }.getOrDefault("")
val jei = prop("deps.jei")

sourceSets.main { resources.srcDir(rootProject.file("src/eras/1.20.1")) }

repositories {
    maven("https://maven.blamejared.com") {
        name = "BlameJared"
        content { includeGroup("mezz.jei") }
    }
}

legacyForge {
    version = sc.properties.get<String>("deps.forge")
    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }
    runs {
        register("client") {
            gameDirectory = rootProject.file("runs/${sc.current.project}")
            client()
        }
        register("server") {
            gameDirectory = rootProject.file("runs/${sc.current.project}-server")
            server()
        }
    }
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    if (jei.isNotEmpty()) {
        compileOnly("mezz.jei:jei-$mc-common-api:$jei")
        compileOnly("mezz.jei:jei-$mc-forge-api:$jei")
    }
}

sourceSets.main {
    java.exclude("com/workshopcraft/morefood/fabric/**", "com/workshopcraft/morefood/neoforge/**")
    if (jei.isEmpty()) java.exclude("com/workshopcraft/morefood/compat/jei/**")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain.languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
}

tasks {
    processResources {
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion,
            "author" to sc.properties.get<String>("mod.author"),
            "credits" to sc.properties.get<String>("mod.credits"),
            "license" to sc.properties.get<String>("mod.license"),
            "description" to sc.properties.get<String>("mod.description"),
            "minecraft" to sc.properties.get<String>("mod.mc_compat"),
            "pack_format" to prop("mod.pack_format"),
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) { expand(props) }
        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml", "data/neoforge/**", "$modId.accesswidener", "META-INF/accesstransformer.cfg")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    withType<Jar> {
        val name = sc.properties.get<String>("mod.name")
        inputs.property("mod_name", name)
        from(rootProject.file("LICENSE")) { rename { "${it}_$name" } }
    }

    jar {
        manifest.attributes(
            "Specification-Title" to modName,
            "Specification-Vendor" to sc.properties.get<String>("mod.author"),
            "Specification-Version" to modVersion,
            "Implementation-Title" to "forge",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
