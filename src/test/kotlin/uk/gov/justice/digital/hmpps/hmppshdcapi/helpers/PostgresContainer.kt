package uk.gov.justice.digital.hmpps.hmppshdcapi.helpers

import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.postgresql.PostgreSQLContainer
import java.io.IOException
import java.net.ServerSocket

object PostgresContainer {
  val instance: PostgreSQLContainer? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer? = if (checkPostgresRunning().not()) {
    PostgreSQLContainer("postgres:15.19").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withExposedPorts(5432)
      withDatabaseName("hmpps-hdc-api")
      withUsername("licences")
      withPassword("hmpps-hdc-api")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  } else {
    null
  }

  private fun checkPostgresRunning(): Boolean = try {
    val serverSocket = ServerSocket(5432)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  }
}
