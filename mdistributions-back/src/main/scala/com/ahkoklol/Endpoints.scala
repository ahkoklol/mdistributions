package com.ahkoklol

import com.ahkoklol.domain.services.*
import com.ahkoklol.infra.http.{EmailEndpoints, Security, UserEndpoints}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import zio.Task
import zio.RIO

object Endpoints:
  // Define the total environment needed for all API endpoints
  type AppDependencies = UserEndpoints.UserEndpointsEnv & EmailEndpoints.EmailEndpointsEnv

  // Combine API endpoints from all modules
  val apiEndpoints: List[ZServerEndpoint[AppDependencies, Any]] =
    UserEndpoints.all ++ EmailEndpoints.all

  // Create Swagger endpoints
  val docEndpoints: List[ZServerEndpoint[Any, Any]] =
    SwaggerInterpreter()
      // Use .widen[Any] to satisfy Swagger's expectation of [Any, Task]
      .fromServerEndpoints[Task](apiEndpoints.map(_.widen[Any]), "mdistributions-back", "1.0.0")

  // Create Prometheus metrics endpoint
  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  // Combine all endpoints (API + Docs + Metrics)
  val all: List[ZServerEndpoint[AppDependencies, Any]] =
    apiEndpoints ++
    docEndpoints.map(_.widen[AppDependencies]) ++ // Widen doc/metric endpoints
    List(metricsEndpoint.widen[AppDependencies])

end Endpoints