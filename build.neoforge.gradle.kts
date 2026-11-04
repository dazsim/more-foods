plugins {
    id("net.neoforged.moddev")
    id("neoforge-mutex")
}

// -Pversion=2.1.0-alpha.1 overrides mod.version for test jars
val modVersion = project.version.toString().takeIf { it != "unspecified" } ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-neoforge-$mc"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}
val modern = sc.current.parsed >= "26.1"
fun prop(key: String) = runCatching { sc.properties.get<String>(key) }.getOrDefault("")
val jei = prop("deps.jei")

// json whose format changed between versions lives in src/eras, tools/gen_eras.py writes the older eras
val era = when {
    sc.current.parsed >= "26.1" -> "26.1"
    sc.current.parsed >= "1.21" -> "1.21.1"
    else -> "1.20.1"
}
sourceSets.main { resources.srcDir(rootProject.file("src/eras/$era")) }

repositories {
    maven("https://maven.blamejared.com") {
        name = "BlameJared"
        content { includeGroup("mezz.jei") }
    }
}

neoForge {
    version = sc.properties.get<String>("deps.neo_loader")
    if (modern) accessTransformers.from(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
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
        compileOnly("mezz.jei:jei-$mc-neoforge-api:$jei")
    }
}

sourceSets.main {
    java.exclude("com/workshopcraft/morefood/fabric/**", "com/workshopcraft/morefood/forge/**")
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
            "neoforge" to sc.properties.get<String>("deps.neo_compat"),
            "fml" to sc.properties.get<String>("deps.neo_fml_compat"),
            // pack_min and pack_max from 1.21.9, pack_format before
            "pack_min" to prop("mod.pack_min"),
            "pack_max" to prop("mod.pack_max"),
            "pack_format" to prop("mod.pack_format"),
            "logo_key" to if (sc.current.parsed >= "26.3") "iconFile" else "logoFile",
        )
        inputs.properties(props)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) { expand(props) }
        exclude("fabric.mod.json", "META-INF/mods.toml", "data/forge/**")
        if (!modern) exclude("$modId.accesswidener", "META-INF/accesstransformer.cfg")
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
            "Implementation-Title" to "neoforge",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
