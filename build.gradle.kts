plugins {
    id("java")
    id("application")
}

group = "com.heroes_task.programs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(files("libs/heroes_task_lib-1.0-SNAPSHOT.jar"))
}

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveBaseName.set("HeroesAlgos")
}

tasks.test {
    useJUnitPlatform()
}