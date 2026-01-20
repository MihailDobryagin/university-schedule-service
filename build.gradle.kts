import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
  mavenCentral()
  maven("https://repo.clojars.org/")
}

plugins {
  kotlin("jvm") version "2.0.20"
  application
  java
  `maven-publish`
}

application {
  applicationName = project.name
  group = "com.mdobryagin"
  mainClass.set("LauncherKt")
  applicationDefaultJvmArgs += listOf("-XX:+ExitOnOutOfMemoryError", "-XX:+PrintFlagsFinal")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
  val ktorVersion = "3.2.3"
  val koinVersion = "4.0.0"

  implementation(kotlin("stdlib"))

  implementation(files("libs/cryptography.jar"))

  implementation("io.ktor:ktor-server-netty:$ktorVersion")
  implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
  implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
  implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
  implementation("io.ktor:ktor-client-okhttp-jvm:$ktorVersion")

  implementation("io.insert-koin:koin-core:$koinVersion")
  implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

  implementation("com.google.guava:guava:33.2.0-jre")

  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13.1")

  implementation("org.slf4j:slf4j-api:2.0.16")
  implementation("ch.qos.logback:logback-classic:1.5.12")

  implementation(files("libs/mails-lib.jar"))
  runtimeOnly("com.sun.mail:jakarta.mail:2.0.1")

  implementation("org.jsoup:jsoup:1.21.2")

  implementation(files("libs/tg-bot-lib.jar"))
  implementation("org.telegram:telegrambots:6.9.7.1")

  testImplementation("org.assertj:assertj-core:3.23.1")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  val junitVersion = "5.11.0"
  testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

configurations {
  all {
    exclude(module = "slf4j-log4j12")
  }
}

sourceSets {
  main {
    java.srcDir("src")
    resources.srcDir("resources")
  }

  test {
    java.srcDir("test")
    resources.srcDir("test")
  }
}

tasks.withType<KotlinCompile> {
  compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}
