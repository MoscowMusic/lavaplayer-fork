@file:Suppress("UnstableApiUsage")

import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("org.ajoberstar.grgit") version "5.2.0"
    id("de.undercouch.download") version "5.4.0"
    alias(libs.plugins.maven.publish.base) apply false
}

val projectVersion = "2.0.2";
logger.lifecycle("Version: $projectVersion");

allprojects {
    group = "MoscowMusic";
    version = projectVersion;

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    if(project.name == "natives" || project.name == "extensions-project") {
        return@subprojects
    }

    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    configure<PublishingExtension> {
        if(findProperty("MAVEN_PASSWORD") != null && findProperty("MAVEN_USERNAME") != null) {
            repositories {
                val releases = "https://maven.moscowmusic.su/releases"

                maven(releases) {
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
