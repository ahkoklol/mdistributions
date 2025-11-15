package com.ahkoklol

import sttp.tapir.*
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import sttp.tapir.ztapir.ZServerEndpoint
import com.ahkoklol.infrastructure.http.{EmailEndpoints, UserEndpoints}
import com.ahkoklol.domain.services.{EmailService, UserService}
import com.ahkoklol.infrastructure.utils.JwtUtility
import com.ahkoklol.infrastructure.http.Security.SecurityDeps

import zio.Task
import zio.ZIO
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}

object Endpoints:
  
  // Define a type alias for the full set of dependencies required by all API endpoints
  type ApiDependencies = UserService & EmailService & JwtUtility

  val apiEndpoints: List[ZServerEndpoint[ApiDependencies, Any]] = 
    UserEndpoints.all.asInstanceOf[List[ZServerEndpoint[ApiDependencies, Any]]] ++ 
    EmailEndpoints.all.asInstanceOf[List[ZServerEndpoint[ApiDependencies, Any]]]

  val docEndpoints: List[ZServerEndpoint[Any, Any]] = SwaggerInterpreter()
    // The issue was here: casting to ZServerEndpoint[R, Any] simplifies the type signature required by SwaggerInterpreter
    .fromServerEndpoints[Task](apiEndpoints.map(_.asInstanceOf[ZServerEndpoint[Any, Any]]), "mdistributions-back", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Task] = PrometheusMetrics.default[Task]()
  val metricsEndpoint: ZServerEndpoint[Any, Any] = prometheusMetrics.metricsEndpoint

  // All endpoints combined
  val all: List[ZServerEndpoint[ApiDependencies, Any]] = apiEndpoints ++ docEndpoints.map(_.asInstanceOf[ZServerEndpoint[ApiDependencies, Any]]) ++ List(metricsEndpoint.asInstanceOf[ZServerEndpoint[ApiDependencies, Any]])