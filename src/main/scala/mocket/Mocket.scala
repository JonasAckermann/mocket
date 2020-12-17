package mocket

import indigo._
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.util.Random

@JSExportTopLevel("IndigoGame")
object Mocket extends IndigoSandbox[Unit, Model] {

  val colors = Vector(RGB(240.0/255.0, 236.0/255.0, 228.0/255.0), RGB(230.0/255.0, 170.0/255.0, 51.0/255.0), RGB(5.0/255.0, 50.0/255.0, 255.0/255.0), RGB(0.0/255.0, 195.0/255.0, 160.0/255.0), RGB(242.0/255.0, 71.0/255.0, 71.0/255.0))

  val config: GameConfig = GameConfig(
    viewport = GameViewport(3840, 2160),
    frameRate = 60,
    clearColor = ClearColor.fromHexString("0x1A242C"),
    magnification = 1
  )

  val animations: Set[Animation] =
    Set()

  val rocketAssetName = AssetName("rocket")
  val boomAssetName = AssetName("boom")

  val assets: Set[AssetType] =
    Set(
      AssetType.Image(AssetName("rocket"), AssetPath("assets/rocket.png")),
      AssetType.Image(AssetName("boom"), AssetPath("assets/boom.png"))
    )

  val fonts: Set[FontInfo] =
    Set()

  def setup(
      assetCollection: AssetCollection,
      dice: Dice
  ): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Model = Model.initial

  def updateModel(
      context: FrameContext[Unit],
      model: Model
  ): GlobalEvent => Outcome[Model] = {
    case MouseEvent.Click(x, _) =>
      Outcome(model.addRocket(Rocket.initial(
        Point(x, config.viewport.height),
        Random.between(0, config.viewport.height / 2),
        Radians(Random.between(-0.02, 0.02)),
        colors(Random.between(0, colors.size))
      )))

    case FrameTick =>
      Outcome(model.update(context.delta))

    case _ =>
      Outcome(model)
  }

  def present(
      context: FrameContext[Unit],
      model: Model
  ): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
      drawRocket(model.rockets)
    )
    .withLights(drawLights(model.rockets))

  def drawRocket(
      rockets: List[Rocket]
  ): List[Graphic] =
    rockets.map(rocket => rocket.state match {
      case Boom(frame) =>  Graphic(Rectangle(0, 0, 439, 350), 1, Material.Textured(boomAssetName).lit)
        .withRef(220, 175)
        .rotate(rocket.angle)
        .scaleBy(scaleFromFrame(frame), scaleFromFrame(frame))
        .withAlpha(alphaFromFrame(frame))
        .moveTo(rocket.location)
      case _ => Graphic(Rectangle(0, 0, 512, 1204), 1, Material.Textured(rocketAssetName).lit)
        .withRef(256, 1204)
        .scaleBy(0.25, 0.25)
        .rotate(rocket.angle)
        .moveTo(rocket.location)
    }
    )

  // scale quickly at beginning, slower at end, up to ~2, assuming 50 frames
  def scaleFromFrame(frame: Int): Double = (Math.log(-frame.toDouble + 52))/2.0

  def alphaFromFrame(frame: Int): Double = (Math.log(frame.toDouble + 1.0)/4.0)


  def drawLights(rockets: List[Rocket]): List[PointLight] =
    rockets.map(rocket => rocket.state match {
      case Boom(_) => PointLight.default
        .moveTo(rocket.location)
        .withAttenuation(500) // How far the light fades out to
        .withColor(rocket.color)
        .withHeight(100)
        .withPower(20)
      case _ => PointLight.default.withPower(0.0)
    })


}

case class Model(rockets: List[Rocket]) {
  def addRocket(rocket: Rocket): Model =
    this.copy(rockets =  (rocket :: rockets).take(300))

  def update(timeDelta: Seconds): Model =
    this.copy(rockets = rockets.map(_.update(timeDelta)))
}
object Model {
  def initial: Model = Model(List.empty)
}
case class Rocket(location: Point, angle: Radians, rotateBy: Radians, explodeAt: Int, state: RocketState, color: RGB) {
  def update(timeDelta: Seconds): Rocket = state match {
    case Waiting => this
    case Flying if location.y < explodeAt => this.copy(state = Boom(50))
    case Flying =>
      val newAngle = angle + rotateBy
      val distance = (60.0*35*timeDelta.value)
      // 0 is 90 degrees off rocket heading -> switch sin/cos
      val newX = location.x - (distance*Math.sin(newAngle.value))
      val newY = location.y - (distance*Math.cos(newAngle.value))
      this.copy(location = Point(newX.toInt, newY.toInt ), angle = newAngle )
    case Boom(frame) if frame > 0 => this.copy(state = Boom(frame - 1))
    case Boom(_) => this.copy(location = Point(10000, 10000), state = Waiting) // TODO explodey steps
  }
}
object Rocket {
  def initial(location: Point, explodeAt: Int, rotateBy: Radians, color: RGB): Rocket = Rocket(location, Radians(0.0), rotateBy, explodeAt, Flying, color)
}

sealed trait RocketState
case object Waiting extends RocketState
case object Flying extends RocketState
case class Boom(frame: Int) extends RocketState