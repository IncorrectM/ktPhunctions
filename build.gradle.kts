plugins {
    kotlin("jvm") version "1.8.0"
    application
    id("com.github.johnrengelman.shadow").version("7.1.2")
}

group = "tech.zzhdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.jline/jline
    implementation("org.jline:jline:3.23.0")

    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "tech.zzhdev.phunctions.MainKt"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("tech.zzhdev.phunctions.MainKt")
}