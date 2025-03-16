plugins {
    kotlin("jvm") version "2.1.0"
}

group = "io.github.dockyard.demo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://mvn.devos.one/releases")
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.github.dockyardmc:dockyard:0.8.5")
    implementation("cz.lukynka.shulkerbox:common:2.5")
    implementation("cz.lukynka.shulkerbox:dockyard:2.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}