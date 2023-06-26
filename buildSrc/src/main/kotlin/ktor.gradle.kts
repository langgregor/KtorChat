val ktorVersion: String by project

plugins {
    id("common")
}

dependencies {
    implementation(project(":common"))

    // Client
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Server
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
}