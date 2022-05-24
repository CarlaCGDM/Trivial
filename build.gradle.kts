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
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.exposed", "exposed-core", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.38.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.38.1")
    implementation("com.h2database:h2:2.1.212")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("net.anwiba.database:anwiba-database-sqlite:1.1.158")
    implementation("org.apache.commons:commons-csv:1.9.0")
    // https://mvnrepository.com/artifact/com.floern.castingcsv/casting-csv-kt
  //  implementation("com.floern.castingcsv:casting-csv-kt:1.2")
    // https://mvnrepository.com/artifact/com.github.doyaaaaaken/kotlin-csv-jvm
    //runtimeOnly("com.github.doyaaaaaken:kotlin-csv-jvm:1.3.0")

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