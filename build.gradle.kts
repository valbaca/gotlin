import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project

plugins {
    id("org.graalvm.buildtools.native") version "0.9.20"
    kotlin("jvm") version "1.8.20"
    application
}

group = "com.valbaca.gotlin"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    // skrape{it} for html parsing
    implementation("it.skrape:skrapeit:1.1.5")
    implementation("it.skrape:skrapeit-http-fetcher:1.1.5")
    implementation("it.skrape:skrapeit-browser-fetcher:1.1.5")
    implementation("it.skrape:skrapeit-async-fetcher:1.1.5")

    // http4k for requests and server
    implementation(platform("org.http4k:http4k-bom:4.42.1.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-client-apache")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("com.valbaca.gotlin.ch8.Reverb1Kt")
}