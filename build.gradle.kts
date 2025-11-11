plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.aspectj.post-compile-weaving") version "8.4"
}

group = "ru.alumni.hub"
version = "0.0.1-SNAPSHOT"
description = "SubtleNotify"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // RSpringDoc OpenAPI (compatible with Spring Boot 3.x)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // Logback JSON formatting
    implementation("ch.qos.logback.contrib:logback-jackson:0.1.5")
    implementation("ch.qos.logback.contrib:logback-json-classic:0.1.5")

    // AspectJ for compile-time weaving
    implementation("org.aspectj:aspectjrt:1.9.20")
    aspect("org.springframework:spring-aspects")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.hsqldb:hsqldb")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
