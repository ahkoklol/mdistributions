package com.ahkoklol

import com.ahkoklol.domain.services.*
import com.ahkoklol.infra.http.{EmailEndpoints, Security, UserEndpoints}
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.{ZServerEndpoint, RichZServerEndpoint}
import sttp.tapir.server.ServerEndpoint
import zio.Task

object Endpoints:
  type AppDependencies = UserEndpoints.UserEndpointsEnv & EmailEndpoints.EmailEndpointsEnv

  // Combine API endpoints
  val apiEndpoints: List[ZServerEndpoint[AppDependencies, Any]] =
    UserEndpoints.all.map(_.widen[AppDependencies]) ++
    EmailEndpoints.all.map(_.widen[AppDependencies])

  // Swagger endpoints: convert ZServerEndpoint to plain Tapir endpoints
  val swaggerEndpoints: List[ServerEndpoint[Any, Task]] =
    apiEndpoints.map(_.endpoint) // only the endpoint description, errors ignored for Swagger

  val docEndpoints: List[ServerEndpoint[Any, Task]] =
    SwaggerInterpreter()
      .fromServerEndpoints(
        swaggerEndpoints,
        "mdistributions-back",
        "1.0.0"
      )

  // Prometheus metrics
  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  // Combine all endpoints
  val all: List[ZServerEndpoint[Any, Any]] =
    apiEndpoints.map(_.widen[Any]) ++
    docEndpoints.map(se => ZServerEndpoint[Any, Any](se)) ++ // wrap plain endpoint into ZServerEndpoint
    List(metricsEndpoint)

end Endpoints
