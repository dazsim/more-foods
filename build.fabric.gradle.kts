plugins {
    id("dev.kikugie.loom-back-compat")
}

// -Pversion=2.1.0-alpha.1 overrides mod.version for test jars
val modVersion = project.version.toString().takeIf { it != "unspecified" } ?: sc.properties.get<String>("mod.version")
val modId = sc.properties.get<String>("mod.id")
val modName = sc.properties.get<String>("mod.name")
val mc = sc.current.version

version = modVersion
group = sc.properties.get<String>("mod.group")
base.archivesName = "$modId-fabric-$mc"

val requiredJava = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
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

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")
    minecraft("com.mojang:minecraft:$mc")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${sc.properties.get<String>("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties.get<String>("deps.fabric_api")}")
    if (jei.isNotEmpty()) {
        modCompileOnly("mezz.jei:jei-$mc-common-api:$jei")
        modCompileOnly("mezz.jei:jei-$mc-fabric-api:$jei")
    }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    if (modern) accessWidenerPath = rootProject.file("src/main/resources/$modId.accesswidener")
    runConfigs.all {
        runDirectory = rootProject.file("runs/${sc.current.project}")
    }
}

sourceSets.main {
    java.exclude("com/workshopcraft/morefood/neoforge/**", "com/workshopcraft/morefood/forge/**")
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
            "loader" to sc.properties.get<String>("deps.fabric_loader"),
            "java" to requiredJava.majorVersion,
            // pack_min and pack_max from 1.21.9, pack_format before
            "pack_min" to prop("mod.pack_min"),
            "pack_max" to prop("mod.pack_max"),
            "pack_format" to prop("mod.pack_format"),
        )
        inputs.properties(props)
        filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) { expand(props) }
        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "data/neoforge/**", "data/forge/**")
        if (!modern) {
            filesMatching("fabric.mod.json") { filter(StripAccessWidener::class.java) }
            exclude("$modId.accesswidener", "META-INF/accesstransformer.cfg")
        }
        if (jei.isEmpty()) filesMatching("fabric.mod.json") { filter(StripJei::class.java) }
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
            "Implementation-Title" to "fabric",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to sc.properties.get<String>("mod.author"),
            "Built-On-Minecraft" to mc,
        )
    }
}
