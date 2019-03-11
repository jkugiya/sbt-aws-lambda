package com.gilt.aws.lambda

import java.time.Instant

import software.amazon.awssdk.services.lambda.model._
// import com.amazonaws.services.lambda.model._
import scala.collection.JavaConverters._
import scala.util.Try

private[lambda] class AwsLambda(client: wrapper.AwsLambda) {

  def publishVersion(name: String, revisionId: String, version: String)
  : Try[PublishVersionResponse] = {
    val request = PublishVersionRequest.builder
      .functionName(name)
      .revisionId(revisionId)
      .description(version)
      .build
    client.publishVersion(request)
  }

  def updateLambdaWithFunctionCodeRequest(updateFunctionCodeRequest: UpdateFunctionCodeRequest, version: String): Try[UpdateFunctionCodeResponse] = {
    println(s"Updating lambda code ${updateFunctionCodeRequest.functionName}")
    for {
      updateResult <- client.updateFunctionCode(updateFunctionCodeRequest)
      _ = println(s"Updated lambda code ${updateResult.functionArn}")
      _ <- publishVersion(name = updateResult.functionName, revisionId = updateResult.revisionId, version = version)
    } yield {
      updateResult
    }
  }

  def tagLambda(functionArn: String, version: String) = {
    val tags = Map(
      "deploy.code.version" -> version,
      "deploy.timestamp" -> Instant.now.toString
    )

    val tagResourceReq = TagResourceRequest.builder
      .resource(functionArn)
      .tags(tags.asJava)
      .build

    client.tagResource(tagResourceReq)
  }

  def getLambdaConfig(functionName: LambdaName): Try[Option[GetFunctionConfigurationResponse]] = {
    val request = GetFunctionConfigurationRequest.builder
      .functionName(functionName.value)
      .build

    client.getFunctionConfiguration(request)
      .map(Option.apply)
      .recover {
        case _: ResourceNotFoundException => None
      }
  }

  def updateLambdaConfig(functionName: LambdaName,
                         handlerName: HandlerName,
                         roleName: RoleARN,
                         timeout:  Option[Timeout],
                         memory: Option[Memory],
                         deadLetterName: Option[DeadLetterARN],
                         vpcConfig: Option[VpcConfig],
                         environment: Environment,
                         version: String): Try[UpdateFunctionConfigurationResponse] = {

    var request = UpdateFunctionConfigurationRequest.builder
        .functionName(functionName.value)
        .handler(handlerName.value)
        .role(roleName.value)
        .runtime(Runtime.JAVA8)
        .environment(environment)

    request = timeout.fold(request)(t => request.withTimeout(t.value))
    request = memory.fold(request)(m => request.withMemorySize(m.value))
    request = vpcConfig.fold(request)(request.withVpcConfig)
    request = deadLetterName.fold(request)(d => request.withDeadLetterConfig(new DeadLetterConfig().withTargetArn(d.value)))

    for {
      updateResult <- client.updateFunctionConfiguration(request)
      _ = println(s"Updated lambda config ${updateResult.getFunctionArn}")
      _ <- publishVersion(name = updateResult.getFunctionName, revisionId = updateResult.getRevisionId, version = version)
    } yield {
      updateResult
    }
  }

  def createLambda(functionName: LambdaName,
                   handlerName: HandlerName,
                   roleName: RoleARN,
                   timeout:  Option[Timeout],
                   memory: Option[Memory],
                   deadLetterName: Option[DeadLetterARN],
                   vpcConfig: Option[VpcConfig],
                   functionCode: FunctionCode,
                   environment: Environment,
                   version: String): Try[CreateFunctionResponse] = {

    var request = new CreateFunctionRequest()
      .withFunctionName(functionName.value)
      .withHandler(handlerName.value)
      .withRole(roleName.value)
      .withRuntime(com.amazonaws.services.lambda.model.Runtime.Java8)
      .withEnvironment(environment)
      .withCode(functionCode)
    request = timeout.fold(request)(t => request.withTimeout(t.value))
    request = memory.fold(request)(m => request.withMemorySize(m.value))
    request = vpcConfig.fold(request)(request.withVpcConfig)
    request = deadLetterName.fold(request)(n => request.withDeadLetterConfig(new DeadLetterConfig().withTargetArn(n.value)))

    for {
      createResult <- client.createFunction(request)
      _ = println(s"Create lambda ${createResult.getFunctionArn}")
      _ <- publishVersion(name = createResult.getFunctionName, revisionId = createResult.getRevisionId, version = version)
    } yield {
      createResult
    }
  }
}
