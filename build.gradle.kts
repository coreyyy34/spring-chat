import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.gradle.node.npm.task.NpmTask
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "nz.coreyh"
version = "0.0.1-SNAPSHOT"

val exposedVersion = "0.61.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

node {
    version.set("22.12.0")
    download.set(true)
    nodeProjectDir.set(file("${projectDir}/src/main/frontend"))
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-messaging")
    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("io.jsonwebtoken:jjwt:0.12.6")

    implementation("org.webjars.npm:stomp__stompjs:7.1.1")
    implementation("io.konform:konform-jvm:0.11.1")

    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.h2database:h2")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val buildFrontend by tasks.registering(NpmTask::class) {
    dependsOn(tasks.npmInstall)
    args.set(listOf("run", "build"))
    inputs.files(fileTree("src/main/frontend") {
        include("css/**/*.css", "js/**/*.js")
    })
    inputs.files(fileTree("src/main/resources/templates") {
        include("**/*.html")
    })
    outputs.dir("src/main/resources/static")
}

tasks.processResources {
    dependsOn(buildFrontend)

    inputs.dir("src/main/frontend/dist")

    doLast {
        val manifestPath = projectDir.toPath().resolve("src/main/frontend/dist/.vite/manifest.json")
        val manifest = ObjectMapper().readValue(
            Files.readString(manifestPath),
            object : TypeReference<Map<String, Map<String, Any>>>() {}
        )
        val cssFileName = manifest["css/main.css"]?.get("file") as? String
            ?: error("CSS file not found in manifest")
        val jsFileName = manifest["js/main.js"]?.get("file") as? String
            ?: error("JS file not found in manifest")

        val templatesSrcDir = projectDir.toPath().resolve("src/main/resources/templates")
        val templatesOutDir = layout.buildDirectory.dir("resources/main/templates").get().asFile.toPath()
        templatesOutDir.toFile().mkdirs()

        Files.walk(templatesSrcDir)
            .filter { it.toString().endsWith(".html") }
            .forEach { srcFile ->
                val relativePath = templatesSrcDir.relativize(srcFile)
                val targetFile = templatesOutDir.resolve(relativePath)
                targetFile.parent.toFile().mkdirs()

                val content = srcFile.toFile().readText()
                    .replace("__CSS_FILE__", cssFileName)
                    .replace("__JS_FILE__", jsFileName)
                targetFile.toFile().writeText(content)
            }

        val sourceAssetsDir = projectDir.toPath().resolve("src/main/frontend/dist/assets")
        val targetAssetsDir = layout.buildDirectory.get().asFile.toPath().resolve("resources/main/static/assets")
        require(Files.exists(sourceAssetsDir)) { "The assets directory '$sourceAssetsDir' was not found. " }

        Files.walk(sourceAssetsDir)
            .forEach { sourcePath ->
                if (Files.isRegularFile(sourcePath)) {
                    val relativePath = sourceAssetsDir.relativize(sourcePath)
                    val targetPath = targetAssetsDir.resolve(relativePath)
                    targetPath.parent.toFile().mkdirs()
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
            }
    }
}