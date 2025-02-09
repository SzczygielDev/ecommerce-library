plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.spring") version "1.9.23"
    id("com.gradleup.shadow") version "8.3.5"
    id("maven-publish")
}

group = "pl.szczygieldev"
version = "3.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.springframework.boot:spring-boot-starter:3.4.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    implementation("io.arrow-kt:arrow-core:1.2.4")
    implementation("com.trendyol:kediatr-core:3.1.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    runtimeOnly("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.55.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/szczygieldev/ecommerce-library")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register("gpr", MavenPublication::class) {
            from(components["java"])
        }
    }
}

tasks.bootJar {
    enabled = false
}