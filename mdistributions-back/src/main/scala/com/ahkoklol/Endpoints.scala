package com.ahkoklol

import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.ztapir.ZServerEndpoint
import com.ahkoklol.infrastructure.http.{EmailEndpoints, UserEndpoints}
import com.ahkoklol.domain.services.{EmailService, UserService}
import com.ahkoklol.infrastructure.utils.JwtUtility
import com.ahkoklol.infrastructure.http.Security // Note: Security is needed for the dependencies used by the Endpoints

import zio.Task

object Endpoints:
  
  // Define a type alias for the full set of dependencies required by all API endpoints
  type ApiDependencies = UserService & EmailService & JwtUtility

  val apiEndpoints: List[ZServerEndpoint[ApiDependencies, Any]] = 
    UserEndpoints.all.asInstanceOf[List[ZServerEndpoint[ApiDependencies, Any]]] ++ 
    EmailEndpoints.all.asInstanceOf[List[ZServerEndpoint[ApiDependencies, Any]]]

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    .fromServerEndpoints[Task](apiEndpoints, "mdistributions-back", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  // All endpoints combined for the ZIO HTTP server
  val all: List[ZServerEndpoint[ApiDependencies, Any]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)