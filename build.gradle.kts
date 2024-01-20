plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    kotlin("jvm")
}

group = "dev.loudbook"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveFileName = "yoinker.jar"
    minimize()

    manifest {
        attributes["Main-Class"] = "dev.loudbook.githubyoinker.Main"
    }
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    main {
        resources {
            srcDirs.add(File("src/main/resources"))
        }
    }
}