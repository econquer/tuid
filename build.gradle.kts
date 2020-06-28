import com.adarshr.gradle.testlogger.theme.ThemeType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.3.72"

buildscript {
    val kotlinVersion = "1.3.72"
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    java
    maven
    `maven-publish`
    jacoco
    signing
    idea

    id("com.palantir.git-version") version "0.11.0"
    id("com.adarshr.test-logger") version "1.6.0"
}

apply {
    plugin("kotlin")
}

val gitVersion: groovy.lang.Closure<Any> by extra
version = gitVersion().toString().replaceFirst("([0-9]+\\.[0-9]+\\.[0-9](\\..*)?)".toRegex(), "$1");

configure<JavaPluginConvention> {
    group = "im.toss"
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")


    implementation("io.github.microutils:kotlin-logging:1.5.9")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("com.github.toss:assert-extensions:0.2.0")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks

compileTestKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "1.8"
}

jacoco {
    toolVersion = "0.8.4"
}

tasks {
    test {
        useJUnitPlatform {
        }
    }

    jacocoTestReport {
        executionData.setFrom(
            fileTree("build/jacoco") {
                include("**/*.exec")
            }
        )

        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }

    jacocoTestCoverageVerification {
        dependsOn(setOf(jacocoTestReport))
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    minimum = "1.000000000".toBigDecimal()
                }

                limit {
                    counter = "BRANCH"
                    minimum = "1.000000000".toBigDecimal()
                }
            }
        }
    }
}

testlogger {
    theme = ThemeType.STANDARD_PARALLEL
    showExceptions = true
    slowThreshold = 2000
    showSummary = true
    showPassed = true
    showSkipped = true
    showFailed = true
    showStandardStreams = false
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = false
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "im.toss"
            artifactId = "tuid"
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set("tuid")
                description.set("TUID(Time-sequential Unique Identifier)")
                url.set("https://github.com/toss/tuid")
                scm {
                    url.set("git@github.com:toss/tuid.git")
                    connection.set("scm:git:git@github.com:toss/tuid.git")
                    developerConnection.set("scm:git:git@github.com:toss/tuid.git")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Jinsung Oh")
                        email.set("econquer@toss.im")
                        organizationUrl.set("https://toss.im/")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}

signing {
    sign(publishing.publications["maven"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
