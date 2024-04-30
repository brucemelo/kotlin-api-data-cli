plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    application
}

group = "com.github.brucemelo"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    implementation("com.github.ajalt.clikt:clikt:4.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.github.brucemelo.MainKt"
}