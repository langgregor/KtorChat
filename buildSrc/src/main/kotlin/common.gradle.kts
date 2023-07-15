val ktorVersion: String by project

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
