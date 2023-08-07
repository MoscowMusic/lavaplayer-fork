@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.ajoberstar.grgit.Grgit

plugins {
    id("org.ajoberstar.grgit") version "5.2.0"
    id("de.undercouch.download") version "5.4.0"
    alias(libs.plugins.maven.publish.base) apply false
}

val (gitVersion, release) = versionFromGit()
logger.lifecycle("Version: $gitVersion (release: $release)")

allprojects {
    group = "MoscowMusic"
    version = gitVersion

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    if (project.name == "natives" || project.name == "extensions-project") {
        return@subprojects
    }

    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    configure<PublishingExtension> {
        if (findProperty("MAVEN_PASSWORD") != null && findProperty("MAVEN_USERNAME") != null) {
            repositories {
                val snapshots = "https://maven.moscowmusic.su/snapshots"
                val releases = "https://maven.moscowmusic.su/releases"

                maven(if (release) releases else snapshots) {
                    credentials {
                        password = findProperty("MAVEN_PASSWORD") as String?
                        username = findProperty("MAVEN_USERNAME") as String?
                    }
                }
            }
        } else {
            logger.lifecycle("Not publishing to maven.moscowmusic.su because credentials are not set")
        }
    }

    afterEvaluate {
        plugins.withId(libs.plugins.maven.publish.base.get().pluginId) {
            configure<MavenPublishBaseExtension> {
                coordinates(group.toString(), project.the<BasePluginExtension>().archivesName.get(), version.toString())

                if (findProperty("mavenCentralUsername") != null && findProperty("mavenCentralPassword") != null) {
                    publishToMavenCentral(SonatypeHost.S01, false)
                    if (release) {
                        signAllPublications()
                    }
                } else {
                    logger.lifecycle("Not publishing to maven.moscowmusic.su due to missing credentials")
                }

                pom {
                    name = "lavaplayer"
                    description = "Modified Lavaplayer, which is used in the infrastructure of our application."
                    url = "https://github.com/MoscowMusic/lavaplayer-fork"

                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "https://github.com/lavalink-devs/lavaplayer/blob/main/LICENSE"
                        }
                    }

                    developers {
                        developer {
                            id = "mxscowc1ty"
                            name = "Kirill Blagochev"
                            url = "https://github.com/mxscowc1ty"
                        }
                    }

                    scm {
                        url = "https://github.com/MoscowMusic/lavaplayer-fork/"
                        connection = "scm:git:git://github.com/MoscowMusic/lavaplayer-fork.git"
                        developerConnection = "scm:git:ssh://git@github.com/MoscowMusic/lavaplayer-fork.git"
                    }
                }
            }
        }
    }
}

@SuppressWarnings("GrMethodMayBeStatic")
fun versionFromGit(): Pair<String, Boolean> {
    Grgit.open(mapOf("currentDir" to project.rootDir)).use { git ->
        val headTag = git.tag.list().last()

        return headTag.name to true
    }
}
