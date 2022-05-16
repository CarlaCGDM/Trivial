import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//kdoc / plugin:dokka

plugins {
    kotlin("jvm") version "1.5.10"
    application
}

group = "me.usuariot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.anwiba.database:anwiba-database-sqlite:1.1.158")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.exposed", "exposed-core", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.38.1")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3") //sqlite

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
}

application {
    mainClass.set("MainKt")
}