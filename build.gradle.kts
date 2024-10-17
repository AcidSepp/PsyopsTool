plugins {
    alias(libs.plugins.kotlin)
    application
}

application {
    mainClass = "de.yw.psyops.MainKt"
}

group = "de.yw.psyops"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.jline)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.scripting.commmon)
    implementation(libs.kotlin.scripting.jvm)
    implementation(libs.kotlin.scripting.jvm.host)
    implementation(libs.kotlin.scripting.main.kts)

    testImplementation(kotlin("test"))
    testImplementation(libs.assertj)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.engine)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}