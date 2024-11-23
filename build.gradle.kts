plugins {
    id("java-library")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.44-beta"
}

///
operator fun String.invoke(): String {
    return (rootProject.properties[this] as String?) ?: throw IllegalArgumentException("Couldn't find '$this' property")
}
///

version = file("VERSION").readText().trim()
group = "mod.group_id"()

repositories {
    mavenLocal()
    // JEI
    maven("https://modmaven.dev/mezz/jei/jei-1.21.1-neoforge/")
    maven("https://maven.k-4u.nl") // The One Probe
    maven("https://modmaven.dev/") // JEI, Mekanism
    maven("https://maven.izzel.io/releases/") // Modern UI
    maven("https://maven.gtceu.com") { // GregTech CEu Modern
        content { includeGroup("com.gregtechceu.gtceu") }
    }
    maven("https://maven.firstdarkdev.xyz/snapshots") // LDLib (For GT:CEuM)
    maven("https://maven.ithundxr.dev/snapshots") { // Registrate 1.20.4+
        content { includeGroup("com.tterrag.registrate") }
    }
    maven("https://cursemaven.com") {
        content { includeGroup("curse.maven") }
    }
    maven("https://maven.octo-studios.com/releases") { // Curios
        content { includeGroup("top.theillusivec4.curios") }
    }
    mavenCentral()
}

base {
    archivesName = "mod.artifact_name"() + "-" + "mod.minecraft"()
}

// Minecraft:
// 1.12-1.16.5 => Java 8
// 1.17-1.17.1 => Java 16
// 1.18-1.20.4 => Java 17
// 1.20.5+ => Java 21
java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    // Specify the version of NeoForge to use.
    version = "mod.neo_version"()

    parchment {
        parchment.enabled = "mappings.type"() == "parchment"

        val ver = "mappings.version"()
        if (ver.contains(":")) {
            val (mcVersion, date) = ver.split(":")
            mappingsVersion = date
            minecraftVersion = mcVersion
        } else {
            mappingsVersion = ver
            minecraftVersion = "mod.minecraft"()
        }
    }

    // This line is optional. Access Transformers are automatically detected
    // setAccessTransformers(file("src/main/resources/META-INF/accesstransformer.cfg"))

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        register("client") {
            ideName = "NeoForge Client"
            client()
            // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
            systemProperty("neoforge.enabledGameTestNamespaces", "mod.id"())
        }

        register("server") {
            ideName = "NeoForge Server"
            server()
            // TODO
            programArguments.add("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", "mod.id"())
        }

        // This run config launches GameTestServer and runs all registered gametests, then exits.
        // By default, the server will crash when no gametests are provided.
        // The gametest system is also enabled by default for other run configs under the /test command.
        register("gameTestServer") {
            type = "gameTestServer"
            ideName = "NeoForge GameTest Server"
            systemProperty("neoforge.enabledGameTestNamespaces", "mod.id"())
        }

        register("data") {
            ideName = "NeoForge Datagen"
            data()

            // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
            // gameDirectory = file("run-data")

            // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
            programArguments.addAll("--mod", "mod.id"(), "--all", "--output", file("src/generated/resources/").getAbsolutePath(), "--existing", file("src/main/resources/").getAbsolutePath())
        }

        // applies to all the run configs above
        configureEach {
            // Recommended logging data for userdev environment
            // The markers can be added/remove as needed separated by commas.
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")

            // Recommended logging level for the console
            // You can set various levels here.
            // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        // define mod <-> source bindings
        // these are used to tell the game which sources are for which mod
        // mostly optional in a single mod project
        // but multi mod projects should define one per mod
        register("mod.id"()) {
            sourceSet(sourceSets.main.get())
        }
    }
}

// Include resources generated by data generators.
sourceSets.main.configure {
    resources.srcDir("src/generated/resources")
}

// Sets up a dependency configuration called 'localRuntime'.
// This configuration should be used instead of 'runtimeOnly' to declare
// a dependency that will be present for runtime testing but that is
// "optional", meaning it will not be pulled by dependents of this mod.
val localRuntime = configurations.register("localRuntime") {
    extendsFrom(configurations.runtimeOnly.get())
}

dependencies {

//    compileOnly("mekanism:Mekanism:${"deps.mekanism"()}")
    compileOnly("mekanism:Mekanism:${"deps.mekanism"()}:api")
    localRuntime("mekanism:Mekanism:${"deps.mekanism"()}")

    // The JEI API is declared for compile time use, while the full JEI artifact is used at runtime
    compileOnly("mezz.jei:jei-${"mod.minecraft"()}-common-api:${"deps.jei"()}")
    compileOnly("mezz.jei:jei-${"mod.minecraft"()}-neoforge-api:${"deps.jei"()}")
    // We add the full version to localRuntime, not runtimeOnly, so that we do not publish a dependency on it
    localRuntime("mezz.jei:jei-${"mod.minecraft"()}-neoforge:${"deps.jei"()}")

    // The One Probe
    implementation("mcjty.theoneprobe:theoneprobe:${"deps.top"()}")

    // Curios
    compileOnly("top.theillusivec4.curios:curios-neoforge:${"deps.curios"()}")
    localRuntime("top.theillusivec4.curios:curios-neoforge:${"deps.curios"()}")

    // GregTech CEu Modern
    implementation("com.gregtechceu.gtceu:gtceu-${"mod.minecraft"()}:${"deps.gregtech_ceum"()}") {
        exclude("maven.modrinth", "embeddium") // No
    }

    // Modern UI
    implementation("icyllis.modernui:ModernUI-NeoForge:${"deps.modernui"()}") {
        exclude("org.apache.logging.log4j", "log4j-core")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.jetbrains", "annotations")
        exclude("com.ibm.icu", "icu4j")
        exclude("it.unimi.dsi", "fastutil")
    }
    implementation("icyllis.modernui:ModernUI-Core:${"deps.modernui_core"()}") {
        exclude("org.apache.logging.log4j", "log4j-core")
        exclude("org.apache.logging.log4j", "log4j-api")
        exclude("com.google.code.findbugs", "jsr305")
        exclude("org.jetbrains", "annotations")
        exclude("com.ibm.icu", "icu4j")
        exclude("it.unimi.dsi", "fastutil")
    }
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error.
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties: Map<String, String> = mapOf(
        "minecraft_version" to "mod.minecraft"(),
        "minecraft_version_range" to "mod.minecraft_range"(),
        "neo_version" to "mod.neo_version"(),
        "neo_version_range" to "mod.neo_range"(),
        "loader_version_range" to "mod.loader_range"(),
        "mod_id" to "mod.id"(),
        "mod_name" to "mod.name"(),
        "mod_license" to "mod.license"(),
        "mod_version" to project.version.toString(),
        "mod_authors" to "mod.authors"(),
        "mod_description" to "mod.description"()
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

val buildRelease = tasks.register("buildRelease") {
    group = "build"

    dependsOn("runData")
    dependsOn("build")
}

// Include the output of "generateModMetadata" as an input directory for the build
// this works with both building through Gradle and the IDE.
sourceSets.main.configure {
    resources.srcDir(generateModMetadata)
}
tasks.processResources.get().dependsOn(generateModMetadata)

// To avoid having to run "generateModMetadata" manually, make it run on every project reload
neoForge.ideSyncTasks.add(generateModMetadata)

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
// TODO: fix?
//idea.module.isDownloadJavadoc = true
//idea.module.isDownloadSources = true